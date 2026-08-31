package io.nicheblog.dreamdiary.feature.user.account.service;

import io.nicheblog.dreamdiary.auth.config.AuthProperties;
import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyQueryService;
import io.nicheblog.dreamdiary.auth.security.repository.jpa.RoleRepository;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntityTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.account.model.UserDtoTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.feature.user.account.spec.UserSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 사용자 수정 경로가 canonical modify 계약의 전처리(preModify) hook을 보존하는지 검증한다.
 * <p>접속 IP 목록이 비면 {@code useAllowedIpYn}을 {@code N}으로 정규화하는 preModify가
 * {@code modify()} 안에서 실제로 실행되어야 한다.</p>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceModifyContractTest {

    @Mock
    private UserRepository repository;
    @Mock
    private UserSpec spec;
    @Mock
    private AuthPolicyQueryService authPolicyQueryService;
    @Mock
    private AuthProperties authProperties;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserPasswordHistoryService userPasswordHistoryService;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void modifyNormalizesEmptyAllowedIpToDisabled() throws Exception {
        final UserEntity modifyEntity = UserEntityTestFactory.create();
        final UserDto modifyDto = UserDtoTestFactory.create();
        modifyDto.setId(1);
        // IP 제한을 켠 채 허용 IP 목록이 비어 들어온 입력
        modifyDto.setUseAllowedIpYn("Y");
        modifyDto.setAllowedIpListStr("");
        when(repository.findById(1)).thenReturn(Optional.of(modifyEntity));
        when(repository.saveAndFlush(any())).thenReturn(modifyEntity);

        userService.modify(modifyDto);

        // preModify가 modify 안에서 실행되어 빈 목록 입력을 미사용으로 정규화한다.
        assertEquals("N", modifyDto.getUseAllowedIpYn());
        assertNull(modifyDto.getAllowedIpListStr());
    }
}
