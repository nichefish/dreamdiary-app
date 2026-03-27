package io.nicheblog.dreamdiary.auth.security.exception;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

/**
 * AccountDormantException
 * <pre>
 *  Spring Security:: 장기간 미로그인시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class AccountDormantException
        extends AuthenticationException {
}
