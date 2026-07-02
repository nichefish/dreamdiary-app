package io.nicheblog.dreamdiary.auth.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 보안 필터와 진입점의 Ajax 오류 응답을 공통 JSON 계약으로 작성한다. */
@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    /**
     * HTTP 상태와 본문 status가 일치하는 Ajax 오류 응답을 작성한다.
     *
     * @param response HTTP 응답
     * @param status HTTP 상태 코드
     * @param message 사용자에게 표시할 안전한 메시지
     * @throws IOException 응답 작성 실패
     */
    public void write(final HttpServletResponse response, final int status, final String message) throws IOException {
        final AjaxResponse body = new AjaxResponse(false, message);
        body.setStatus(status);
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
