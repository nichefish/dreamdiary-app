package io.nicheblog.dreamdiary.feature.attachable.state.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.StateCd;
import io.nicheblog.dreamdiary.feature.attachable.state.adapter.StateCacheUpdater;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.StateEntity;
import io.nicheblog.dreamdiary.feature.attachable.state.mapstruct.StateMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.state.model.StateDto;
import io.nicheblog.dreamdiary.feature.attachable.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.feature.attachable.state.repository.jpa.StateRepository;
import io.nicheblog.dreamdiary.feature.attachable.state.spec.StateSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.TransactionHookUtils;
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

    /**
     * 전략 validation
     */
    @PostConstruct
    private void validateStateCacheUpdaters() {
        final Set<ContentType> requiredTypes = EnumSet.of(
                ContentType.JOURNAL_CHAPTER,
                ContentType.JOURNAL_DIARY,
                ContentType.JOURNAL_DREAM,
                ContentType.JOURNAL_INTERPRETATION
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
     * 상태 토글
     * @param stateToggle StateToggleDto
     * @return ServiceResponse
     */
    @Transactional
    public ServiceResponse toggle(final StateToggleDto stateToggle) throws Exception {

        final StateEntity existingEntity = this.getSelf().getDtlEntity(stateToggle);
        final boolean isEnabled = existingEntity == null;
        if (isEnabled) {
            final StateEntity newState = StateEntity.of(stateToggle);
            repository.save(newState);
        } else {
            repository.delete(existingEntity);
        }

        // 의미 전이 규칙 처리
        this.applyDerivedStates(stateToggle, isEnabled);
        // 캐시 처리
        this.scheduleCacheUpdateAfterCommit(stateToggle, isEnabled);

        return ServiceResponse.builder()
                .rslt(true)
                .rsltSts(isEnabled ? "ON" : "OFF")
                .build();
    }

    /**
     * 캐시 처리
     * @param stateToggle StateToggleDto
     * @param isEnabled Boolean
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
     * 상태 등록 시 파생 상태 처리
     * @param stateToggle StateToggleDto
     */
    private void applyDerivedStates(final StateToggleDto stateToggle, final Boolean isEnabled) throws Exception {
        if (StateCd.RESOLVED.equals(stateToggle.getStateCode())) {
            if (isEnabled) {
                final StateToggleDto collapsedToggle = StateToggleDto.builder()
                        .id(stateToggle.getId())
                        .contentType(stateToggle.getContentType())
                        .stateCode(StateCd.COLLAPSED)
                        .cacheContext(stateToggle.getCacheContext())
                        .build();

                final StateEntity collapsed = this.getSelf().getDtlEntity(collapsedToggle);
                if (collapsed == null) {
                    repository.save(StateEntity.of(collapsedToggle));
                }
                // 캐시 처리
                this.scheduleCacheUpdateAfterCommit(collapsedToggle, true);
            }
        }
    }

    /**
     * commit 이후 상태 캐시를 반영한다.
     */
    private void scheduleCacheUpdateAfterCommit(final StateToggleDto stateToggle, final Boolean isEnabled) throws Exception {
        if (stateToggle.getCacheContext() == null) return;

        TransactionHookUtils.runAfterCommitOrNow(
                () -> doCache(stateToggle, isEnabled),
                e -> log.error(
                        "State cache update failed [{}:{}:{}]: {}",
                        stateToggle.getContentType(),
                        stateToggle.getId(),
                        stateToggle.getStateCode(),
                        e.getMessage(),
                        e
                )
        );
    }

    /**
     * 기존 상태 조회
     * @param stateToggle StateToggleDto
     * @return StateEntity 상태
     */
    public StateEntity getDtlEntity(StateToggleDto stateToggle) throws Exception {
        return repository.findByRefIdAndRefContentTypeAndStateCode(stateToggle.getId(), stateToggle.getContentType().key, stateToggle.getStateCode().key);
    }
}

