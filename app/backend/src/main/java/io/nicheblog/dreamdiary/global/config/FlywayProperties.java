package io.nicheblog.dreamdiary.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * FlywayProperties
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "spring.flyway")
@Getter
@Setter
public class FlywayProperties {
    private String baselineVersion;
    private String target;

    private String url;
    private String user;
    private String password;

    private List<String> schemas;
    private List<String> locations;
    private boolean baselineOnMigrate = false;
    private boolean validateOnMigrate = false;

    /** 배포일자 */
    private String releaseDate;
}