package io.nicheblog.dreamdiary.auth.security.exception;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

/**
 * AlreadyAuthenticatedException
 * <pre>
 *  이미 인증된 계정을 재인증 시도시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class AlreadyAuthenticatedException
        extends AuthenticationException {
}
