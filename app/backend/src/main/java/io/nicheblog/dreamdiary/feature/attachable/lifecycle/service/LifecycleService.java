package io.nicheblog.dreamdiary.feature.attachable.lifecycle.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.AttachableCacheContext;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.adapter.LifecycleCacheUpdater;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.entity.LifecycleEntity;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.LifecycleSetDto;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.policy.AttachableContentLifecyclePolicy;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.repository.jpa.LifecycleRepository;
import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.StateEntity;
import io.nicheblog.dreamdiary.feature.attachable.state.model.CacheContext;
import io.nicheblog.dreamdiary.feature.attachable.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.feature.attachable.state.repository.jpa.StateRepository;
import io.nicheblog.dreamdiary.feature.attachable.state.service.StateService;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayResolvedGuard;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.TransactionHookUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 부착 가능 컨텐츠의 라이프사이클 축을 처리하는 서비스.
 *
 * <p>라이프사이클은 state처럼 여러 개를 켜고 끄는 값이 아니라, 컨텐츠의 현재 진행 단계를 나타내는 단일값이다.
 * 그래서 API 의미도 {@code toggle}이 아니라 {@code set}으로 둔다.</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class LifecycleService {

    private final LifecycleRepository repository;
    private final StateRepository stateRepository;
    private final StateService stateService;
    private final List<LifecycleCacheUpdater> cacheUpdaters;
    private final JournalDayResolvedGuard journalDayResolvedGuard;

    /**
     * 컨텐츠 하나의 현재 라이프사이클 값을 설정한다.
     *
     * <p>{@code contentType + id} 기준으로 row 하나를 upsert한다. 새 값이 {@code RESOLVED}이면
     * 기존 화면 동작과 맞추기 위해 {@code COLLAPSED} state를 함께 켠다.</p>
     *
     * @param lifecycleSet 라이프사이클 설정 요청 및 캐시 컨텍스트
     * @return 이전 라이프사이클과 현재 라이프사이클을 담은 처리 결과
     * @throws Exception 캐시 훅 등록 또는 저장 처리 중 예외가 발생한 경우
     */
    @Transactional
    public ServiceResponse set(final LifecycleSetDto lifecycleSet) throws Exception {
        if (lifecycleSet.getContentType() == null || lifecycleSet.getLifecycleKey() == null) {
            return ServiceResponse.builder()
                    .rslt(false)
                    .message(MessageUtils.getMessage("lifecycle.required"))
                    .build();
        }
        if (!AttachableContentLifecyclePolicy.isAllowed(lifecycleSet.getContentType(), lifecycleSet.getLifecycleKey())) {
            return ServiceResponse.builder()
                    .rslt(false)
                    .message(MessageUtils.getMessage(
                            "lifecycle.not-allowed",
                            new Object[]{
                                    lifecycleSet.getContentType().key,
                                    lifecycleSet.getLifecycleKey().key
                            }))
                    .build();
        }

        if (lifecycleSet.getContentType() != ContentType.JOURNAL_DAY) {
            journalDayResolvedGuard.assertWritableForRef(lifecycleSet.getId(), lifecycleSet.getContentType());
        }

        final LifecycleEntity lifecycle = repository.findByRefIdAndRefContentType(
                        lifecycleSet.getId(),
                        lifecycleSet.getContentType().key
                )
                .orElse(null);
        final LifecycleKey previousKey = lifecycle == null
                ? null
                : LifecycleKey.getByKey(lifecycle.getLifecycleKey());

        if (lifecycle == null) {
            repository.save(LifecycleEntity.of(lifecycleSet));
        } else {
            lifecycle.setLifecycleKey(lifecycleSet.getLifecycleKey().key);
            repository.save(lifecycle);
        }

        final StateToggleDto derivedCollapsedToggle = this.applyDerivedStates(lifecycleSet);
        this.scheduleCacheUpdateAfterCommit(
                lifecycleSet,
                previousKey,
                lifecycleSet.getLifecycleKey(),
                derivedCollapsedToggle
        );

        final Map<String, String> rsltObj = new LinkedHashMap<>();
        rsltObj.put("previousLifecycleKey", previousKey == null ? null : previousKey.key);
        rsltObj.put("currentLifecycleKey", lifecycleSet.getLifecycleKey().key);

        return ServiceResponse.builder()
                .rslt(true)
                .rsltObj(rsltObj)
                .build();
    }

    /**
     * 저널 UX에 남아 있는 state 파생 처리를 적용한다.
     *
     * <p>{@code RESOLVED}의 소유권은 라이프사이클로 옮겼지만, 완료된 글은 기본적으로 접혀야 한다.
     * 반대로 {@code OPEN}이나 {@code PENDING}으로 바뀌었다고 {@code COLLAPSED}를 자동 해제하지는 않는다.
     * 글접기는 진행 단계가 아니라 표시 선호에 가깝기 때문이다.</p>
     *
     * @param lifecycleSet 현재 라이프사이클 설정 요청
     */
    private StateToggleDto applyDerivedStates(final LifecycleSetDto lifecycleSet) {
        if (!LifecycleKey.RESOLVED.equals(lifecycleSet.getLifecycleKey())) return null;

        final StateToggleDto collapsedToggle = StateToggleDto.builder()
                .id(lifecycleSet.getId())
                .contentType(lifecycleSet.getContentType())
                .stateKey(StateKey.COLLAPSED)
                .cacheContext(toStateCacheContext(lifecycleSet.getCacheContext()))
                .build();

        final StateEntity collapsed = stateRepository.findByRefIdAndRefContentTypeAndStateKey(
                collapsedToggle.getId(),
                collapsedToggle.getContentType().key,
                collapsedToggle.getStateKey().key
        );
        if (collapsed == null) {
            stateRepository.save(StateEntity.of(collapsedToggle));
        }
        return collapsedToggle;
    }

    /**
     * lifecycle 요청에서 전달된 저널 캐시 컨텍스트를 state 캐시 updater 형식으로 옮긴다.
     */
    private CacheContext toStateCacheContext(final AttachableCacheContext cacheContext) {
        if (cacheContext == null) return null;
        return CacheContext.builder()
                .yy(cacheContext.getYy())
                .mnth(cacheContext.getMnth())
                .weekStartDt(cacheContext.getWeekStartDt())
                .build();
    }

    /**
     * 트랜잭션 커밋 이후 저널 라이프사이클 캐시를 갱신하도록 예약한다.
     *
     * @param lifecycleSet 현재 라이프사이클 설정 요청
     * @param previousKey 변경 전 라이프사이클
     * @param currentKey 변경 후 라이프사이클
     * @throws Exception 트랜잭션 훅 등록 중 예외가 발생한 경우
     */
    private void scheduleCacheUpdateAfterCommit(
            final LifecycleSetDto lifecycleSet,
            final LifecycleKey previousKey,
            final LifecycleKey currentKey,
            final StateToggleDto derivedCollapsedToggle
    ) throws Exception {
        if (lifecycleSet.getCacheContext() == null) return;

        TransactionHookUtils.runAfterCommitOrNow(
                () -> {
                    doCache(lifecycleSet, previousKey, currentKey);
                    if (derivedCollapsedToggle != null) {
                        stateService.doCache(derivedCollapsedToggle, true);
                    }
                },
                e -> log.error(
                        "Lifecycle cache update failed [{}:{}:{}]: {}",
                        lifecycleSet.getContentType(),
                        lifecycleSet.getId(),
                        lifecycleSet.getLifecycleKey(),
                        e.getMessage(),
                        e
                )
        );
    }

    /**
     * contentType을 지원하는 updater에 라이프사이클 캐시 갱신을 위임한다.
     *
     * @param lifecycleSet 현재 라이프사이클 설정 요청
     * @param previousKey 변경 전 라이프사이클
     * @param currentKey 변경 후 라이프사이클
     */
    public void doCache(
            final LifecycleSetDto lifecycleSet,
            final LifecycleKey previousKey,
            final LifecycleKey currentKey
    ) {
        for (final LifecycleCacheUpdater updater : cacheUpdaters) {
            if (updater.supports(lifecycleSet.getContentType())) {
                updater.update(lifecycleSet, previousKey, currentKey);
                break;
            }
        }
    }

    /**
     * 목록 화면 병합용 라이프사이클 값을 {@code id -> key} 형태로 조회한다.
     *
     * @param contentType 부착 가능 컨텐츠 타입
     * @param refIds 라이프사이클을 붙일 컨텐츠 ID 목록
     * @return 컨텐츠 ID 기준 라이프사이클 키 맵
     */
    @Transactional(readOnly = true)
    public Map<Integer, String> getLifecycleMap(final ContentType contentType, final List<Integer> refIds) {
        if (contentType == null || refIds == null || refIds.isEmpty()) return Map.of();
        return repository.findAllByRefContentTypeAndRefIdIn(contentType.key, refIds).stream()
                .filter(lifecycle -> lifecycle.getRefId() != null && lifecycle.getLifecycleKey() != null)
                .collect(Collectors.toMap(
                        LifecycleEntity::getRefId,
                        LifecycleEntity::getLifecycleKey,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    /**
     * 비어 있거나 알 수 없는 라이프사이클 값을 화면 기본값인 {@code OPEN}으로 정규화한다.
     *
     * @param lifecycleKey 저장된 라이프사이클 키
     * @return 해석된 라이프사이클 키. 없으면 {@link LifecycleKey#OPEN}
     */
    public static LifecycleKey normalize(final String lifecycleKey) {
        final LifecycleKey key = LifecycleKey.getByKey(lifecycleKey);
        return key == null ? LifecycleKey.OPEN : key;
    }
}
