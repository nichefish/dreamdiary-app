package io.nicheblog.dreamdiary.auth.service;

import io.nicheblog.dreamdiary.auth.entity.AuthRoleEntity;
import io.nicheblog.dreamdiary.auth.mapstruct.AuthRoleMapstruct;
import io.nicheblog.dreamdiary.auth.model.AuthRoleDto;
import io.nicheblog.dreamdiary.auth.repository.jpa.AuthRoleRepository;
import io.nicheblog.dreamdiary.auth.spec.AuthRoleSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseReadonlyService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * AuthRoleService
 * <pre>
 *  Spring Security:: 인증 및 권한 처리 관련 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AuthRoleService
        implements BaseReadonlyService<AuthRoleDto, AuthRoleDto, String, AuthRoleEntity, AuthRoleRepository, AuthRoleSpec, AuthRoleMapstruct> {

    @Getter
    private final AuthRoleRepository repository;
    @Getter
    private final AuthRoleSpec spec;
    @Getter
    private final AuthRoleMapstruct mapstruct = AuthRoleMapstruct.INSTANCE;
}