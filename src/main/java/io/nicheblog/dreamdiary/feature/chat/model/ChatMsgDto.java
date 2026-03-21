package io.nicheblog.dreamdiary.feature.chat.model;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseClsfDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * ChatMsgDto
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
public class ChatMsgDto
        extends BaseClsfDto
        implements Identifiable<Integer> {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.CHAT_MSG;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE.name() + "_CTGR_CD";

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = CONTENT_TYPE.key;

    /** 내 메세지(등록자) 여부 */
    private Boolean isRegstr;

       /** 제목 */
    protected String title;

    /** 내용 */
    protected String cn;

    /** 마크다운 처리된 내용 */
    protected String markdownCn;

    /** 중요 여부 (Y/N) */
    @Builder.Default
    protected String imprtcYn = "N";

    /** 상단고정 여부 (Y/N) */
    @Builder.Default
    protected String fxdYn = "N";

    /* ----- */

    @Override
    public Integer getKey() {
        return this.postNo;
    }
}
