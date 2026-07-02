package io.nicheblog.dreamdiary.infrastructure.release.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ReleaseHistoryProperties
 * <pre>
 *  Release history settings.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "app.release-history")
@Getter
@Setter
public class ReleaseHistoryProperties {

    /** Maximum number of release history rows returned by list APIs. */
    private Integer maxListSize = 50;
}
