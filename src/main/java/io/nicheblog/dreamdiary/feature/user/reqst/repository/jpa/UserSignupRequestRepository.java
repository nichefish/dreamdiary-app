package io.nicheblog.dreamdiary.feature.user.reqst.repository.jpa;

import io.nicheblog.dreamdiary.feature.user.reqst.entity.UserSignupRequestEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSignupRequestRepository extends BaseRepository<UserSignupRequestEntity, Integer> {
    Optional<UserSignupRequestEntity> findTopByUsernameAndStatusOrderByCreatedAtDesc(String username, String status);
    List<UserSignupRequestEntity> findByStatusOrderByCreatedAtDesc(String status);
    boolean existsByUsernameAndStatus(String username, String status);
    boolean existsByEmailAndStatus(String email, String status);
}
