package io.nicheblog.dreamdiary.infrastructure.freemarker.config;

import io.nicheblog.dreamdiary.global.Constant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

/**
 * FreemarkerConfig
 * <pre>
 *  Freemarker 관련 설정 커스터마이즈
 * </pre>
 *
 * 변경 전: MVC FreeMarkerConfigurer 커스터마이즈(BeanPostProcessor, react 템플릿 경로·공유 변수 주입) + 이메일용 config 겸용.
 * 변경 후: 화면 뷰가 전부 Vue SPA 로 이관되어 MVC 렌더 경로 제거 — 이메일 템플릿 렌더용 config 만 남긴다.
 *
 * @author nichefish
 */
@Configuration
public class FreemarkerConfig {

    /** Email template root. (화면 뷰 FreeMarker 는 제거 완료 — 이메일 전용) */
    private static final String EMAIL_TEMPLATE_PATH = "classpath:/templates/";

    /**
     * 빈 설정: 이메일용 config
     * @return {@link FreeMarkerConfigurationFactoryBean} -- Freemarker 설정 객체
     */
    @Bean
    @Primary
    public FreeMarkerConfigurationFactoryBean freemarkerEmailConfig() {
        final FreeMarkerConfigurationFactoryBean factoryBean = new FreeMarkerConfigurationFactoryBean();
        factoryBean.setTemplateLoaderPath(EMAIL_TEMPLATE_PATH);
        factoryBean.setDefaultEncoding(Constant.CHARSET_UTF_8);
        return factoryBean;
    }
}
