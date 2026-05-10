package io.nicheblog.dreamdiary.feature.chat.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * ChatSessionDto
 * <pre>
 *  AI 채팅 세션 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChatSessionDto
        extends BaseAuditDto
        implements Identifiable<Integer> {

    /** 세션 ID */
    private Integer id;

    /** 제목 */
    private String title;

    /** 상태 */
    @Builder.Default
    private String status = "ACTIVE";

    /** AI 모델 */
    private String model;

    /** 시스템 프롬프트 */
    private String systemPrompt;

    /** 마지막 메시지 일시 */
    private String lastMessageAt;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }
}
