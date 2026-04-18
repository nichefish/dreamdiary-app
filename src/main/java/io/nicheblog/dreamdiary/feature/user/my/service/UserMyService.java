package io.nicheblog.dreamdiary.feature.user.my.service;

import io.nicheblog.dreamdiary.auth.jwt.service.RefreshTokenService;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.file.model.FileRecordDto;
import io.nicheblog.dreamdiary.feature.file.utils.FileUtils;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.model.UserPwChgParam;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.feature.user.account.service.UserService;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
public class UserMyService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

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
            throw new BadCredentialsException(MessageUtils.PW_MISMATCH);
        }
        retrievedEntity.setPassword(passwordEncoder.encode(newPw));
        retrievedEntity.acntStus.setNeedsPasswordReset("N");
        retrievedEntity.acntStus.setPasswordResetTokenIssuedAt(null);
        retrievedEntity.acntStus.setPasswordChangedAt(DateUtils.getCurrDate());
        final UserEntity modified = userRepository.saveAndFlush(retrievedEntity);
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
            throw new BadCredentialsException(MessageUtils.PW_MISMATCH);
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
            throw new BadCredentialsException(MessageUtils.PW_MISMATCH);
        }
        // 2. 맞으면 비밀번호 업데이트
        retrievedEntity.setPassword(passwordEncoder.encode(newPw));
        retrievedEntity.acntStus.setNeedsPasswordReset("N");
        retrievedEntity.acntStus.setPasswordResetTokenIssuedAt(null);
        retrievedEntity.acntStus.setPasswordChangedAt(DateUtils.getCurrDate());
        final UserEntity modified = userRepository.saveAndFlush(retrievedEntity);
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
}

