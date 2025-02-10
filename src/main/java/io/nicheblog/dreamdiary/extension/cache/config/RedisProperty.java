package io.nicheblog.dreamdiary.extension.cache.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RedisiProperty
 * <pre>
 *  application.yml에서 Redis 관련 설정값을 가져온다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "spring.redis")
@Getter
@Setter
public class RedisProperty {

    /** Redis 호스트 */
    private String host;

    /** Redis 포트 */
    private Integer port;

    /** Redis 패스워드 */
    private String password;

}
