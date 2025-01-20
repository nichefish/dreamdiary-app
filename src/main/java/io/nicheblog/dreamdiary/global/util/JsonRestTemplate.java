package io.nicheblog.dreamdiary.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
     */
    public JsonRestTemplate(final ClientHttpRequestFactory clientHttpRequestFactory) {
        super(clientHttpRequestFactory);

        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module())
                                                      .registerModule(new JavaTimeModule())
                                                      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        final MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter() {

            public boolean canRead(
                    Class<?> clazz,
                    org.springframework.http.MediaType mediaType
            ) {
                return true;
            }

            public boolean canRead(
                    final java.lang.reflect.Type type,
                    final Class<?> contextClass,
                    final org.springframework.http.MediaType mediaType
            ) {
                return true;
            }

            protected boolean canRead(
                    final org.springframework.http.MediaType mediaType
            ) {
                return true;
            }
        };

        jsonMessageConverter.setObjectMapper(objectMapper);
        messageConverters.add(jsonMessageConverter);
        super.setMessageConverters(messageConverters);
    }

    /**
     * 커스텀 생성자 (UrlEncoding 설정 끄기)
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