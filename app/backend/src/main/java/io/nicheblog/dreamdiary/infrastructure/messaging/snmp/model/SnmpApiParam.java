package io.nicheblog.dreamdiary.infrastructure.messaging.snmp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;

/**
 * SnmpParam
 * <pre>
 *  API:: SNMP 연동 Param.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@ToString
public class SnmpApiParam
        implements Serializable {

    /** IP 주소 */
    @Builder.Default
    private String ipAddr = "127.0.0.1";

    /** community */
    @Builder.Default
    private String community = "private";

    /** trapOid */
    @Builder.Default
    private String trapOid = "1";

    /** 메세지 */
    @Builder.Default
    private String message = "Security";

    /** 포트 */
    @Builder.Default
    private Integer port = 162;

    /* ----- */

    /**
     * 생성자.
     * @param ipAddr - SNMP API 요청을 보낼 IP 주소
     */
    public SnmpApiParam(final String ipAddr) {
        this.ipAddr = ipAddr;
    }
}
