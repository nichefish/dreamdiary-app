package io.nicheblog.dreamdiary.feature.user.signup.repository.jpa;

import io.nicheblog.dreamdiary.feature.user.signup.entity.UserSignupRequestEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * `user_signup_request` JPA 저장소.
 *
 * 명명 규약: 테이블 대응 레포지토리는 {@code UserSignupRequest*}.
 */
@Repository
public interface UserSignupRequestRepository extends BaseRepository<UserSignupRequestEntity, Integer> {
    Optional<UserSignupRequestEntity> findTopByUsernameAndStatusOrderByCreatedAtDesc(String username, String status);
    List<UserSignupRequestEntity> findByStatusOrderByCreatedAtDesc(String status);
    boolean existsByUsernameAndStatus(String username, String status);
    boolean existsByEmailAndStatus(String email, String status);
}
