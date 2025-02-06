package io.nicheblog.dreamdiary.auth.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * NotAuthorizedException
 * <pre>
 *  자원에 할당된 인가authorization 정보 부재시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
public class NotAuthorizedException
        extends AuthenticationException {

    /**
     * 생성자.
     *
     * @param msg 예외 메시지
     * @param cause 이 예외의 원인
     */
    public NotAuthorizedException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * 생성자.
     *
     * @param msg 예외 메시지
     */
    public NotAuthorizedException(final String msg) {
        super(msg);
    }
}
