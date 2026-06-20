package io.nicheblog.dreamdiary.auth.security.filter;

import io.nicheblog.dreamdiary.auth.security.service.AuthSessionPolicyService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AuthSessionTimeoutFilter
 * <pre>
 *  Applies the runtime authentication timeout policy to already-created sessions.
 * </pre>
 *
 * <p>The filter never creates a new session. It only refreshes the max inactive
 * interval of an existing session so auth policy changes take effect without an
 * application restart on the next request.</p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class AuthSessionTimeoutFilter
        extends OncePerRequestFilter {

    private final AuthSessionPolicyService authSessionPolicyService;

    @Override
    protected void doFilterInternal(
            final @NotNull HttpServletRequest request,
            final @NotNull HttpServletResponse response,
            final @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        final HttpSession session = request.getSession(false);
        if (session != null) authSessionPolicyService.applyToSession(session);

        filterChain.doFilter(request, response);
    }
}
