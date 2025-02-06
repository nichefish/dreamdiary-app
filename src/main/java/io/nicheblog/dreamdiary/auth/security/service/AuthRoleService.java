package io.nicheblog.dreamdiary.auth.security.service;

import io.nicheblog.dreamdiary.auth.security.entity.AuthRoleEntity;
import io.nicheblog.dreamdiary.auth.security.mapstruct.AuthRoleMapstruct;
import io.nicheblog.dreamdiary.auth.security.model.AuthRoleDto;
import io.nicheblog.dreamdiary.auth.security.repository.jpa.AuthRoleRepository;
import io.nicheblog.dreamdiary.auth.security.spec.AuthRoleSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseReadonlyService;

/**
 * AuthRoleService
 * <pre>
 *  Spring Security:: 인증 및 권한 처리 관련 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface AuthRoleService
        extends BaseReadonlyService<AuthRoleDto, AuthRoleDto, String, AuthRoleEntity, AuthRoleRepository, AuthRoleSpec, AuthRoleMapstruct> {
    //
}