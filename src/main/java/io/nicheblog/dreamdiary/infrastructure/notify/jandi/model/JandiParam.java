package io.nicheblog.dreamdiary.infrastructure.notify.jandi.model;

import io.nicheblog.dreamdiary.infrastructure.notify.jandi.JandiTopic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JandiParam
 * <pre>
 *  API:: JANDI:: JANDI API 호출 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class JandiParam {

    /** 잔디 알림 발송 여부 (Y/N) */
    @Builder.Default
    @Schema(description = "대상 토픽 코드", example = "NOTICE")
    private String jandiYn = "N";

    /** 대상 토픽 코드 */
    @Schema(description = "대상 토픽 코드", example = "NOTICE")
    private JandiTopic trgetTopic;

    /** 메세지 */
    @Schema(description = "대상 토픽 코드", example = "새로운 휴가계획서가 등록되었습니다.")
    private String msg;
}
