package io.nicheblog.dreamdiary.feature.clsf.state.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.feature.clsf.state.adapter.StateCacheUpdater;
import io.nicheblog.dreamdiary.feature.clsf.state.adapter.impl.JrnlStateCacheUpdater;
import io.nicheblog.dreamdiary.feature.clsf.state.entity.StateEntity;
import io.nicheblog.dreamdiary.feature.clsf.state.mapstruct.StateMapstruct;
import io.nicheblog.dreamdiary.feature.clsf.state.model.StateDto;
import io.nicheblog.dreamdiary.feature.clsf.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.feature.clsf.state.repository.jpa.StateRepository;
import io.nicheblog.dreamdiary.feature.clsf.state.spec.StateSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * StateService
 * <pre>
 *  상태 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("stateService")
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

    private final List<JrnlStateCacheUpdater> cacheUpdaters;

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
        if (stateToggle.getCacheContext() != null) {
            doCache(stateToggle, isEnabled);
        }

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
        final Integer yy = stateToggle.getCacheContext().getYy();
        final Integer mnth = stateToggle.getCacheContext().getMnth();
        final String cacheKey = AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth;

        for (final StateCacheUpdater updater : cacheUpdaters) {
            if (updater.supports(stateToggle.getContentType())) {
                updater.update(stateToggle, cacheKey, isEnabled);
                break;
            }
        }
    }

    /**
     * 상태 등록 시 파생 상태 처리
     * @param stateToggle StateToggleDto
     */
    private void applyDerivedStates(final StateToggleDto stateToggle, final Boolean isEnabled) throws Exception {
        if (StateCd.RESOLVED.equals(stateToggle.getStateCd())) {
            if (isEnabled) {
                final StateToggleDto collapsedToggle = StateToggleDto.builder()
                        .postNo(stateToggle.getPostNo())
                        .contentType(stateToggle.getContentType())
                        .stateCd(StateCd.COLLAPSED)
                        .cacheContext(stateToggle.getCacheContext())
                        .build();

                final StateEntity collapsed = this.getSelf().getDtlEntity(collapsedToggle);
                if (collapsed == null) {
                    repository.save(StateEntity.of(collapsedToggle));
                }
                // 캐시 처리
                if (stateToggle.getCacheContext() != null) {
                    doCache(collapsedToggle, true);
                }
            }
        }
    }

    /**
     * 기존 상태 조회
     * @param stateToggle StateToggleDto
     * @return StateEntity 상태
     */
    public StateEntity getDtlEntity(StateToggleDto stateToggle) throws Exception {
        return repository.findByRefPostNoAndRefContentTypeAndStateCd(stateToggle.getPostNo(), stateToggle.getContentType().key, stateToggle.getStateCd().key);
    }
}
