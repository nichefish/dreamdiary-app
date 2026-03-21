package io.nicheblog.dreamdiary.infrastructure.messaging.jandi.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * JandiException
 * <pre>
 *  잔디 프로퍼티 세팅 오류시 던지는 Custom Exception.
 * </pre>
 *
 * @author nichefish
 */
public class JandiException
        extends AuthenticationException {

    public JandiException(
            final String msg,
            final Throwable cause
    ) {
        super(msg, cause);
    }

    public JandiException(final String msg) {
        super(msg);
    }
}
