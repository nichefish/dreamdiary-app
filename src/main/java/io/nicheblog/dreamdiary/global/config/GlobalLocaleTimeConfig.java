package io.nicheblog.dreamdiary.global.config;

import io.nicheblog.dreamdiary.global.Constant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.annotation.PostConstruct;
import java.util.Locale;
import java.util.TimeZone;

/**
 * GlobalLocaleTimeConfig
 * <pre>
 *  Global default locale/time-zone configuration.
 * </pre>
 */
@Configuration
public class GlobalLocaleTimeConfig {

    /**
     * Set JVM and Spring default locale/time-zone once at startup.
     */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(Constant.LOC_SEOUL));
        Locale.setDefault(Locale.KOREA);
        LocaleContextHolder.setDefaultLocale(Locale.KOREA);
    }

    /**
     * Use request Accept-Language header with Korea as fallback.
     */
    @Bean
    public LocaleResolver localeResolver() {
        final AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.KOREA);

        return localeResolver;
    }
}
