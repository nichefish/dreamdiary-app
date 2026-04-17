package io.nicheblog.dreamdiary.feature.user.reqst.repository.jpa;

import io.nicheblog.dreamdiary.feature.user.reqst.entity.UserSignupRequestEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserSignupRequestRepository extends BaseRepository<UserSignupRequestEntity, Integer> {
    Optional<UserSignupRequestEntity> findTopByUsernameAndRequestStatusOrderByCreatedAtDesc(String username, String requestStatus);
    List<UserSignupRequestEntity> findByRequestStatusOrderByCreatedAtDesc(String requestStatus);
    boolean existsByUsernameAndRequestStatus(String username, String requestStatus);
    boolean existsByEmailAndRequestStatus(String email, String requestStatus);
}
