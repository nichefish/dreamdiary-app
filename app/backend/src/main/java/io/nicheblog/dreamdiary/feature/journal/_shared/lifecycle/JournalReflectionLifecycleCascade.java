package io.nicheblog.dreamdiary.feature.journal._shared.lifecycle;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.AttachableCacheContext;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.LifecycleSetDto;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.repository.jpa.LifecycleRepository;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.StateEntity;
import io.nicheblog.dreamdiary.feature.attachable.state.model.CacheContext;
import io.nicheblog.dreamdiary.feature.attachable.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.feature.attachable.state.repository.jpa.StateRepository;
import io.nicheblog.dreamdiary.feature.attachable.state.service.StateService;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayResolvedGuard;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

/**
 * Reflection 과 일기·꿈·노트(primary target) 사이의 라이프사이클 연쇄.
 *
 * <p>계약({@code docs/spec/REFLECTION_ONE_TYPE.md} §5): 저장은 독립, 완료·재개는 부모 주도.
 * primary 가 {@code RESOLVED} 이면 딸린 Reflection 도 {@code RESOLVED} 로 맞추고,
 * {@code RESOLVED} primary 에 Reflection 을 신규로 묶으면 primary 를 {@code OPEN} 으로 되돌린다.
 * Reflection→Reflection 과 자식→부모 완료 연쇄는 하지 않는다.</p>
 */
@Component
@Log4j2
public class JournalReflectionLifecycleCascade {

    private static final EnumSet<ContentType> PRIMARY_TARGET_TYPES = EnumSet.of(
            ContentType.JOURNAL_DIARY,
            ContentType.JOURNAL_DREAM,
            ContentType.JOURNAL_NOTE
    );

    private final JournalReflectionRepository journalReflectionRepository;
    private final LifecycleRepository lifecycleRepository;
    private final StateRepository stateRepository;
    private final StateService stateService;
    private final JournalDayResolvedGuard journalDayResolvedGuard;
    private final LifecycleService lifecycleService;

    public JournalReflectionLifecycleCascade(
            final JournalReflectionRepository journalReflectionRepository,
            final LifecycleRepository lifecycleRepository,
            final StateRepository stateRepository,
            final StateService stateService,
            final JournalDayResolvedGuard journalDayResolvedGuard,
            final @Lazy LifecycleService lifecycleService
    ) {
        this.journalReflectionRepository = journalReflectionRepository;
        this.lifecycleRepository = lifecycleRepository;
        this.stateRepository = stateRepository;
        this.stateService = stateService;
        this.journalDayResolvedGuard = journalDayResolvedGuard;
        this.lifecycleService = lifecycleService;
    }

    /**
     * primary 엔트리가 {@code RESOLVED} 로 바뀐 뒤, 그 엔트리를 target 으로 둔 Reflection 을 같은 값으로 맞춘다.
     *
     * @param parent 방금 설정된 primary 라이프사이클 요청
     */
    public void cascadeResolvedToAttachedReflections(final LifecycleSetDto parent) {
        if (parent == null || parent.getId() == null || parent.getContentType() == null) return;
        if (!LifecycleKey.RESOLVED.equals(parent.getLifecycleKey())) return;
        if (!PRIMARY_TARGET_TYPES.contains(parent.getContentType())) return;

        final List<JournalReflectionEntity> reflections = journalReflectionRepository
                .findAllByRefIdAndRefContentType(parent.getId(), parent.getContentType());
        if (reflections.isEmpty()) return;

        log.info(
                "[ReflectionLifecycleCascade] primary RESOLVED → 딸린 Reflection {}건. targetId={}, targetType={}",
                reflections.size(),
                parent.getId(),
                parent.getContentType()
        );

        for (final JournalReflectionEntity reflection : reflections) {
            if (reflection == null || reflection.getId() == null) continue;
            try {
                lifecycleService.set(LifecycleSetDto.builder()
                        .id(reflection.getId())
                        .contentType(ContentType.JOURNAL_REFLECTION)
                        .lifecycleKey(LifecycleKey.RESOLVED)
                        .cacheContext(parent.getCacheContext())
                        .build());
            } catch (final Exception e) {
                log.error(
                        "[ReflectionLifecycleCascade] Reflection RESOLVED 연쇄 실패. reflectionId={}, targetId={}: {}",
                        reflection.getId(),
                        parent.getId(),
                        e.getMessage(),
                        e
                );
                throw (e instanceof RuntimeException re) ? re : new IllegalStateException(e);
            }
        }
    }

    /**
     * {@code RESOLVED} primary 에 Reflection 을 새로 묶을 때 primary 를 {@code OPEN} 으로 되돌리고,
     * 파생 {@code COLLAPSED} 가 켜져 있으면 끈다(다시 손대기 신호).
     * 일자 축 잠금이면 primary 쓰기를 건너뛴다(Reflection 자체 등록은 별축).
     *
     * @param targetId primary 엔트리 ID
     * @param targetContentType primary 콘텐츠 타입
     * @param cacheContext 캐시 컨텍스트(없으면 맵 부분 갱신 생략)
     */
    public void reopenPrimaryTargetIfResolved(
            final Integer targetId,
            final ContentType targetContentType,
            final AttachableCacheContext cacheContext
    ) {
        if (targetId == null || targetContentType == null) return;
        if (!PRIMARY_TARGET_TYPES.contains(targetContentType)) return;

        final String currentKey = lifecycleRepository
                .findByRefIdAndRefContentType(targetId, targetContentType.key)
                .map(entity -> entity.getLifecycleKey())
                .orElse(LifecycleKey.OPEN.key);
        if (!LifecycleKey.RESOLVED.key.equals(currentKey)) return;

        try {
            journalDayResolvedGuard.assertWritableForRef(targetId, targetContentType);
        } catch (final BusinessException e) {
            log.info(
                    "[ReflectionLifecycleCascade] 일자 축 잠금으로 primary 재개 생략. targetId={}, targetType={}",
                    targetId,
                    targetContentType
            );
            return;
        }

        log.info(
                "[ReflectionLifecycleCascade] RESOLVED primary 에 Reflection 신규 → OPEN. targetId={}, targetType={}",
                targetId,
                targetContentType
        );

        try {
            lifecycleService.set(LifecycleSetDto.builder()
                    .id(targetId)
                    .contentType(targetContentType)
                    .lifecycleKey(LifecycleKey.OPEN)
                    .cacheContext(cacheContext)
                    .build());
        } catch (final Exception e) {
            log.error(
                    "[ReflectionLifecycleCascade] primary OPEN 재개 실패. targetId={}, targetType={}: {}",
                    targetId,
                    targetContentType,
                    e.getMessage(),
                    e
            );
            throw (e instanceof RuntimeException re) ? re : new IllegalStateException(e);
        }

        clearCollapsedIfPresent(targetId, targetContentType, cacheContext);
    }

    private void clearCollapsedIfPresent(
            final Integer id,
            final ContentType contentType,
            final AttachableCacheContext cacheContext
    ) {
        final StateEntity collapsed = stateRepository.findByRefIdAndRefContentTypeAndStateKey(
                id,
                contentType.key,
                StateKey.COLLAPSED.key
        );
        if (collapsed == null) return;

        try {
            final CacheContext stateCache = cacheContext == null ? null : CacheContext.builder()
                    .yy(cacheContext.getYy())
                    .mnth(cacheContext.getMnth())
                    .weekStartDt(cacheContext.getWeekStartDt())
                    .build();
            stateService.toggle(StateToggleDto.builder()
                    .id(id)
                    .contentType(contentType)
                    .stateKey(StateKey.COLLAPSED)
                    .cacheContext(stateCache)
                    .build());
        } catch (final Exception e) {
            log.error(
                    "[ReflectionLifecycleCascade] COLLAPSED 해제 실패. id={}, contentType={}: {}",
                    id,
                    contentType,
                    e.getMessage(),
                    e
            );
            throw (e instanceof RuntimeException re) ? re : new IllegalStateException(e);
        }
    }
}