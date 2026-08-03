package io.nicheblog.dreamdiary.feature.user.group.repository.jpa;

import io.nicheblog.dreamdiary.feature.user.group.entity.UserGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserGroupRepository
 *
 * @author nichefish
 */
@Repository
public interface UserGroupRepository
        extends JpaRepository<UserGroupEntity, Integer>,
                JpaSpecificationExecutor<UserGroupEntity> {

    Optional<UserGroupEntity> findByGroupKey(String groupKey);

}
