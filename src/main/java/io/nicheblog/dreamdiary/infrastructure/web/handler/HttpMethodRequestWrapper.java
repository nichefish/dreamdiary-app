package io.nicheblog.dreamdiary.infrastructure.web.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * HttpMethodRequestWrapper
 * <pre>
 *  HttpServletRequest에서 메소드를 GET으로 오버라이드
 * </pre>
 *
 * @author nichefish
 */
public class HttpMethodRequestWrapper
        extends HttpServletRequestWrapper {

    public HttpMethodRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getMethod() {
        return "GET";  // 항상 GET 메서드를 반환하도록 수정
    }
}
