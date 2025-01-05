package io.nicheblog.dreamdiary.domain.user.my.service.impl;

import io.nicheblog.dreamdiary.auth.entity.AuditorInfo;
import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.domain.user.info.model.UserPwChgParam;
import io.nicheblog.dreamdiary.domain.user.info.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.domain.user.info.service.UserService;
import io.nicheblog.dreamdiary.domain.user.my.service.UserMyService;
import io.nicheblog.dreamdiary.global._common.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.global._common.file.model.AtchFileDtlDto;
import io.nicheblog.dreamdiary.global._common.file.utils.FileUtils;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
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
@Service("userMyService")
@RequiredArgsConstructor
public class UserMyServiceImpl
        implements UserMyService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 만료시 비밀번호 변경 (미로그인 상태)
     *
     * @return 비밀번호 변경 성공 여부 (boolean)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    public Boolean lgnPwChg(final UserPwChgParam param) throws Exception {
        final String userId = param.getUserId();
        final String currPw = param.getCurrPw();
        final String newPw = param.getNewPw();

        final UserEntity retrievedEntity = userService.getDtlEntity(userId);
        if (retrievedEntity == null) return false;

        // password 일치여부 체크
        if (!passwordEncoder.matches(currPw, retrievedEntity.getPassword())) {
            throw new BadCredentialsException(MessageUtils.PW_MISMATCH);
        }
        retrievedEntity.setPassword(passwordEncoder.encode(newPw));
        retrievedEntity.acntStus.setNeedsPwReset("N");
        retrievedEntity.acntStus.setPwChgDt(DateUtils.getCurrDate());
        final UserEntity modified = userRepository.saveAndFlush(retrievedEntity);

        return modified.getUserNo() != null;
    }

    /**
     * 사용자 관리 > 내 비밀번호 확인
     *
     * @return 내 비밀번호 확인 성공 여부 (boolean)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public Boolean myPwCf(final String lgnUserId, final String currPw) throws Exception {
        // Entity 레벨 조회
        final UserEntity retrievedEntity = userService.getDtlEntity(lgnUserId);
        if (retrievedEntity == null) return false;

        // 1. 내 비밀번호가 맞는지부터 확인
        if (!passwordEncoder.matches(currPw, retrievedEntity.getPassword())) {
            throw new BadCredentialsException(MessageUtils.PW_MISMATCH);
        }

        return true;
    }

    @Override
    @Transactional
    public Boolean myPwChg(final UserPwChgParam pwChgParam) throws Exception {
        return this.myPwChg(pwChgParam.getCurrPw(), pwChgParam.getNewPw());
    }

    /**
     * 사용자 관리 > 내 비밀번호 변경
     *
     * @return 내 비밀번호 변경 성공 여부 (boolean)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    public Boolean myPwChg(final String currPw, final String newPw) throws Exception {
        final String lgnUserId = AuthUtils.getLgnUserId();

        // Entity 레벨 조회
        final UserEntity retrievedEntity = userService.getDtlEntity(lgnUserId);
        if (retrievedEntity == null) return false;

        // 1. 내 비밀번호가 맞는지부터 확인
        if (!passwordEncoder.matches(currPw, retrievedEntity.getPassword())) {
            throw new BadCredentialsException(MessageUtils.PW_MISMATCH);
        }
        // 2. 맞으면 비밀번호 업데이트
        retrievedEntity.setPassword(passwordEncoder.encode(newPw));
        retrievedEntity.acntStus.setNeedsPwReset("N");
        retrievedEntity.acntStus.setPwChgDt(DateUtils.getCurrDate());
        final UserEntity modified = userRepository.saveAndFlush(retrievedEntity);

        return modified.getUserNo() != null;
    }

    /**
     * 사용자 관리 > 내 프로필 이미지 업로드
     *
     * @return 프로필 이미지 업로드 성공 여부 (boolean)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    public boolean uploadProflImg(final MultipartHttpServletRequest request) throws Exception {
        // 파일 영역 처리 후 업로드 정보 받아서 반환
        final AtchFileDtlDto uploaded = FileUtils.uploadDtlFile(request);
        if (uploaded == null) return false;

        // 프로필 url 업데이트
        final String url = uploaded.getUrl();
        final String lgnUserId = AuthUtils.getLgnUserId();
        final UserEntity retrievedEntity = userService.getDtlEntity(lgnUserId);
        retrievedEntity.setProflImgUrl(url);
        final UserEntity modified = userRepository.saveAndFlush(retrievedEntity);

        // 관련 캐시 삭제
        EhCacheUtils.clearL2Cache(AuditorInfo.class);

        return modified.getUserNo() != null;
    }

    /**
     * 사용자 관리 > 내 프로필 이미지 삭제
     *
     * @return 프로필 이미지 삭제 성공 여부 (boolean)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    public boolean removeProflImg() throws Exception {
        // 프로필 url 삭제
        final String lgnUserId = AuthUtils.getLgnUserId();
        final UserEntity retrievedEntity = userService.getDtlEntity(lgnUserId);
        retrievedEntity.setProflImgUrl(null);
        final UserEntity updatedEntity = userRepository.saveAndFlush(retrievedEntity);

        // 관련 캐시 삭제
        EhCacheUtils.clearL2Cache(AuditorInfo.class);

        return updatedEntity.getUserNo() != null;
    }
}