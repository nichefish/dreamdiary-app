package io.nicheblog.dreamdiary.auth.security.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.entity.RoleEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * RoleRepository
 * <pre>
 *  권한 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface RoleRepository
        extends BaseStreamRepository<RoleEntity, Integer> {

    RoleEntity findByRoleKey(String roleKey);
}


