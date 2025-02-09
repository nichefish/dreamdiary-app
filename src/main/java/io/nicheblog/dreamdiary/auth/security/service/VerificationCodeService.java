package io.nicheblog.dreamdiary.auth.security.service;

/**
 * VerificationCodeService
 * <pre>
 *  인증 코드 관리 서비스.
 * </pre>
 *
 * @author nichefish
 */
public interface VerificationCodeService {
    void setVerificationCode(final String email, final String code);

    String getVerificationCode(final String email);

    void deleteVerificationCode(final String email);

    String getRedisKey(final String email);
}
