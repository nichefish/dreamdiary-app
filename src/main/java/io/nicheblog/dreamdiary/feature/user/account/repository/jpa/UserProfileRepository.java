package io.nicheblog.dreamdiary.feature.user.account.repository.jpa;

import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends BaseStreamRepository<UserProfileEntity, Integer> {
    //
}

