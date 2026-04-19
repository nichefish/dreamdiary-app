package io.nicheblog.dreamdiary.feature.user.account.repository.jpa;

import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.EntityGraph;
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
@Repository
public interface UserRepository
    extends BaseStreamRepository<UserEntity, Integer> {

    /**
     * 사용자 ID로 사용자 엔티티를 검색합니다.
     * (로그인 시 권한 매핑을 위해 user_role → role 을 함께 로드)
     *
     * @param username 검색할 사용자 계정명
     * @return {@link Optional} -- 사용자 엔티티를 포함한 Optional 객체
     */
    @EntityGraph(attributePaths = {"userRoles", "userRoles.roleInfo"})
    Optional<UserEntity> findByUsername(final String username);

    /**
     * 사용자 이메일로 사용자 엔티티를 검색합니다.
     * (OAuth 이후에도 동일하게 역할 그래프 로드)
     *
     * @param email 검색할 사용자 이메일
     * @return {@link Optional} -- 사용자 엔티티를 포함한 Optional 객체
     */
    @EntityGraph(attributePaths = {"userRoles", "userRoles.roleInfo"})
    Optional<UserEntity> findByEmail(final String email);
}

