package io.nicheblog.dreamdiary.feature.file.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * FileProperties
 * <pre>
 *  File upload policy settings.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "app.file")
@Getter
@Setter
public class FileProperties {

    /** Allowed file extensions separated by {@code |}. */
    private String allowedExtensions;
    /** Allowed MIME types separated by {@code |}. */
    private String allowedMimeTypes;
    /** Image extensions separated by {@code |}. */
    private String imageExtensions;
}
