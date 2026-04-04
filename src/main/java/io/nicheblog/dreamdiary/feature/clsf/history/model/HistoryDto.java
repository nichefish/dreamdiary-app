package io.nicheblog.dreamdiary.feature.clsf.history.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditRegDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    /** 이력 번호 (PK) */
    @Positive
    private Integer historyNo;

    /** 참조 글 번호 */
    @Positive
    private Integer refPostNo;

    /** 참조 컨텐츠 타입 */
    @Size(max = 50)
    private String refContentType;

    private String cn;

    @Builder.Default
    private String historyType = HistoryType.CHANGE.key;

    private Integer fromHistoryNo;

    private String previewCn;

    /* ----- */

    /**
     * 생성자.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     */
    public HistoryDto(final BaseClsfKey refKey) {
        this.refPostNo = refKey.getPostNo();
        this.refContentType = refKey.getContentType();
    }

    public HistoryDto(final BaseClsfKey refKey, final String cn) {
        this(refKey);
        this.cn = cn;
    }
}
