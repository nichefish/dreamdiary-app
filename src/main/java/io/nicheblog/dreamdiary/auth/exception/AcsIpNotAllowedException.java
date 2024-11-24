package io.nicheblog.dreamdiary.auth.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * AcsIpNotAllowedException
 * <pre>
 *  Spring Security:: 접속IP 불일치시 던지는 Custom Exception
 * </pre>
 */
public class AcsIpNotAllowedException
        extends AuthenticationException {

    /**
     * 생성자.
     *
     * @param msg 예외 메시지
     * @param cause 이 예외의 원인
     */
    public AcsIpNotAllowedException(
            final String msg,
            final Throwable cause
    ) {
        super(msg, cause);
    }

    /**
     * 생성자.
     *
     * @param msg 예외 메시지
     */
    public AcsIpNotAllowedException(final String msg) {
        super(msg);
    }
}
