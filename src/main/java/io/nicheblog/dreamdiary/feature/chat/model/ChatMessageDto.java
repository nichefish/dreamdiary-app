package io.nicheblog.dreamdiary.feature.chat.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * ChatMessageDto
 * <pre>
 *  채팅 메세지 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChatMessageDto
        extends BaseAttachableDto
        implements Identifiable<Integer> {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.CHAT_MESSAGE;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE.name() + "_CTGR_CD";

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = CONTENT_TYPE.key;

    /** 내 메세지(등록자) 여부 */
    private Boolean isCreatedBy;

    /** 제목 */
    private String title;

    /** 내용 */
    private String content;

    /** 마크다운 처리된 내용 */
    private String markdownContent;

    /** 글 분류 코드 */
    private String categoryCode;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }
}
