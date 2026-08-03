package io.nicheblog.dreamdiary.auth.permission.repository.jpa;

import io.nicheblog.dreamdiary.auth.permission.entity.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RolePermissionRepository
 *
 * @author nichefish
 */
@Repository
public interface RolePermissionRepository
        extends JpaRepository<RolePermissionEntity, Integer> {

    List<RolePermissionEntity> findByRoleId(Integer roleId);

    void deleteByRoleIdAndPermissionId(Integer roleId, Integer permissionId);
}
