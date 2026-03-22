package io.nicheblog.dreamdiary.auth;

/**
 * AuthConstant
 * <pre>
 *  권한 관련 코드성 데이터 정의
 * </pre>
 *
 * @author nichefish
 */
public interface AuthConstant {

    /** 사용자 권한 코드 */
    String AUTH_CD = "AUTH_CD";

    /** AUTH */
    String AUTH_MNGR = Auth.MNGR.name();
    String AUTH_USER = Auth.USER.name();
    String AUTH_DEV = Auth.DEV.name();

    String ROLE_MNGR = "ROLE_MNGR";
    String ROLE_USER = "ROLE_USER";
    String ROLE_DEV = "ROLE_DEV";
}
