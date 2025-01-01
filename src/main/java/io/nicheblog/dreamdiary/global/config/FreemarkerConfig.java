package io.nicheblog.dreamdiary.global.config;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * FreemarkerConfig
 * <pre>
 *  Freemarker 관련 설정 커스터마이즈
 * </pre>
 *
 * @author nichefish
 */
@Configuration
public class FreemarkerConfig
        implements BeanPostProcessor {

    /** 기본 세팅 */
    private final Properties PROPERTY_SETTINGS = new Properties() {{
        setProperty("template_exception_handler", "ignore");
    }};
    /** 템플릿 경로 */
    private final String[] TEMPLATE_PATHS = {
            "classpath:/static/react",
            "classpath:/react",
            "file:templates"
    };

    /**
     * FreeMarkerConfigurer 빈의 초기화 후 처리를 담당합니다.
     *
     * @param bean 초기화된 빈 객체 (FreeMarkerConfigurer를 포함할 수 있음)
     * @param beanName 빈의 이름
     * @return {@link Object} -- 초기화된 후 수정된 빈 객체
     * @throws BeansException 빈 초기화 중 발생할 수 있는 예외
     */
    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(final @NotNull Object bean, final @NotNull String beanName) throws BeansException {
        if (bean instanceof FreeMarkerConfigurer) {
            final FreeMarkerConfigurer configurer = (FreeMarkerConfigurer) bean;
            final freemarker.template.Configuration configuration = configurer.getConfiguration();

            // 프리마커 기본 설정 세팅
            this.setFreemarkerDefaults(configuration);

            // 추가 설정 추가
            configurer.setFreemarkerSettings(PROPERTY_SETTINGS);
            // 템플릿 위치 추가
            configurer.setTemplateLoaderPaths(TEMPLATE_PATHS);
        }
        return bean;
    }

    /**
     * Freemarker 기본 설정을 지정합니다.
     *
     * @param configuration Freemarker 설정 객체
     */
    private void setFreemarkerDefaults(freemarker.template.Configuration configuration) throws TemplateModelException {
        // 인코딩 설정
        configuration.setDefaultEncoding(Constant.CHARSET_UTF_8);
        // Locale, TimeZone 설정
        configuration.setLocale(Constant.LC_KO);
        configuration.setTimeZone(Constant.TZ_SEOUL);
        // 날짜 포맷 설정
        configuration.setDateTimeFormat(DatePtn.DATETIME.pattern);
        configuration.setDateFormat(DatePtn.DATE.pattern);
        configuration.setTimeFormat(DatePtn.TIME.pattern);
        // 숫자에 콤마 제거!!! (1000 넘어가는 ID(PK)에서 개꼬임...)
        configuration.setNumberFormat("computer");
        // 템플릿 import
        configuration.addAutoImport("spring", "lib/spring.ftl");
        configuration.addAutoImport("fn", "lib/functions.ftl");
        configuration.addAutoImport("component", "lib/components.ftl");
        // static 변수 임포트
        configuration.setSharedVariables(this.getSharedVariables(configuration));
    }

    /**
     * 프리마커 공유 변수를 가져와 추가 설정을 세팅합니다.
     *
     * @param configuration 프리메커 설정 객체
     * @return {@link Map} -- 공유 변수들이 담긴 Map 객체
     * @throws TemplateModelException 템플릿 모델을 처리하는 중 발생할 수 있는 예외
     */
    private Map<String, ?> getSharedVariables(final freemarker.template.Configuration configuration) throws TemplateModelException {
        return this.getSharedVariables((BeansWrapper) configuration.getObjectWrapper());
    }

    /**
     * 프리마커 공유 변수를 가져와 추가 설정을 세팅합니다.
     *
     * @param config 프리메커 BeansWrapper 설정 객체
     * @return {@link Map} -- 공유 변수들이 담긴 Map 객체
     * @throws TemplateModelException 템플릿 모델을 처리하는 중 발생할 수 있는 예외
     */
    private Map<String, ?> getSharedVariables(final BeansWrapper config) throws TemplateModelException {
        Map<String, Object> sharedVariables = new HashMap<>();

        // Add global variables and Add static support
        final TemplateHashModel statics = config.getStaticModels();
        sharedVariables.put("Statics", statics);
        sharedVariables.put("Constant", statics.get("io.nicheblog.dreamdiary.global.Constant"));
        sharedVariables.put("Url", statics.get("io.nicheblog.dreamdiary.global.Url"));
        // TODO: SiteMenu 빼기
        sharedVariables.put("SiteMenu", statics.get("io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu"));
        sharedVariables.put("DateUtils", statics.get("io.nicheblog.dreamdiary.global.util.date.DateUtils"));
        return sharedVariables;
    }

    /**
     * 템플릿을 불러올 경로 목록을 반환합니다.
     *
     * @return {@link Properties} -- 세팅 정보
     */
    private Properties getPropertySettings() {
        final Properties settings = new Properties();
        settings.setProperty("template_exception_handler", "ignore");
        return settings;
    }
}
