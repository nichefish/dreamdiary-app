package io.nicheblog.dreamdiary.auth.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * AccountDormantException
 * <pre>
 *  Spring Security:: 장기간 미로그인시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
public class AccountDormantException
        extends AuthenticationException {

    /**
     * 생성자.
     *
     * @param msg 예외 메시지
     * @param cause 이 예외의 원인
     */
    public AccountDormantException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * 생성자.
     *
     * @param msg 예외 메시지
     */
    public AccountDormantException(final String msg) {
        super(msg);
    }
}
