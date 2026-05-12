package io.nicheblog.dreamdiary.auth.security.exception;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

/**
 * AccountNeedsPwResetException
 * <pre>
 *  Spring Security:: 패스워드 리셋 강제시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class AccountNeedsPwResetException
        extends AuthenticationException {
}
