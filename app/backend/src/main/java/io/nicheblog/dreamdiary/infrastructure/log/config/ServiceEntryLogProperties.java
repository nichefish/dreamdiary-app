package io.nicheblog.dreamdiary.infrastructure.log.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ServiceEntryLogProperties
 * <pre>
 *  Service entry log AOP settings.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "app.logging.service-entry")
@Getter
@Setter
public class ServiceEntryLogProperties {

    /** Whether service entry logging is enabled. */
    private boolean enabled;
    /** Whether service finish logging is enabled. */
    private boolean logFinish;
}
