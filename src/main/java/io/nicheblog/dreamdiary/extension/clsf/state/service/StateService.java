package io.nicheblog.dreamdiary.extension.clsf.state.service;

import io.nicheblog.dreamdiary.extension.clsf.state.entity.StateEntity;
import io.nicheblog.dreamdiary.extension.clsf.state.mapstruct.StateMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.state.model.StateDto;
import io.nicheblog.dreamdiary.extension.clsf.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.extension.clsf.state.repository.jpa.StateRepository;
import io.nicheblog.dreamdiary.extension.clsf.state.spec.StateSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

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
        implements BaseCrudService<StateDto, StateDto, Integer, StateEntity> {

    @Getter
    private final StateRepository repository;
    @Getter
    private final StateSpec spec;
    @Getter
    private final StateMapstruct mapstruct = StateMapstruct.INSTANCE;

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

        final StateEntity entity = this.getSelf().getDtlEntity(stateToggle);
        final boolean isRegist = entity == null;
        String status;
        if (isRegist) {
            final StateEntity newState = StateEntity.of(stateToggle);
            repository.save(newState);
            status = "ON";
        } else {
            repository.delete(entity);
            status = "OFF";
        }

        return ServiceResponse.builder()
                .rslt(true)
                .rsltSts(status)
                .build();
    }

    public StateEntity getDtlEntity(StateToggleDto stateToggle) throws Exception {
        return repository.findByRefPostNoAndRefContentTypeAndStateCd(stateToggle.getPostNo(), stateToggle.getContentType().key, stateToggle.getStateCd().key);
    }
}