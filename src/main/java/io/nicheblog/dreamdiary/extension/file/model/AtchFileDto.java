package io.nicheblog.dreamdiary.extension.file.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * AtchFileDto
 * <pre>
 *  첨부파일 Dto.
 *  ※첨부파일(atch_file) = 여러 첨부파일을 하나의 단위로 묶어놓은 객체. 첨부파일 상세(atch_file_dtl)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
  */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class AtchFileDto
        extends BaseCrudDto
        implements Identifiable<Integer> {

    /** 첨부파일 번호 (PK) */
    private Integer atchFileNo;

    /** 첨부파일 목록 */
    private List<AtchFileDtlDto> atchFileList;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.atchFileNo;
    }
}
