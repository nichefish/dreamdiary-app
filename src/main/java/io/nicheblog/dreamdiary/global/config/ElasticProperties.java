package io.nicheblog.dreamdiary.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ElasticProperty
 * <pre>
 *  application.yml에서 ElasticSearch 관련 설정값을 가져온다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "spring.elasticsearch")
@Getter
@Setter
public class ElasticProperties {

    /** ElasticSearch 사용자 */
    private String username;
    /** ElasticSearch 패스워드 */
    private String password;
    /** ElasticSearch uris */
    private String[] uris;
}
