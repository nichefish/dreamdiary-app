package io.nicheblog.dreamdiary.auth.security.exception;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

/**
 * IpNotAllowedException
 * <pre>
 *  Spring Security:: 접속IP 불일치시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class IpNotAllowedException
        extends AuthenticationException {
}
