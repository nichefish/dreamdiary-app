package io.nicheblog.dreamdiary.extension.file.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * AtchFileDtlDto
 * <pre>
 *  첨부파일 상세 Dto.
 *  ※첨부파일 상세(atch_file_dtl) = 실제 첨부파일 정보를 담고 있는 객체. 첨부파일(atch_file)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class AtchFileDtlDto
        extends BaseCrudDto
        implements Identifiable<Integer> {

    /** 첨부파일 상세 번호 (PK) */
    private Integer atchFileDtlNo;

    /** 첨부파일 번호 */
    private Integer atchFileNo;

    /** 파일 순번 */
    private String fileSn;

    /** 원본파일명 */
    private String orgnFileNm;

    /** 저장파일명 */
    private String streFileNm;

    /** 파일 확장자 */
    private String fileExtn;

    /** 파일 크기 */
    private Long fileSize;

    /** 파일 경로 */
    private String fileStrePath;

    /**  URL (상대경로) */
    private String url;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.atchFileDtlNo;
    }
}
