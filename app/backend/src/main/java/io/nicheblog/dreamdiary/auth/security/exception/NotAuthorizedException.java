package io.nicheblog.dreamdiary.auth.security.exception;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

/**
 * NotAuthorizedException
 * <pre>
 *  자원에 할당된 인가authorization 정보 부재시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class NotAuthorizedException
        extends AuthenticationException {
}
