package io.nicheblog.dreamdiary.feature.user.my.service;

import io.nicheblog.dreamdiary.auth.jwt.service.RefreshTokenService;
import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyQueryService;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.feature.user.account.service.UserPasswordHistoryService;
import io.nicheblog.dreamdiary.feature.user.account.service.UserService;
import io.nicheblog.dreamdiary.feature.user.my.model.UserMyUpdateRequest;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 로그인 사용자 개인 프로필 수정 계약 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class UserMyServiceTest {

    private static final String FIXTURE_USERNAME = "alice";
    private static final String FIXTURE_NICKNAME = "Alice";
    private static final String FIXTURE_PHONE_NUMBER = "010-0000-0000";
    private static final String FIXTURE_PROFILE_CONTENT = "가상 사용자 소개";
    private static final LocalDate FIXTURE_BIRTH_DATE = LocalDate.of(2000, 1, 1);

    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthPolicyQueryService authPolicyQueryService;
    @Mock
    private UserPasswordHistoryService userPasswordHistoryService;
    @InjectMocks
    private UserMyService service;

    private MockedStatic<AuthUtils> authUtils;
    private MockedStatic<EhCacheUtils> cacheUtils;

    @BeforeEach
    void setUp() {
        authUtils = mockStatic(AuthUtils.class);
        cacheUtils = mockStatic(EhCacheUtils.class);
        authUtils.when(AuthUtils::getLoginUsername).thenReturn(FIXTURE_USERNAME);
    }

    @AfterEach
    void tearDown() {
        cacheUtils.close();
        authUtils.close();
    }

    @Test
    void modifyMyInfoUpdatesOnlyPersonalProfileFields() throws Exception {
        final UserProfileEntity profile = UserProfileEntity.builder().build();
        final UserEntity user = user(profile);
        when(userService.getDtlEntity(FIXTURE_USERNAME)).thenReturn(user);
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        final boolean result = service.modifyMyInfo(request());

        assertTrue(result);
        assertEquals(FIXTURE_NICKNAME, user.getNickname());
        assertEquals(FIXTURE_PHONE_NUMBER, user.getPhoneNumber());
        assertEquals(FIXTURE_BIRTH_DATE, profile.getBrthdy());
        assertEquals("Y", profile.getLunarYn());
        assertEquals(FIXTURE_PROFILE_CONTENT, profile.getProflCn());
        verify(userRepository).saveAndFlush(user);
        cacheUtils.verify(() -> EhCacheUtils.evictCacheByKey("auditorInfo", FIXTURE_USERNAME));
    }

    @Test
    void modifyMyInfoCreatesMissingProfileRow() throws Exception {
        final UserEntity user = user(null);
        when(userService.getDtlEntity(FIXTURE_USERNAME)).thenReturn(user);
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        assertTrue(service.modifyMyInfo(request()));

        assertNotNull(user.getProfile());
        assertSame(user, user.getProfile().getUser());
        assertEquals(FIXTURE_BIRTH_DATE, user.getProfile().getBrthdy());
    }

    @Test
    void modifyMyInfoRejectsMissingAuthenticatedUser() throws Exception {
        when(userService.getDtlEntity(FIXTURE_USERNAME)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.modifyMyInfo(request()));

        verify(userRepository, never()).saveAndFlush(any());
        cacheUtils.verifyNoInteractions();
    }

    private UserEntity user(final UserProfileEntity profile) {
        final UserEntity user = UserEntity.builder()
                .id(1)
                .username(FIXTURE_USERNAME)
                .nickname("Before")
                .phoneNumber("Before")
                .profile(profile)
                .build();
        if (profile != null) profile.setUser(user);
        return user;
    }

    private UserMyUpdateRequest request() {
        final UserMyUpdateRequest request = new UserMyUpdateRequest();
        request.setNickname("  " + FIXTURE_NICKNAME + "  ");
        request.setPhoneNumber("  " + FIXTURE_PHONE_NUMBER + "  ");
        request.setBrthdy(FIXTURE_BIRTH_DATE);
        request.setLunarYn("Y");
        request.setProflCn("  " + FIXTURE_PROFILE_CONTENT + "  ");
        return request;
    }
}
