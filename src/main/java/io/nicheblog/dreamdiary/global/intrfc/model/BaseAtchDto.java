package io.nicheblog.dreamdiary.global.intrfc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nicheblog.dreamdiary.extension.file.model.AtchFileDtlDto;
import io.nicheblog.dreamdiary.extension.file.model.AtchFileDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import java.util.List;

/**
 * BaseAtchDto
 * <pre>
 *  (공통/상속) 첨부파일 정보 추가 Dto.
 *  "All classes in the hierarchy must be annotated with @SuperBuilder."
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class BaseAtchDto
        extends BaseAuditDto {

    /** 첨부파일 번호 */
    @Positive
    protected Integer atchFileNo;

    /** 첨부파일 정보 (1:N) */
    protected AtchFileDto atchFileInfo;

    /** 첨부파일 .zip 다운로드 URL */
    protected String atchFileZipUrl;

    /** 첨부파일 .zip 파일명 */
    @JsonIgnore
    protected String atchFileZipNm;

    /** 첨부파일 존재 여부 */
    protected Boolean hasAtchFile;

    /* ---- */

    /**
     * Getter :: 첨부파일 상세 목록을 반환한다.
     * @return {@link List} -- 첨부파일 싱세 목록.
     */
    public List<AtchFileDtlDto> getAtchFileList() {
        if (this.getAtchFileNo() == null || this.getAtchFileInfo() == null) return null;
        return this.getAtchFileInfo()
                   .getAtchFileList();
    }
}
