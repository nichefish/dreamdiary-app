package io.nicheblog.dreamdiary.infrastructure.messaging.jandi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JandiApiRcvMsgDto
 * <pre>
 *  API:: JANDI:: outgoing webhook 메세지 수신 Dto
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class JandiApiRcvMsgDto {

    /**
     * token
     */
    @JsonProperty("token")
    private String token;

    /**
     * teamName
     */
    @JsonProperty("teamName")
    private String teamName;

    /**
     * roomName
     */
    @JsonProperty("roomName")
    private String roomName;

    /**
     * writerName
     */
    @JsonProperty("writerName")
    private String writerName;

    /**
     * text
     */
    @JsonProperty("text")
    private String text;

    /**
     * keyword
     */
    @JsonProperty("keyword")
    private String keyword;

    /**
     * createdAt
     */
    @JsonProperty("createdAt")
    private String createdAt;
}
