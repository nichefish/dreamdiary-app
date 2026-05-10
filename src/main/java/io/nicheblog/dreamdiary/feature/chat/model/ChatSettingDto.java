package io.nicheblog.dreamdiary.feature.chat.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * ChatSettingDto
 * <pre>
 *  AI 채팅 설정 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChatSettingDto
        extends BaseAuditDto
        implements Identifiable<Integer> {

    /** 설정 ID */
    private Integer id;

    /** 설정 범위 */
    @Builder.Default
    private String scope = "USER";

    /** 설정 범위 키 */
    private String scopeKey;

    /** 최근 대화 기억 메시지 수 */
    @Builder.Default
    private Integer recentMessageLimit = 20;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }
}
