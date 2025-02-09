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
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.*;

/**
 * FreemarkerConfig
 * <pre>
 *  Freemarker 관련 설정 커스터마이즈
 * </pre>
 *
 * @author nichefish
 */
@Configuration
@Profile("test")
public class TestFreemarkerConfig
        implements BeanPostProcessor {

    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if (bean instanceof FreeMarkerConfigurer) {
            FreeMarkerConfigurer configurer = (FreeMarkerConfigurer) bean;
            freemarker.template.Configuration configuration = configurer.getConfiguration();
            BeansWrapper objectWrapper = (BeansWrapper) configuration.getObjectWrapper();
            // 인코딩 설정
            configuration.setDefaultEncoding(Constant.CHARSET_UTF_8);
            // Locale, TimeZone 설정
            configuration.setLocale(Constant.LC_KO);
            configuration.setTimeZone(Constant.TZ_SEOUL);
            // 날짜 포맷 설정
            configuration.setDateTimeFormat(DatePtn.DATETIME.pattern);
            configuration.setDateFormat(DatePtn.DATE.pattern);
            configuration.setTimeFormat(DatePtn.TIME.pattern);
            // static 변수 임포트
            configuration.setSharedVariables(this.getSharedVariables(objectWrapper));
            // 숫자에 콤마 제거!!! (1000 넘어가는 ID(PK)에서 개꼬임...)
            configuration.addAutoImport("spring", "lib/spring.ftl");
            configuration.addAutoImport("fn", "lib/functions.ftl");
            configuration.addAutoImport("component", "lib/components.ftl");
            configuration.setNumberFormat("computer");
            Properties settings = new Properties();
            settings.setProperty("template_exception_handler", "ignore");
            configurer.setFreemarkerSettings(settings);
            // 템플릿 위치 추가
            List<String> pluginTemplatePaths = new ArrayList<>();
            pluginTemplatePaths.add(0, "classpath:/static/react");
            pluginTemplatePaths.add(0, "classpath:/react");
            pluginTemplatePaths.add(0, "file:templates");
            String[] paths = pluginTemplatePaths.toArray(new String[0]);
            configurer.setTemplateLoaderPaths(paths);
        }
        return bean;
    }

    private Map<String, ?> getSharedVariables(BeansWrapper config) throws TemplateModelException {
        Map<String, Object> sharedVariables = new HashMap<>();

        // Add global variables and Add static support
        TemplateHashModel statics = config.getStaticModels();
        sharedVariables.put("Statics", statics);
        sharedVariables.put("Constant", statics.get("io.nicheblog.dreamdiary.global.Constant"));
        sharedVariables.put("Url", statics.get("io.nicheblog.dreamdiary.global.Url"));
        sharedVariables.put("DateUtils", statics.get("io.nicheblog.dreamdiary.global.util.date.DateUtils"));
        return sharedVariables;
    }
}
