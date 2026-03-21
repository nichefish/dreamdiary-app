package io.nicheblog.dreamdiary.infrastructure.notify.jandi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JandiApiSndMsgDto
 * <pre>
 *  API:: JANDI:: incoming webhook 메세지 전송 요청 Dto
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JandiApiSndMsgDto {

    /**
     * email
     */
    @Schema(name = "email", description = "같은 내용으로 여러명에게 발송시 컴마(,)로 구분", example = "nichefish@gmail.com,nichefish@naver.com")
    @JsonProperty("email")
    private String email;

    /**
     * body
     */
    @JsonProperty("body")
    private String msg;

    /**
     * connectColor (optional)
     */
    @JsonProperty("connectColor")
    private String connectColor = "#FAC11B";

    /**
     * connectInfo (optional)
     */
    @JsonProperty("connectInfo")
    private List<JandiApiSndMsgConnectInfoDto> connectInfo = new ArrayList<>();

    /**
     * 생성자 :: msg
     */
    public JandiApiSndMsgDto(final String msg) {
        this.msg = msg;
    }

    /* ----- */

    public JandiApiSndMsgDto(
            final String msg,
            final String connMsg
    ) {
        this(msg);
        JandiApiSndMsgConnectInfoDto conn = new JandiApiSndMsgConnectInfoDto();
    }

    /**
     * connectorInfo 추가추가
     */
    public void addConnMsg(final JandiApiSndMsgConnectInfoDto connInfo) {
        this.connectInfo.add(connInfo);
    }
}
