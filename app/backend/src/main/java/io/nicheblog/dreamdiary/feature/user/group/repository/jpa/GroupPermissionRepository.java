package io.nicheblog.dreamdiary.feature.user.group.repository.jpa;

import io.nicheblog.dreamdiary.feature.user.group.entity.GroupPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * GroupPermissionRepository
 *
 * @author nichefish
 */
@Repository
public interface GroupPermissionRepository
        extends JpaRepository<GroupPermissionEntity, Integer> {

    List<GroupPermissionEntity> findByGroupId(Integer groupId);

    Optional<GroupPermissionEntity> findByGroupIdAndPermissionId(Integer groupId, Integer permissionId);

    void deleteByGroupIdAndPermissionId(Integer groupId, Integer permissionId);
}
