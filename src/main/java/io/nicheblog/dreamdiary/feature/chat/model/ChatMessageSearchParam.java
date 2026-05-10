package io.nicheblog.dreamdiary.feature.chat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.nicheblog.dreamdiary.feature.attachable._shared.model.param.BaseAttachableSearchParam;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * ChatMessageSearchParam
 * <pre>
 *  채팅 메세지 목록 검색 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageSearchParam
        extends BaseAttachableSearchParam {

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.CHAT_MESSAGE.key;

    /** 팝업 여부 */
    @Builder.Default
    private String popupYn = "N";

    /** 시작일자 */
    private Date managtStartDt;

    /** 채팅 세션 ID */
    private Integer sessionId;
}
