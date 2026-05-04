package io.nicheblog.dreamdiary.auth.security.service;

import io.nicheblog.dreamdiary.auth.policy.entity.AuthPolicyEntity;
import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyQueryService;
import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.auth.security.entity.RoleEntity;
import io.nicheblog.dreamdiary.auth.security.mapstruct.AuthInfoMapstruct;
import io.nicheblog.dreamdiary.auth.security.mapstruct.RoleMapstruct;
import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.model.RoleDto;
import io.nicheblog.dreamdiary.auth.security.repository.jpa.RoleRepository;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntity;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * AuthService
 * <pre>
 *  Spring Security:: 인증 및 권한 처리 관련 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService
        implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthPolicyQueryService authPolicyQueryService;
    private final AuthInfoMapstruct authInfoMapstruct;

    /**
     * username으로 계정 + 사용자 정보 조회
     * 로그인 등 인증시 Spring Security에서 사용.
     *
     * @param username 조회할 사용자의 계정명
     * @return {@link AuthInfo} -- Spring Security용 사용자 인증정보 객체
     * @throws UsernameNotFoundException 사용자 정보를 찾을 수 없는 경우
     */
    @Override
    @SneakyThrows
    @Transactional(readOnly = true)
    public AuthInfo loadUserByUsername(final String username) throws UsernameNotFoundException {
        final Optional<UserEntity> rsWrapper = userRepository.findByUsername(username);
        if (rsWrapper.isEmpty()) throw new UsernameNotFoundException("exception.UsernameNotFoundException");
        final UserEntity rsUser = rsWrapper.get();

        // TODO: 사용자 프로필 정보 존재여부 체크
        // Integer userProflNo = rsUserEntity.getUserProflNo();
        // if (userProflNo != null) {
        //     UserProflEntity rsUserInfo = userProflRepository.findById(userProflNo).orElse(null);
        //     rsUserEntity.setUserProfl(rsUserInfo);
        // }

        final AuthInfo authInfo = authInfoMapstruct.toDto(rsUser);
        this.fillRolesFromUserByRoleIdIfMissing(rsUser, authInfo);
        return authInfo;
    }

    /**
     * OAuth2AuthenticationToken을 사용하여 사용자 정보를 로드합니다.
     *
     * @param email OAuth2AuthenticationToken 으로부터 추출한 사용자 이메일
     * @return {@link AuthInfo}
     */
    public AuthInfo loadUserByEmail(final String email) throws Exception {
        final Optional<UserEntity> rsWrapper = userRepository.findByEmail(email);
        if (rsWrapper.isEmpty()) throw new UsernameNotFoundException("exception.UsernameNotFoundException");
        final UserEntity rsUser = rsWrapper.get();

        final AuthInfo authInfo = authInfoMapstruct.toDto(rsUser);
        this.fillRolesFromUserByRoleIdIfMissing(rsUser, authInfo);
        return authInfo;
    }

    /**
     * user_role.role_id 만 있고 연관 role 이 로드되지 않은 경우 보강.
     */
    private void fillRolesFromUserByRoleIdIfMissing(final UserEntity user, final AuthInfo authInfo) throws Exception {
        if (!CollectionUtils.isEmpty(authInfo.getRoles())) {
            return;
        }
        if (CollectionUtils.isEmpty(user.getUserRoles())) {
            return;
        }
        final List<RoleDto> roles = new ArrayList<>();
        for (final UserRoleEntity ur : user.getUserRoles()) {
            if (ur.getRoleInfo() != null) {
                roles.add(RoleMapstruct.INSTANCE.toDto(ur.getRoleInfo()));
                continue;
            }
            if (ur.getRoleId() != null) {
                final Optional<RoleEntity> roleOpt = roleRepository.findById(ur.getRoleId());
                if (roleOpt.isPresent()) {
                    roles.add(RoleMapstruct.INSTANCE.toDto(roleOpt.get()));
                }
            }
        }
        authInfo.setRoles(roles);
    }

    /**
     * 로그인 실패시 실패 카운트를 증가시킨다.
     *
     * @param username 로그인 실패한 사용자 계정명
     * @return {@link Integer} -- 업데이트된 로그인 실패 횟수
     */
    @Transactional
    public Integer applyLoginFailCnt(final String username) {
        // ID로 사용자 정보 조회
        final Optional<UserEntity> userEntityWrapper = userRepository.findByUsername(username);
        if (userEntityWrapper.isEmpty()) return 0;
        final UserEntity userEntity = userEntityWrapper.get();
        Integer loginAttemptWindowMinutes = 10;
        try {
            final AuthPolicyEntity authPolicy = authPolicyQueryService.getDtlEntity();
            if (authPolicy != null && authPolicy.getLoginAttemptWindowMinutes() != null) {
                loginAttemptWindowMinutes = authPolicy.getLoginAttemptWindowMinutes();
            }
        } catch (final Exception ignore) {
            // 정책 조회 실패시 기본값 사용
        }
        final Date now = new Date();
        final Date windowStart = userEntity.acntStus.getLoginFailWindowStartedAt();

        final long windowMillis = (loginAttemptWindowMinutes == null ? 0L : TimeUnit.MINUTES.toMillis(loginAttemptWindowMinutes));
        final boolean shouldResetWindow = (windowStart == null) || (windowMillis > 0 && (now.getTime() - windowStart.getTime()) >= windowMillis);

        if (shouldResetWindow) {
            userEntity.acntStus.setLoginFailCnt(0);
            userEntity.acntStus.setLoginFailWindowStartedAt(now);
        }

        // 로그인 실패횟수 조회해서 세팅
        final Integer currLoginFailCnt = userEntity.acntStus.getLoginFailCnt();
        final Integer newLoginFailCnt = (currLoginFailCnt == null) ? 1 : currLoginFailCnt + 1;
        userEntity.acntStus.setLoginFailCnt(newLoginFailCnt);
        // 저장 후 반환된 값 반환
        final UserEntity rsltEntity = userRepository.save(userEntity);
        return rsltEntity.acntStus.getLoginFailCnt();
    }

    /**
     * 계정 잠금 처리
     *
     * @param username 계정을 잠글 사용자 계정명
     */
    @Transactional
    public void lockAccount(final String username) {
        // ID로 사용자 정보 조회
        final Optional<UserEntity> userEntityWrapper = userRepository.findByUsername(username);
        final UserEntity userEntity = userEntityWrapper.orElseThrow(NullPointerException::new);
        Integer accountLockDurationMinutes = 30;
        try {
            final AuthPolicyEntity authPolicy = authPolicyQueryService.getDtlEntity();
            if (authPolicy != null && authPolicy.getAccountLockDurationMinutes() != null) {
                accountLockDurationMinutes = authPolicy.getAccountLockDurationMinutes();
            }
        } catch (final Exception ignore) {
            // 정책 조회 실패시 기본값 사용
        }
        final Date now = new Date();
        final Date lockExpiresAt = (accountLockDurationMinutes == null)
                ? null
                : new Date(now.getTime() + TimeUnit.MINUTES.toMillis(accountLockDurationMinutes));

        // 계정 잠금 처리
        userEntity.acntStus.setLockedYn("Y");
        userEntity.acntStus.setLoginFailCnt(0);
        userEntity.acntStus.setLoginFailWindowStartedAt(null);
        userEntity.acntStus.setLockExpiresAt(lockExpiresAt);
        userRepository.save(userEntity);
    }

    /**
     * 로그인 성공시 최종 로그인일자 세팅 및 실패 카운트 초기화
     *
     * @param username 처리할 사용자 계정명
     */
    @Transactional
    public void setLastLoginAt(final String username) {
        // ID로 사용자 정보 조회
        final Optional<UserEntity> userEntityWrapper = userRepository.findByUsername(username);
        final UserEntity userEntity = userEntityWrapper.orElseThrow(NullPointerException::new);
        // 최종 로그인 날짜 세팅 및 실패 카운터 0으로 세팅
        userEntity.acntStus.setLastLoginAt(new Date());
        userEntity.acntStus.setLoginFailCnt(0);
        userEntity.acntStus.setLoginFailWindowStartedAt(null);
        userEntity.acntStus.setLockedYn("N");
        userEntity.acntStus.setLockExpiresAt(null);
        userRepository.save(userEntity);
    }

    /**
     * 권한 정보 조회
     * TODO: 사이트 커지면 역할 분리해야 함
     *
     * @param roleKey 조회할 역할 키 (비즈니스 키)
     * @return {@link RoleEntity} -- 권한 정보 객체
     */
    public RoleEntity getRole(final String roleKey) {
        return roleRepository.findByRoleKey(roleKey);
    }

    /**
     * getAuditorInfo
     *
     * @param username 사용자 계정명
     * @return AuditorInfo
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "auditorInfo", key = "#username", condition = "#username!=null")
    public AuditorInfo getAuditorInfo(final String username) {
        final Optional<UserEntity> userEntityWrapper = userRepository.findByUsername(username);
        if (userEntityWrapper.isEmpty()) return null;

        final UserEntity userEntity = userEntityWrapper.get();
        return authInfoMapstruct.toAuditorInfo(userEntity);
    }
}

