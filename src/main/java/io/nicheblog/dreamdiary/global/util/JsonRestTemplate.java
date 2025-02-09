package io.nicheblog.dreamdiary.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * JsonRestTemplate
 * <pre>
 *  기존 RestTemplate이 서버측 Response를 'text/html;charset=utf-8'로 읽어오면서 생기는 문제를 해결하기 위해 커스터마이징.
 *  모든 응답의 Content-Type을 json으로 이해하게끔 설정
 * </pre>
 *
 * @author nichefish
 */
public class JsonRestTemplate
        extends RestTemplate {

    /**
     * 커스텀 생성자
     *
     * @param clientHttpRequestFactory HTTP 요청을 생성하는 팩토리 객체.
     */
    public JsonRestTemplate(final ClientHttpRequestFactory clientHttpRequestFactory) {
        super(clientHttpRequestFactory);

        // ObjectMapper 설정: JDK 8, JavaTime 모듈 등록 및 날짜 직렬화 설정
        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module())
                                                      .registerModule(new JavaTimeModule())
                                                      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 커스텀 JSON 메시지 컨버터 추가
        final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        final MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter() {
            /**
             * 모든 클래스 타입을 읽을 수 있도록 설정.
             */
            public boolean canRead(final @NotNull Class<?> clazz, final org.springframework.http.MediaType mediaType) {
                return true;
            }
            /**
             * 모든 타입을 읽을 수 있도록 설정.
             */
            public boolean canRead(final java.lang.reflect.@NotNull Type type, final Class<?> contextClass, final org.springframework.http.MediaType mediaType) {
                return true;
            }
            /**
             * 특정 미디어 타입을 읽을 수 있도록 설정.
             */
            protected boolean canRead(final org.springframework.http.MediaType mediaType) {
                return true;
            }
        };

        // ObjectMapper를 JSON 메시지 컨버터에 적용
        jsonMessageConverter.setObjectMapper(objectMapper);
        messageConverters.add(jsonMessageConverter);

        // RestTemplate의 메시지 컨버터 설정
        super.setMessageConverters(messageConverters);
    }

    /**
     * URL 인코딩을 비활성화할 수 있는 추가 생성자.
     *
     * @param clientHttpRequestFactory HTTP 요청을 생성하는 팩토리 객체.
     * @param urlEncAt URL 인코딩 활성화 여부 (false일 경우 비활성화).
     */
    public JsonRestTemplate(
            final ClientHttpRequestFactory clientHttpRequestFactory,
            final Boolean urlEncAt
    ) {
        this(clientHttpRequestFactory);
        if (!urlEncAt) {
            // disable uriEncoding
            final DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
            defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
            super.setUriTemplateHandler(defaultUriBuilderFactory);
        }
    }
}