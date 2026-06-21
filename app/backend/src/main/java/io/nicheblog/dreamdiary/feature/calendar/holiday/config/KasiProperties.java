package io.nicheblog.dreamdiary.feature.calendar.holiday.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * KasiProperties
 * <pre>
 *  KASI holiday API settings.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "app.integration.kasi")
@Getter
@Setter
public class KasiProperties {

    /** KASI API service key. */
    private String serviceKey;
    /** KASI holiday API URL. */
    private String apiUrl;
}
