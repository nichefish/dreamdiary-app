package io.nicheblog.dreamdiary.infrastructure.messaging.jandi.exception;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

/**
 * JandiException
 * <pre>
 *  잔디 프로퍼티 세팅 오류시 던지는 Custom Exception.
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class JandiException
        extends AuthenticationException {
}
