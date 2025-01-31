package io.nicheblog.dreamdiary.auth.filter;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CorsFilter
 * <pre>
 *  API 호출시 CORS(Cross-Origin-Resource-Sharing) 에러 발생 조치 위해 Response Header 핸들링
 * </pre>
 *
 * @author nichefish
 */
@Component
public class CorsFilter
        implements Filter {

    /**
     * CORS(Cross-Origin Resource Sharing) 관련 HTTP 응답 헤더 설정.
     */
    @Override
    public void doFilter(
            final ServletRequest req,
            final ServletResponse res,
            final FilterChain chain
    ) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT, PATCH");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");
        chain.doFilter(request, response);
    }
}