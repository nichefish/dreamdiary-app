package io.nicheblog.dreamdiary.auth.oauth2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * OAuth2Dto
 *
 * @author nichefish a
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2Dto {
    /** Access Token */
    @JsonProperty("access_token")
    private String accessToken;
    /** Token Type (ex.bearer) */
    @JsonProperty("token_type")
    private String tokenType;
    /** 만료 시간 (초 단위) */
    @JsonProperty("expires_in")
    private Long expiresIn;
    /** 상태 값 (추가로 필요한 경우) */
    private String state;
}
