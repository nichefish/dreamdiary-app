package io.nicheblog.dreamdiary.auth.permission.repository.jpa;

import io.nicheblog.dreamdiary.auth.permission.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PermissionRepository
 *
 * @author nichefish
 */
@Repository
public interface PermissionRepository
        extends JpaRepository<PermissionEntity, Integer> {

    Optional<PermissionEntity> findByPermKey(String permKey);

    List<PermissionEntity> findByUseYnOrderBySortOrderAscPermKeyAsc(String useYn);

    @Query("""
            SELECT DISTINCT p.permKey
            FROM RolePermissionEntity rp
            JOIN rp.permission p
            WHERE rp.roleId IN :roleIds
              AND p.useYn = 'Y'
            """)
    List<String> findPermKeysByRoleIds(@Param("roleIds") List<Integer> roleIds);

    @Query("""
            SELECT DISTINCT p.permKey
            FROM GroupPermissionEntity gp
            JOIN gp.permission p
            WHERE gp.groupId IN :groupIds
              AND p.useYn = 'Y'
            """)
    List<String> findPermKeysByGroupIds(@Param("groupIds") List<Integer> groupIds);
}
