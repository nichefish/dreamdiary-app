package io.nicheblog.dreamdiary.feature.file.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * FileGroupDto
 * <pre>
 *  첨부파일 Dto.
 *  ※첨부파일(file_group) = 여러 첨부파일을 하나의 단위로 묶어놓은 객체. 첨부파일 상세(file_record)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
  */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class FileGroupDto
        extends BaseCrudDto
        implements Identifiable<Integer> {

    /** 첨부파일 ID */
    private Integer id;

    /** 첨부파일 목록 */
    private List<FileRecordDto> fileRecordList;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }
}
