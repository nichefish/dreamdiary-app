package io.nicheblog.dreamdiary.domain.jrnl.entry.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * JrnlEntryPatchDto
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
public class JrnlEntryPatchDto {

    /** 글접기 여부 */
    private Boolean collapsed;

    /**
     * 요청이 전부 공백인 경우 판별
     * @return 전부 공백 여부
     */
    public boolean isAllNull() {
        return collapsed == null;
    }
}
