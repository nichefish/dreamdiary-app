package io.nicheblog.dreamdiary.auth.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * DupIdLgnException
 * <pre>
 *  Spring Security:: 중복 로그인시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
public class DupIdLgnException
        extends AuthenticationException {

    /**
     * 생성자.
     * @param msg 예외 메시지
     * @param cause 이 예외의 원인
     */
    public DupIdLgnException(
            final String msg,
            final Throwable cause
    ) {
        super(msg, cause);
    }

    /**
     * 생성자.
     * @param msg 예외 메시지
     */
    public DupIdLgnException(final String msg) {
        super(msg);
    }
}
