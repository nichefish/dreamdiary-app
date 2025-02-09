package io.nicheblog.dreamdiary.auth.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * AlreadyAuthenticatedException
 * <pre>
 *  이미 인증된 계정을 재인증 시도시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
public class AlreadyAuthenticatedException
        extends AuthenticationException {

    /**
     * 생성자.
     *
     * @param msg 예외 메시지
     * @param cause 이 예외의 원인
     */
    public AlreadyAuthenticatedException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * 생성자.
     *
     * @param msg 예외 메시지
     */
    public AlreadyAuthenticatedException(final String msg) {
        super(msg);
    }
}
