package io.nicheblog.dreamdiary.global.config;

import io.nicheblog.dreamdiary.infrastructure.log.actvty.filter.TraceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * FilterConfig
 * 전역 Filter 등록 설정 클래스.
 *
 * @author nichefish
 */
@Configuration
public class FilterConfig {

    /**
     * TraceFilter 등록
     * 모든 요청에 TraceId 설정 위해 가장 먼저 실행되도록 설정
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilter() {
        final FilterRegistrationBean<TraceFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new TraceFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

}
