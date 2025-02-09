package io.nicheblog.dreamdiary.auth;

import io.nicheblog.dreamdiary.extension.notify.email.model.EmailAddress;
import io.nicheblog.dreamdiary.global.Constant;

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

    /** 수정 권한 코드 */
    String MDFABLE_CD = "MDFABLE_CD";

    /** MDFABLE */
    String MDFABLE_REGSTR = Mdfable.REGSTR.name();
    String MDFABLE_MNGR = Mdfable.MNGR.name();
    String MDFABLE_USER = Mdfable.USER.name();
    String MDFABLE_ALL = Mdfable.ALL.name();

    /** 기본 계정 정보 */
    String SYSTEM_ACNT = "system";
    String SYSTEM_ACNT_NM = "시스템관리자";
    String SYSTEM_ADMIN_NM = "dreamdiary.io";
    String SYSTEM_EMAIL = SYSTEM_ACNT + "@" + SYSTEM_ADMIN_NM;
    EmailAddress SYSTEM_EMAIL_ADDRESS = new EmailAddress(Constant.SYSTEM_EMAIL, Constant.SYSTEM_ADMIN_NM);
    String DEV_ACNT = "nichefish";
}
