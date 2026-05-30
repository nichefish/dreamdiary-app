package io.nicheblog.dreamdiary.infrastructure.messaging.jandi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * JandiApiSndMsgConnectInfoDto
 * <pre>
 *  API:: JANDI:: incoming webhook 메세지 전송시 하단 첨부 영역 정보 Dto
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class JandiApiSndMsgConnectInfoDto {

    /**
     * title
     */
    @JsonProperty("title")
    private String title;

    /**
     * description
     */
    @JsonProperty("description")
    private String description;

    /**
     * imageUrl
     */
    @JsonProperty("imageUrl")
    private String imageUrl;
}
