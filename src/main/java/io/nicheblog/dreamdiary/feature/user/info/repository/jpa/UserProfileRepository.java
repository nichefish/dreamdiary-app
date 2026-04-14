package io.nicheblog.dreamdiary.feature.user.info.repository.jpa;

import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

@Repository("userProfileRepository")
public interface UserProfileRepository extends BaseStreamRepository<UserProfileEntity, Integer> {
    //
}
