package io.nicheblog.dreamdiary.infrastructure.web.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * ApiEtagFilterConfig
 * <pre>
 *  /api 하위 GET 응답에 ShallowEtag 를 적용해 조건부 GET(304)을 지원한다.
 *  응답 바디 해시로 ETag 를 만들고, If-None-Match 가 일치하면 304(빈 바디)로 응답하여
 *  미변경 조회의 전송 대역폭과 클라이언트 파싱 비용을 절감한다.
 *  서버 연산 자체는 수행되며(해당 조회는 @Cacheable 로 이미 저렴), POST 등 쓰기 요청은 대상이 아니다.
 * </pre>
 *
 * @author nichefish
 */
@Configuration
public class ApiEtagFilterConfig {

    /**
     * /api 하위 경로에만 ShallowEtag 필터를 등록한다.
     * ShallowEtagHeaderFilter 는 캐시 가능한 GET 2xx 응답에만 ETag 를 생성하므로
     * 쓰기(POST) 및 비-2xx 응답은 그대로 통과한다.
     *
     * @return {@link FilterRegistrationBean} -- /api/* 경로에 매핑된 ShallowEtag 필터 등록 정보
     */
    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> apiShallowEtagFilter() {
        final FilterRegistrationBean<ShallowEtagHeaderFilter> registration =
                new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("apiShallowEtagFilter");
        return registration;
    }
}