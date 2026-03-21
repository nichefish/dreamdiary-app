package io.nicheblog.dreamdiary.infrastructure.messaging.jandi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.nicheblog.dreamdiary.infrastructure.web.model.BaseApiRespns;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * JandiApiRespnsDto
 * <pre>
 *  API:: JANDI:: API 호출 응답 반환 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JandiApiRespnsDto
        extends BaseApiRespns {

    // 기본 attribute (result, code, status, message) 상속

    /** message (jsonProperty 재정의) */
    @JsonProperty("msg")
    private String message;
}
