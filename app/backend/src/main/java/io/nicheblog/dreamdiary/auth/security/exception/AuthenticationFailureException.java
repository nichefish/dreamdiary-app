package io.nicheblog.dreamdiary.auth.security.exception;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

/**
 * AuthenticationFailureException
 * <pre>
 *  인증authentication 실패시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class AuthenticationFailureException
        extends AuthenticationException {
}
