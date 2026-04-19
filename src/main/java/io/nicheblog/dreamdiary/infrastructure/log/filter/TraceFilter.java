package io.nicheblog.dreamdiary.infrastructure.log.filter;

import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 모든 요청에 traceId 추가.
 */
public class TraceFilter
        implements Filter {

    private static final String TRACE_ID = "traceId";
    private static final String TRACE_HEADER = "X-Trace-Id";
    /** MDC: HTTP 메서드 (요청 단위, 로그 패턴에서 한 줄에 맥락 부여) */
    public static final String MDC_REQ_METHOD = "reqMethod";
    /** MDC: 요청 URI (동일) */
    public static final String MDC_REQ_URI = "reqUri";

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
        throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;
        final String traceId = resolveTraceId(request);

        MDC.put(TRACE_ID, traceId);
        MDC.put(MDC_REQ_METHOD, request.getMethod());
        MDC.put(MDC_REQ_URI, request.getRequestURI());
        request.setAttribute(TRACE_ID, traceId);
        response.setHeader(TRACE_HEADER, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveTraceId(final HttpServletRequest request) {
        final String inboundTraceId = request.getHeader(TRACE_HEADER);
        if (inboundTraceId != null && !inboundTraceId.isBlank()) {
            return inboundTraceId;
        }
        return UUID.randomUUID().toString();
    }
}
