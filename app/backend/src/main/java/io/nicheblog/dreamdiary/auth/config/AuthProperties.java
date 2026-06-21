package io.nicheblog.dreamdiary.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AuthProperties
 * <pre>
 *  Authentication and token settings.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "app.auth")
@Getter
@Setter
public class AuthProperties {

    /** Initial admin password used by bootstrap and manual password reset. */
    private String initialAdminPassword = "";
    /** JWT settings. */
    private Jwt jwt = new Jwt();
    /** Refresh token settings. */
    private RefreshToken refreshToken = new RefreshToken();
    /** Remember-me cookie settings. */
    private RememberMe rememberMe = new RememberMe();

    /**
     * JWT settings.
     */
    @Getter
    @Setter
    public static class Jwt {
        /** JWT signing secret. */
        private String secret;
        /** Signup token TTL in seconds. */
        private long signupTokenTtlSeconds = 3600L;
    }

    /**
     * Refresh token settings.
     */
    @Getter
    @Setter
    public static class RefreshToken {
        /** Refresh token TTL in seconds. */
        private long ttlSeconds = 1209600L;
    }

    /**
     * Remember-me cookie settings.
     */
    @Getter
    @Setter
    public static class RememberMe {
        /** Remember-me signing key. */
        private String key;
        /** Remember-me request parameter name. */
        private String param;
        /** Remember-me token TTL in days. */
        private long tokenTtlDays = 30L;
    }
}
