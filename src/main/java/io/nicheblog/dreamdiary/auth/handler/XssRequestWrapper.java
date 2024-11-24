package io.nicheblog.dreamdiary.auth.handler;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * XssRequestWrapper
 * <pre>
 *  XSS 방지 위해 request 전처리. (escaping)
 *  TODO: 보완 필요 (미사용 중)
 * </pre>
 *
 * @author nichefish
 */
public class XssRequestWrapper
        extends HttpServletRequestWrapper {

    /**
     * 생성자
     */
    public XssRequestWrapper(final HttpServletRequest servletRequest) {
        super(servletRequest);
    }

    /**
     * 파라미터 값 목록에 대한 처리
     */
    @Override
    public String[] getParameterValues(final String parameterParam) {
        String parameter = this.cleanXSS(parameterParam);

        String[] values = super.getParameterValues(parameter);
        if (values == null) return null;
        return Arrays.stream(values)
                .map(this::cleanXSS)
                .toArray(String[]::new);
    }

    /**
     * 단일 파라미터 값에 대한 처리
     */
    @Override
    public String getParameter(final String parameterParam) {
        String parameter = this.cleanXSS(parameterParam);

        String value = super.getParameter(parameter);
        if (StringUtils.isEmpty(value)) return null;
        return this.cleanXSS(value);
    }

    /**
     * 파라미터 맵에 대한 처리
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> originalMap = super.getParameterMap();
        if (originalMap == null) return null;
        return originalMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> cleanXSS(entry.getKey()), // 키에 대한 XSS 처리
                        entry -> Arrays.stream(entry.getValue()) // 값 배열에 대한 XSS 처리
                                .map(this::cleanXSS) // 각 값에 cleanXSS 적용
                                .toArray(String[]::new) // 결과를 배열로 다시 수집
                ));
    }

    /**
     * 헤더 값에 대한 처리
     */
    @Override
    public String getHeader(final String name) {
        String value = super.getHeader(name);
        if (StringUtils.isEmpty(value)) return null;
        return this.cleanXSS(value);
    }

    /**
     * 값 이스케이핑 공통함수
     */
    private String cleanXSS(final String value) {
        // XSS 처리 로직을 여기에 구현
        // &를 &amp;로 치환하는 작업이 다른 모든 치환 작업보다 먼저 수행되어야 한다.
        if(StringUtils.isEmpty(value)) return null;
        return value.replace("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replace(">", "&gt;")
                .replace("(", "&#40;")
                .replace(")", "&#41;")
                .replace("'", "&#39;")
                .replace("\"", "&quot;")
                .replace("`", "&apos;");
    }
}