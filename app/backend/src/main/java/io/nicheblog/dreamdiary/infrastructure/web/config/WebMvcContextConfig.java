package io.nicheblog.dreamdiary.infrastructure.web.config;

import io.nicheblog.dreamdiary.auth.security.interceptor.CsrfInterceptor;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.freemarker.interceptor.FreemarkerInterceptor;
import io.nicheblog.dreamdiary.infrastructure.log.interceptor.LogInterceptor;
import io.nicheblog.dreamdiary.infrastructure.web.handler.UTF8DecodeResourceResolver;
import io.nicheblog.dreamdiary.infrastructure.web.interceptor.CookieInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.mobile.device.DeviceResolverHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * WebMvcContextConfig
 * <pre>
 *  interceptor 설정
 * </pre>
 *
 * @author nichefish
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcContextConfig
        implements WebMvcConfigurer {

    private final FreemarkerInterceptor freemarkerInterceptor;
    private final CookieInterceptor cookieInterceptor;
    private final CsrfInterceptor csrfInterceptor;
    private final LogInterceptor logActvtyInterceptor;

    private static final List<String> STATIC_RESOURCES_URL_PATTERN = List.of(Constant.STATIC_PATHS);

    /**
     * 모든 /api/** 경로에 대하여 CORS 허용 설정 추가
     */
    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedMethods("GET", "POST")
                .allowedOrigins("*")
                .allowedHeaders("*");
    }

    /**
     * 프로젝트 외부 이미지 연결
     * 업로드파일 + vod 폴더 통째로 연결 (업로드 링크 위해)
     **/
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {

        // 파일 업로드 경로
        final String upfileContextPath = "/upfile/**";
        final String upfileResourcePath = "file:files/upfiles/";
        registry.addResourceHandler(upfileContextPath)
                .addResourceLocations(upfileResourcePath)
                .resourceChain(true)
                .addResolver(new UTF8DecodeResourceResolver());
        // 정적 컨텐츠 경로
        final String contentContextPath = "/content/**";
        final String contentResourcePath = "file:files/contents/";
        registry.addResourceHandler(contentContextPath)
                .addResourceLocations(contentResourcePath)
                .resourceChain(true)
                .addResolver(new UTF8DecodeResourceResolver());
        // react 경로 = 기본경로에 추가로 동작하도록
        final String reactContextPath = "/react/**";
        final String reactResourcePath = "file:static/react/";
        registry.addResourceHandler(reactContextPath)
                .addResourceLocations(reactResourcePath)
                .resourceChain(true)
                .addResolver(new UTF8DecodeResourceResolver());
        // vue-app 경로 = Vue SPA (index.html SPA 폴백 포함)
        final String vueAppContextPath = "/vue-app/**";
        final String vueAppResourcePath = "file:static/vue-app/";
        registry.addResourceHandler(vueAppContextPath)
                .addResourceLocations(vueAppResourcePath)
                .resourceChain(false)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(final String resourcePath, final Resource location) throws java.io.IOException {
                        final Resource requested = location.createRelative(resourcePath);
                        // 실제 파일이 존재하면 그대로 서빙, 없으면 SPA index.html 폴백
                        if (requested.exists() && requested.isReadable()) return requested;
                        final Resource indexHtml = new FileSystemResource("static/vue-app/index.html");
                        return indexHtml.exists() ? indexHtml : null;
                    }
                });
        // 기본 static 경로
        final String staticContextPath = "/static/**";
        final String orglStaticPath = "classpath:/static/";
        final String externalStaticPath = "static/";
        registry.addResourceHandler(staticContextPath)
                .addResourceLocations(orglStaticPath, externalStaticPath)
                .resourceChain(true)
                .addResolver(new UTF8DecodeResourceResolver());
    }

    /**
     * interceptor 추가
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        // freemarker interceptor
        // 화면 조회에만 적용, ajax 및 기타 작동에는 적용 안함
        registry.addInterceptor(freemarkerInterceptor)
                /* 페이지 접근에 대해서만 처리 */
                .addPathPatterns("/")
                .addPathPatterns("/**/*.do")
                /* 에러 화면 경로 포함 */
                .addPathPatterns(Url.ERROR, Url.ERROR + "/**")
                /* 스태틱 자원 경로의 경우 처리하지 않음 */
                .excludePathPatterns(STATIC_RESOURCES_URL_PATTERN)
                /* API 경로의 경우 처리하지 않음 */
                .excludePathPatterns("/api/**")
                /* 파일 다운로드의 경우 처리하지 않음 */
                .excludePathPatterns("/**/*-download.do");

        // 쿠키 관련 인터셉터 수동 추가
        registry.addInterceptor(cookieInterceptor)
                /* 페이지 접근에 대해서만 처리 */
                .addPathPatterns("/")
                .addPathPatterns("/**/*.do")
                /* 스태틱 자원 경로의 경우 처리하지 않음 */
                .excludePathPatterns(STATIC_RESOURCES_URL_PATTERN);

        // CSRF 관련 인터셉터 수동 추가
        registry.addInterceptor(csrfInterceptor)
                /* 페이지 접근에 대해서만 처리 */
                .addPathPatterns("/")
                .addPathPatterns("/**/*.do")
                /* 스태틱 자원 경로의 경우 처리하지 않음 */
                .excludePathPatterns(STATIC_RESOURCES_URL_PATTERN);

        // device 감지 관련 인터셉터 수동 추가
        registry.addInterceptor(new DeviceResolverHandlerInterceptor());

        // 로그 관련 인터셉터 수동 추가`
        registry.addInterceptor(logActvtyInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(STATIC_RESOURCES_URL_PATTERN);
    }
}
