package io.nicheblog.dreamdiary.global;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ServerInfo
 * <pre>
 *  appliccation-{profile}.yml 에 설정된 서버 정보를 가져온다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "server")
@Getter
@Setter
public class ServerInfo {

    /** 도메인 */
    private String domain;
    /** port */
    private String port;
}
