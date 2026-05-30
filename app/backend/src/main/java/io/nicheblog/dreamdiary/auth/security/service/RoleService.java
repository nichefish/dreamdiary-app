package io.nicheblog.dreamdiary.auth.security.service;

import io.nicheblog.dreamdiary.auth.security.entity.RoleEntity;
import io.nicheblog.dreamdiary.auth.security.mapstruct.RoleMapstruct;
import io.nicheblog.dreamdiary.auth.security.model.RoleDto;
import io.nicheblog.dreamdiary.auth.security.repository.jpa.RoleRepository;
import io.nicheblog.dreamdiary.auth.security.spec.RoleSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * RoleService
 * <pre>
 *  Spring Security:: 인증 및 권한 처리 관련 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class RoleService
        implements BaseDtoReadableService<RoleDto, Integer, RoleEntity> {

    @Getter
    private final RoleRepository repository;
    @Getter
    private final RoleSpec spec;
    @Getter
    private final RoleMapstruct mapstruct = RoleMapstruct.INSTANCE;

    public RoleMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public RoleMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }
}
