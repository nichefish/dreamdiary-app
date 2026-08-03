package io.nicheblog.dreamdiary.feature.attachable.state.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.adapter.StateCacheUpdater;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.StateEntity;
import io.nicheblog.dreamdiary.feature.attachable.state.mapstruct.StateMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.state.model.StateDto;
import io.nicheblog.dreamdiary.feature.attachable.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.feature.attachable.state.policy.AttachableContentStatePolicy;
import io.nicheblog.dreamdiary.feature.attachable.state.repository.jpa.StateRepository;
import io.nicheblog.dreamdiary.feature.attachable.state.spec.StateSpec;
import io.nicheblog.dreamdiary.feature.journal._shared.security.JournalContentOwnershipGuard;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayResolvedGuard;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.TransactionHookUtils;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * StateService
 * <pre>
 *  상태 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class StateService
        implements BaseDtoWritableService<StateDto, StateDto, Integer, StateEntity> {

    @Getter
    private final StateRepository repository;
    @Getter
    private final StateSpec spec;
    @Getter
    private final StateMapstruct mapstruct = StateMapstruct.INSTANCE;

    private final List<StateCacheUpdater> cacheUpdaters;
    private final JournalContentOwnershipGuard journalContentOwnershipGuard;
    private final JournalDayResolvedGuard journalDayResolvedGuard;

    /**
     * 캐시 갱신 전략이 필요한 컨텐츠 타입마다 하나씩 등록되어 있는지 검증한다.
     */
    @PostConstruct
    private void validateStateCacheUpdaters() {
        final Set<ContentType> requiredTypes = EnumSet.of(
                ContentType.JOURNAL_CHAPTER,
                ContentType.JOURNAL_DIARY,
                ContentType.JOURNAL_DREAM
        );

        for (final ContentType requiredType : requiredTypes) {
            int supportsCount = 0;
            for (final StateCacheUpdater updater : cacheUpdaters) {
                if (updater.supports(requiredType)) supportsCount++;
            }
            if (supportsCount == 0) {
                throw new IllegalStateException("Missing StateCacheUpdater for ContentType: " + requiredType);
            }
            if (supportsCount > 1) {
                throw new IllegalStateException("Duplicate StateCacheUpdater mapping for ContentType: " + requiredType);
            }
            if (!AttachableContentStatePolicy.registeredContentTypes().contains(requiredType)) {
                throw new IllegalStateException(
                        "AttachableContentStatePolicy must define allowed StateKeys for cache-backed type: " + requiredType);
            }
        }
    }

    public StateMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public StateMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationContext context;
    private StateService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 상태 값을 토글한다.
     *
     * @param stateToggle 상태 토글 요청 DTO
     * @return 서비스 처리 응답
     */
    @Transactional
    public ServiceResponse toggle(final StateToggleDto stateToggle) throws Exception {

        if (stateToggle.getContentType() == null || stateToggle.getStateKey() == null) {
            return ServiceResponse.builder()
                    .rslt(false)
                    .message(MessageUtils.getMessage("state.error.missing-params"))
                    .build();
        }
        if (!AttachableContentStatePolicy.isAllowed(stateToggle.getContentType(), stateToggle.getStateKey())) {
            return ServiceResponse.builder()
                    .rslt(false)
                    .message(MessageUtils.getMessage("state.error.not-allowed", new Object[]{stateToggle.getContentType().key, stateToggle.getStateKey().key}))
                    .build();
        }

        journalContentOwnershipGuard.assertOwned(stateToggle.getId(), stateToggle.getContentType());
        if (stateToggle.getContentType() != ContentType.JOURNAL_DAY) {
            journalDayResolvedGuard.assertWritableForRef(stateToggle.getId(), stateToggle.getContentType());
        }

        final StateEntity existingEntity = this.getSelf().getDtlEntity(stateToggle);
        final boolean isEnabled = existingEntity == null;
        if (isEnabled) {
            final StateEntity newState = StateEntity.of(stateToggle);
            repository.save(newState);
        } else {
            repository.delete(existingEntity);
        }

        // 캐시 처리
        this.scheduleCacheUpdateAfterCommit(stateToggle, isEnabled);

        return ServiceResponse.builder()
                .rslt(true)
                .rsltSts(isEnabled ? "ON" : "OFF")
                .build();
    }

    /**
     * 상태 변경에 따른 캐시를 처리한다.
     *
     * @param stateToggle 상태 토글 요청 DTO
     * @param isEnabled 활성화 여부
     */
    public void doCache(final StateToggleDto stateToggle, final Boolean isEnabled) throws Exception {
        for (final StateCacheUpdater updater : cacheUpdaters) {
            if (updater.supports(stateToggle.getContentType())) {
                updater.update(stateToggle, isEnabled);
                break;
            }
        }
    }

    /**
     * 커밋 이후 상태 캐시를 반영한다.
     */
    private void scheduleCacheUpdateAfterCommit(final StateToggleDto stateToggle, final Boolean isEnabled) throws Exception {
        if (stateToggle.getCacheContext() == null) return;

        TransactionHookUtils.runAfterCommitOrNow(
                () -> doCache(stateToggle, isEnabled),
                e -> log.error(
                        "State cache update failed [{}:{}:{}]: {}",
                        stateToggle.getContentType(),
                        stateToggle.getId(),
                        stateToggle.getStateKey(),
                        e.getMessage(),
                        e
                )
        );
    }

    /**
     * 기존 상태 엔티티를 조회한다.
     *
     * @param stateToggle 상태 토글 요청 DTO
     * @return 상태 엔티티
     */
    public StateEntity getDtlEntity(StateToggleDto stateToggle) throws Exception {
        return repository.findByRefIdAndRefContentTypeAndStateKey(
                stateToggle.getId(), stateToggle.getContentType().key, stateToggle.getStateKey().key);
    }
}
