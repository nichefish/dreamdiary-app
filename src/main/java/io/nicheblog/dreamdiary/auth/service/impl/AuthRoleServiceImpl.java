package io.nicheblog.dreamdiary.auth.service.impl;

import io.nicheblog.dreamdiary.auth.mapstruct.AuthRoleMapstruct;
import io.nicheblog.dreamdiary.auth.repository.jpa.AuthRoleRepository;
import io.nicheblog.dreamdiary.auth.service.AuthRoleService;
import io.nicheblog.dreamdiary.auth.spec.AuthRoleSpec;
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
public class AuthRoleServiceImpl
        implements AuthRoleService {

    @Getter
    private final AuthRoleRepository repository;
    @Getter
    private final AuthRoleSpec spec;
    @Getter
    private final AuthRoleMapstruct mapstruct = AuthRoleMapstruct.INSTANCE;
}