package io.nicheblog.dreamdiary.feature.user.my.service;

import io.nicheblog.dreamdiary.auth.policy.entity.AuthPolicyEntity;
import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyQueryService;
import io.nicheblog.dreamdiary.auth.jwt.service.RefreshTokenService;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.file.model.FileRecordDto;
import io.nicheblog.dreamdiary.feature.file.utils.FileUtils;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.model.UserPwChgParam;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.feature.user.account.service.UserPasswordHistoryService;
import io.nicheblog.dreamdiary.feature.user.account.service.UserService;
import io.nicheblog.dreamdiary.feature.user.my.model.UserMyUpdateRequest;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.persistence.EntityNotFoundException;

/**
 * UserMyService
 * <pre>
 *  사용자 관리 > 내 정보 관리 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class UserMyService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuthPolicyQueryService authPolicyQueryService;
    private final UserPasswordHistoryService userPasswordHistoryService;

    /**
     * 로그인 사용자의 개인 연락처·프로필 수정.
     * 요청에서 사용자 식별자를 받지 않고 인증 컨텍스트의 계정만 변경한다.
     *
     * @param request 수정할 개인 프로필 정보
     * @return 수정 성공 여부
     */
    @Transactional
    public boolean modifyMyInfo(final UserMyUpdateRequest request) throws Exception {
        final String loginUsername = AuthUtils.getLoginUsername();
        final UserEntity user = userService.getDtlEntity(loginUsername);
        if (user == null) {
            log.warn("My profile update rejected: authenticated user not found. username={}", loginUsername);
            throw new EntityNotFoundException();
        }

        user.setNickname(StringUtils.trim(request.getNickname()));
        user.setPhoneNumber(StringUtils.trimToNull(request.getPhoneNumber()));

        boolean profileCreated = false;
        UserProfileEntity profile = user.getProfile();
        if (profile == null) {
            profile = UserProfileEntity.builder()
                    .user(user)
                    .build();
            user.setProfile(profile);
            profileCreated = true;
        }
        profile.setBrthdy(request.getBrthdy());
        profile.setLunarYn(request.getLunarYn());
        profile.setProflCn(StringUtils.trimToNull(request.getProflCn()));

        final UserEntity updated = userRepository.saveAndFlush(user);
        EhCacheUtils.evictCacheByKey("auditorInfo", loginUsername);
        log.info("My profile updated. username={} profileCreated={}", loginUsername, profileCreated);

        return updated.getId() != null;
    }

    /**
     * 비밀번호 만료시 비밀번호 변경 (미로그인 상태)
     *
     * @param param UserPwChgParam
     * @return 비밀번호 변경 성공 여부 (boolean)
     */
    @Transactional
    public Boolean loginPwChg(final UserPwChgParam param) throws Exception {
        final String username = param.getUsername();
        final String currPw = param.getCurrPw();
        final String newPw = param.getNewPw();

        final UserEntity retrievedEntity = userService.getDtlEntity(username);
        if (retrievedEntity == null) return false;

        // password 일치여부 체크
        if (!passwordEncoder.matches(currPw, retrievedEntity.getPassword())) {
            throw new BadCredentialsException(MessageUtils.getMessage("user.pw.mismatch"));
        }
        this.validatePasswordResetTokenIfNeeded(retrievedEntity, param.getPasswordToken());
        userPasswordHistoryService.validateReusablePassword(retrievedEntity, newPw);
        final String previousPasswordHash = retrievedEntity.getPassword();
        retrievedEntity.setPassword(passwordEncoder.encode(newPw));
        retrievedEntity.acntStus.setNeedsPasswordReset("N");
        retrievedEntity.acntStus.setPasswordResetTokenHash(null);
        retrievedEntity.acntStus.setPasswordResetTokenIssuedAt(null);
        retrievedEntity.acntStus.setPasswordChangedAt(DateUtils.getCurrLocalDateTime());
        final UserEntity modified = userRepository.saveAndFlush(retrievedEntity);
        userPasswordHistoryService.recordPasswordChange(modified, previousPasswordHash);
        refreshTokenService.revoke(username);

        return modified.getId() != null;
    }

    /**
     * 사용자 관리 > 내 비밀번호 확인
     *
     * @param loginUsername String
     * @param currPw String
     * @return 내 비밀번호 확인 성공 여부 (boolean)
     */
    public Boolean myPwCf(final String loginUsername, final String currPw) throws Exception {
        // Entity 레벨 조회
        final UserEntity retrievedEntity = userService.getDtlEntity(loginUsername);
        if (retrievedEntity == null) return false;

        // 1. 내 비밀번호가 맞는지부터 확인
        if (!passwordEncoder.matches(currPw, retrievedEntity.getPassword())) {
            throw new BadCredentialsException(MessageUtils.getMessage("user.pw.mismatch"));
        }

        return true;
    }

    /**
     * 사용자 관리 > 내 비밀번호 변경
     *
     * @param pwChgParam UserPwChgParam
     * @return 내 비밀번호 변경 성공 여부 (boolean)
     */
    @Transactional
    public Boolean myPwChg(final UserPwChgParam pwChgParam) throws Exception {
        return this.myPwChg(pwChgParam.getCurrPw(), pwChgParam.getNewPw());
    }

    /**
     * 사용자 관리 > 내 비밀번호 변경
     *
     * @param currPw String
     * @param newPw String
     * @return 내 비밀번호 변경 성공 여부 (boolean)
     */
    @Transactional
    public Boolean myPwChg(final String currPw, final String newPw) throws Exception {
        final String loginUsername = AuthUtils.getLoginUsername();

        // Entity 레벨 조회
        final UserEntity retrievedEntity = userService.getDtlEntity(loginUsername);
        if (retrievedEntity == null) return false;

        // 1. 내 비밀번호가 맞는지부터 확인
        if (!passwordEncoder.matches(currPw, retrievedEntity.getPassword())) {
            throw new BadCredentialsException(MessageUtils.getMessage("user.pw.mismatch"));
        }
        // 2. 맞으면 비밀번호 업데이트
        userPasswordHistoryService.validateReusablePassword(retrievedEntity, newPw);
        final String previousPasswordHash = retrievedEntity.getPassword();
        retrievedEntity.setPassword(passwordEncoder.encode(newPw));
        retrievedEntity.acntStus.setNeedsPasswordReset("N");
        retrievedEntity.acntStus.setPasswordResetTokenHash(null);
        retrievedEntity.acntStus.setPasswordResetTokenIssuedAt(null);
        retrievedEntity.acntStus.setPasswordChangedAt(DateUtils.getCurrLocalDateTime());
        final UserEntity modified = userRepository.saveAndFlush(retrievedEntity);
        userPasswordHistoryService.recordPasswordChange(modified, previousPasswordHash);
        refreshTokenService.revoke(loginUsername);

        return modified.getId() != null;
    }

    /**
     * 사용자 관리 > 내 프로필 이미지 업로드
     * @param request MultipartHttpServletRequest
     *
     * @return 프로필 이미지 업로드 성공 여부 (boolean)
     */
    @Transactional
    public boolean uploadProflImg(final MultipartHttpServletRequest request) throws Exception {
        // 파일 영역 처리 후 업로드 정보 받아서 반환
        final FileRecordDto uploaded = FileUtils.uploadDtlFile(request);
        if (uploaded == null) return false;

        // 프로필 url 업데이트
        final String url = uploaded.getUrl();
        final String loginUsername = AuthUtils.getLoginUsername();
        final UserEntity retrievedEntity = userService.getDtlEntity(loginUsername);
        retrievedEntity.setProfileImageUrl(url);
        final UserEntity modified = userRepository.saveAndFlush(retrievedEntity);

        // 관련 캐시 삭제
        EhCacheUtils.evictCacheByKey("auditorInfo", loginUsername);

        return modified.getId() != null;
    }

    /**
     * 사용자 관리 > 내 프로필 이미지 삭제
     *
     * @return 프로필 이미지 삭제 성공 여부 (boolean)
     */
    @Transactional
    public boolean removeProflImg() throws Exception {
        // 프로필 url 삭제
        final String loginUsername = AuthUtils.getLoginUsername();
        final UserEntity retrievedEntity = userService.getDtlEntity(loginUsername);
        retrievedEntity.setProfileImageUrl(null);
        final UserEntity updatedEntity = userRepository.saveAndFlush(retrievedEntity);

        // 관련 캐시 삭제
        EhCacheUtils.evictCacheByKey("auditorInfo", loginUsername);

        return updatedEntity.getId() != null;
    }

    private void validatePasswordResetTokenIfNeeded(final UserEntity user, final String passwordToken) throws Exception {
        if (user == null || user.acntStus == null) return;
        if (!"Y".equals(user.acntStus.getNeedsPasswordReset())) return;

        if (StringUtils.isBlank(passwordToken) || StringUtils.isBlank(user.acntStus.getPasswordResetTokenHash())) {
            throw new BadCredentialsException(MessageUtils.getMessage("user.pw.mismatch"));
        }
        if (!this.isPasswordResetTokenWindowValid(user.acntStus.getPasswordResetTokenIssuedAt())) {
            throw new CredentialsExpiredException("AbstractUserDetailsAuthenticationProvider.CredentialsExpiredException");
        }

        final String hashed = userService.hashPasswordResetToken(passwordToken);
        if (!MessageDigest.isEqual(
                hashed.getBytes(StandardCharsets.UTF_8),
                user.acntStus.getPasswordResetTokenHash().getBytes(StandardCharsets.UTF_8)
        )) {
            throw new BadCredentialsException(MessageUtils.getMessage("user.pw.mismatch"));
        }
    }

    private boolean isPasswordResetTokenWindowValid(final LocalDateTime issuedAt) throws Exception {
        if (issuedAt == null) return false;

        final AuthPolicyEntity authPolicy = authPolicyQueryService.getDtlEntity();
        final Integer expiryMinutes = (authPolicy == null || authPolicy.getPasswordResetTokenExpiryMinutes() == null)
                ? 30
                : authPolicy.getPasswordResetTokenExpiryMinutes();
        final LocalDateTime expiresAt = issuedAt.plusMinutes(expiryMinutes.longValue());
        return expiresAt.isAfter(DateUtils.getCurrLocalDateTime());
    }
}
