package io.nicheblog.dreamdiary.auth.security.service;

import io.nicheblog.dreamdiary.auth.policy.model.AuthPolicyQueryDto;
import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.time.Duration;

/**
 * AuthSessionPolicyService
 * <pre>
 *  Resolves the user-facing authentication session timeout policy.
 * </pre>
 *
 * <p>{@code auth_policy.session_timeout_minutes} is the primary runtime policy.
 * If the policy row is not ready yet, the existing Spring session timeout
 * setting remains the fallback contract.</p>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AuthSessionPolicyService {

    private static final int MIN_SESSION_TIMEOUT_MINUTES = 1;

    private final AuthPolicyQueryService authPolicyQueryService;

    @Value("${server.servlet.session.timeout:1h}")
    private Duration fallbackSessionTimeout;

    /**
     * Returns the effective session timeout in seconds.
     *
     * @return effective session timeout seconds
     */
    public long getSessionTimeoutSeconds() {
        final Integer policyMinutes = this.getPolicySessionTimeoutMinutes();
        if (policyMinutes != null && policyMinutes >= MIN_SESSION_TIMEOUT_MINUTES) {
            return Duration.ofMinutes(policyMinutes).getSeconds();
        }
        return fallbackSessionTimeout.getSeconds();
    }

    /**
     * Returns the effective session timeout in {@code int} seconds for Servlet and cookie APIs.
     *
     * @return effective session timeout seconds
     */
    public int getSessionTimeoutSecondsAsInt() {
        final long seconds = this.getSessionTimeoutSeconds();
        if (seconds > Integer.MAX_VALUE) {
            log.warn("Auth session timeout exceeds integer range. seconds={} fallback={}", seconds, Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }
        return (int) seconds;
    }

    /**
     * Applies the effective timeout to a servlet session.
     *
     * @param session target session
     */
    public void applyToSession(final HttpSession session) {
        if (session == null) return;
        session.setMaxInactiveInterval(this.getSessionTimeoutSecondsAsInt());
    }

    private Integer getPolicySessionTimeoutMinutes() {
        try {
            final AuthPolicyQueryDto policy = authPolicyQueryService.getDtlDto();
            return policy == null ? null : policy.getSessionTimeoutMinutes();
        } catch (final Exception e) {
            log.warn("Failed to read auth session timeout policy. fallback={}", fallbackSessionTimeout, e);
            return null;
        }
    }
}
