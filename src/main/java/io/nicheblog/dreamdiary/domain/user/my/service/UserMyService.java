package io.nicheblog.dreamdiary.domain.user.my.service;

import io.nicheblog.dreamdiary.domain.user.info.model.UserPwChgParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * UserMyService
 * <pre>
 *  사용자 관리 > 내 정보 관리 서비스 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface UserMyService {

    /**
     * 비밀번호 만료시 비밀번호 변경 (미로그인 상태)
     *
     * @return 비밀번호 변경 성공 여부 (boolean)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    Boolean lgnPwChg(final UserPwChgParam param) throws Exception;

    /**
     * 사용자 관리 > 내 비밀번호 확인
     *
     * @return 내 비밀번호 확인 성공 여부 (boolean)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    Boolean myPwCf(final String lgnUserId, final String currPw) throws Exception;

    /**
     * 사용자 관리 > 내 비밀번호 변경
     *
     * @return 내 비밀번호 변경 성공 여부 (boolean)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    Boolean myPwChg(final UserPwChgParam pwChgParam) throws Exception;

    /**
     * 사용자 관리 > 내 비밀번호 변경
     *
     * @return 내 비밀번호 변경 성공 여부 (boolean)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    Boolean myPwChg(final String currPw, final String newPw) throws Exception;

    /**
     * 사용자 관리 > 내 프로필 이미지 업로드
     *
     * @return 프로필 이미지 업로드 성공 여부 (boolean)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    boolean uploadProflImg(final MultipartHttpServletRequest request) throws Exception;

    /**
     * 사용자 관리 > 내 프로필 이미지 삭제
     *
     * @return 프로필 이미지 삭제 성공 여부 (boolean)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    boolean removeProflImg() throws Exception;
}