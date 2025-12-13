package io.nicheblog.dreamdiary.domain.jrnl.diary.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JrnlDiaryDto
 * <pre>
 *  저널 일기 상태 변경 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class JrnlDiaryPatchDto {

    /** 중요 여부 */
    private Boolean imprtc;
    /** 정리완료 여부 */
    private Boolean resolved;
    /** 글접기 여부 */
    private Boolean collapsed;

    /**
     * 요청이 전부 공백인 경우 판별
     * @return 전부 공백 여부
     */
    public boolean isAllNull() {
        return imprtc == null && collapsed == null && resolved == null;
    }
}
