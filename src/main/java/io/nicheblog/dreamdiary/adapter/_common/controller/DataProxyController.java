package io.nicheblog.dreamdiary.adapter._common.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global.config.HttpClientConfig;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.JsonRestTemplate;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Enumeration;

/**
 * DataProxyController
 * <pre>
 *  (CORS 회피용) API 프록시 호출 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @not-in-use 미사용중
 * TODO: 한참 손봐야된다.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")   // CORS 에러 해결 위한 조치
@Log4j2
@Tag(
        name = "TODO: Data Proxy API",
        description = "Data Proxy API입니다."
)
public class DataProxyController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.API;      // 작업 카테고리 (로그 적재용)

    /**
     * PROXY REQUEST 전달받은 요청을 서버단에서 포크해서 그대로 다시 리턴한다.
     */
    @PostMapping(value = "/api/proxy/**")
    public ResponseEntity<Object> proxy(
            final @RequestBody(required = false) byte[] body
    ) throws Exception {

        final String reqURL = request.getRequestURI();

        String orgnLProtocol = request.getHeader("x-origin-protocol");
        final boolean isOrgnlProtocolEmpty = StringUtils.isEmpty(orgnLProtocol);
        orgnLProtocol = isOrgnlProtocolEmpty ? "https" : orgnLProtocol.toLowerCase();

        final String originReqURL = reqURL.replaceAll("/api/proxy/", "");
        final String originQueryString = request.getQueryString();
        final String urlStr = orgnLProtocol + "://" + originReqURL + (StringUtils.isEmpty(originQueryString) ? "" : "?" + originQueryString);
        URI url = new URI(urlStr);

        // method
        HttpMethod method;
        final String originMethod = request.getHeader("x-origin-method");
        if (StringUtils.isNotEmpty(originMethod)) {
            method = HttpMethod.valueOf(originMethod.toUpperCase());
        } else {
            method = HttpMethod.valueOf(request.getMethod()
                                               .toUpperCase());
        }

        // header
        final boolean excludeHost = "true".equalsIgnoreCase(request.getHeader("x-exclude-host"));
        Enumeration<String> headerNames = request.getHeaderNames();

        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            if ("x-origin-protocol".equalsIgnoreCase(headerName) || "x-origin-method".equalsIgnoreCase(headerName) || "x-exclude-host".equalsIgnoreCase(headerName)) {
                continue;
            }

            if (excludeHost && "host".equalsIgnoreCase(headerName)) {
                continue;
            }

            String headerValue = request.getHeader(headerName);
            headers.add(headerName, headerValue);
        }
        headers.set("Accept", "application/json");
        headers.set("Authorization", "secret_whxACnuVTRxMtB1p19pOrlXSQUvUqscD1Zl9U0gh5Sv");

        // http entity (body, header)
        final HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);

        final JsonRestTemplate restTemplate = new JsonRestTemplate(HttpClientConfig.trustRequestFactory(), Constant.URL_ENC_FALSE);

        return restTemplate.exchange(url, method, httpEntity, Object.class);
    }
}
