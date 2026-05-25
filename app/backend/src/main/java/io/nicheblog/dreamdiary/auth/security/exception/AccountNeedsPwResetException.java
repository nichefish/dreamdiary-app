package io.nicheblog.dreamdiary.auth.security.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

/**
 * AccountNeedsPwResetException
 * <pre>
 *  Spring Security:: 패스워드 리셋 강제시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@Getter
public class AccountNeedsPwResetException
        extends AuthenticationException {

    private final String passwordToken;

    public AccountNeedsPwResetException(final String msg, final String passwordToken) {
        super(msg);
        this.passwordToken = passwordToken;
    }

    public AccountNeedsPwResetException(final String msg, final Throwable cause) {
        super(msg, cause);
        this.passwordToken = null;
    }
}
