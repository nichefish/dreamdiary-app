package io.nicheblog.dreamdiary.feature.user.info.repository.jpa;

import io.nicheblog.dreamdiary.feature.user.profl.entity.UserProflEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * UserProflRepository
 * <pre>
 *  사용자 관리 > 사용자 프로필 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("userProflRepository")
public interface UserProflRepository
        extends BaseStreamRepository<UserProflEntity, Integer> {
    //
}
