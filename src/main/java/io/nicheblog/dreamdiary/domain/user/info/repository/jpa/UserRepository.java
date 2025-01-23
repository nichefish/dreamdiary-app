package io.nicheblog.dreamdiary.domain.user.info.repository.jpa;

import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository
 * <pre>
 *  사용자 관리 > 사용자(계정) 관리 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("userRepository")
public interface UserRepository
    extends BaseStreamRepository<UserEntity, Integer> {

    /**
     * 사용자 ID로 사용자 엔티티를 검색합니다.
     *
     * @param userId 검색할 사용자 ID
     * @return {@link Optional} -- 사용자 엔티티를 포함한 Optional 객체
     */
    Optional<UserEntity> findByUserId(final String userId);

    /**
     * 사용자 이메일로 사용자 엔티티를 검색합니다.
     *
     * @param email 검색할 사용자 이메일
     * @return {@link Optional} -- 사용자 엔티티를 포함한 Optional 객체
     */
    Optional<UserEntity> findByEmail(final String email);
}