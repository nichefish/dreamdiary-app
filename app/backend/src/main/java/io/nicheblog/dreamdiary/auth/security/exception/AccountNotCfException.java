package io.nicheblog.dreamdiary.auth.security.exception;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

/**
 * AccountNotCfException
 * <pre>
 *  Spring Security:: 계정 미승인시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class AccountNotCfException
        extends AuthenticationException {
}
