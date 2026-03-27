package io.nicheblog.dreamdiary.feature.clsf.state.repository.jpa;

import io.nicheblog.dreamdiary.feature.clsf.state.entity.StateEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * StateRepository
 * <pre>
 *  상태 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("stateRepository")
public interface StateRepository
        extends BaseStreamRepository<StateEntity, Integer> {

    StateEntity findByRefPostNoAndRefContentTypeAndStateCd(final Integer postNo, final String key, final String stateCd);

    //
}
