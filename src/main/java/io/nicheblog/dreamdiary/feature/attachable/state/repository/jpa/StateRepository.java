package io.nicheblog.dreamdiary.feature.attachable.state.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.state.entity.StateEntity;
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
@Repository
public interface StateRepository
        extends BaseStreamRepository<StateEntity, Integer> {

    StateEntity findByRefIdAndRefContentTypeAndStateCode(final Integer id, final String key, final String stateCode);

    //
}

