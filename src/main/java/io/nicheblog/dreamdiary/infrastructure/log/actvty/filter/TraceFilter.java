package io.nicheblog.dreamdiary.infrastructure.log.actvty.filter;

import org.slf4j.MDC;

import javax.servlet.*;
import java.io.IOException;
import java.util.UUID;

/**
 * TraceFilter
 * 모든 요청에 traceId 추가
 * 
 * @author nichefish 
 */
public class TraceFilter
        implements Filter {

    /**
     * 요청 전처리
     * @param req ServletRequest
     * @param res ServletResponse
     * @param chain FilterChain
     */
    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
        throws IOException, ServletException {

        final String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        req.setAttribute("traceId", traceId);

        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}
