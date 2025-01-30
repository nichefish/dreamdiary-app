package io.nicheblog.dreamdiary.auth.service;

import io.nicheblog.dreamdiary.auth.entity.AuthRoleEntity;
import io.nicheblog.dreamdiary.auth.mapstruct.AuthRoleMapstruct;
import io.nicheblog.dreamdiary.auth.model.AuthRoleDto;
import io.nicheblog.dreamdiary.auth.repository.jpa.AuthRoleRepository;
import io.nicheblog.dreamdiary.auth.spec.AuthRoleSpec;
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