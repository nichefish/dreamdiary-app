package io.nicheblog.dreamdiary.auth.security.exception;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

/**
 * DupIdLoginException
 * <pre>
 *  Spring Security:: 중복 로그인시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class DupIdLoginException
        extends AuthenticationException {
}
