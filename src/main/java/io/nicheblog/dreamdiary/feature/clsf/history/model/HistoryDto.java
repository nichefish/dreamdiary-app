package io.nicheblog.dreamdiary.feature.clsf.history.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditRegDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * HistoryDto
 * <pre>
 *  이력 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class HistoryDto
        extends BaseAuditRegDto {

    /** 이력 ID */
    @Positive
    private Integer id;

    /** 참조 글 번호 */
    @Positive
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Size(max = 50)
    private String refContentType;

    private String content;

    @Builder.Default
    private String historyType = HistoryType.CHANGE.key;

    private Integer fromHistoryId;

    private String previewContent;

    /* ----- */

    /**
     * 생성자.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     */
    public HistoryDto(final BaseClsfKey refKey) {
        this.refId = refKey.getId();
        this.refContentType = refKey.getContentType();
    }

    public HistoryDto(final BaseClsfKey refKey, final String content) {
        this(refKey);
        this.content = content;
    }
}
