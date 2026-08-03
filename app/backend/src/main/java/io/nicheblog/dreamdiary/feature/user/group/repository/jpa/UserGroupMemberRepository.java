package io.nicheblog.dreamdiary.feature.user.group.repository.jpa;

import io.nicheblog.dreamdiary.feature.user.group.entity.UserGroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserGroupMemberRepository
 *
 * @author nichefish
 */
@Repository
public interface UserGroupMemberRepository
        extends JpaRepository<UserGroupMemberEntity, Integer> {

    List<UserGroupMemberEntity> findByUserId(Integer userId);

    List<UserGroupMemberEntity> findByGroupId(Integer groupId);

    Optional<UserGroupMemberEntity> findByUserIdAndGroupId(Integer userId, Integer groupId);

    void deleteByUserIdAndGroupId(Integer userId, Integer groupId);

    long countByGroupId(Integer groupId);
}
