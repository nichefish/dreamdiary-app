package io.nicheblog.dreamdiary.auth.service;

import io.nicheblog.dreamdiary.auth.entity.AuthRoleEntity;
import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * AuthService
 * <pre>
 *  Spring Security:: 인증 및 권한 처리 관련 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface AuthService
        extends UserDetailsService {

    /**
     * userId로 계정 + 사용자 정보 조회
     * 로그인 등 인증시 Spring Security에서 사용.
     *
     * @param userId 조회할 사용자의 ID
     * @return {@link AuthInfo} -- Spring Security용 사용자 인증정보 객체
     * @throws UsernameNotFoundException 사용자 정보를 찾을 수 없는 경우
     */
    AuthInfo loadUserByUsername(final String userId) throws UsernameNotFoundException;

    /**
     * 로그인 실패시 실패 카운트를 증가시킨다.
     *
     * @param userId 로그인 실패한 사용자 ID
     * @return {@link Integer} -- 업데이트된 로그인 실패 횟수
     */
    Integer applyLgnFailCnt(final String userId);

    /**
     * 계정 잠금 처리
     *
     * @param userId 계정을 잠글 사용자 ID
     */
    void lockAccount(final String userId);

    /**
     * 로그인 성공시 최종 로그인일자 세팅 및 실패 카운트 초기화
     *
     * @param userId 처리할 사용자 ID
     */
    void setLstLgnDt(final String userId);

    /**
     * 권한 정보 조회
     * TODO: 사이트 커지면 역할 분리해야 함
     *
     * @param authCd 조회할 권한 코드
     * @return {@link AuthRoleEntity} -- 권한 정보 객체
     */
    AuthRoleEntity getAuthRole(final String authCd);
}