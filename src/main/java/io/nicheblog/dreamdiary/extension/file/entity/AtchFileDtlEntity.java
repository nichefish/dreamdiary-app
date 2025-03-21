package io.nicheblog.dreamdiary.extension.file.entity;

import io.nicheblog.dreamdiary.extension.file.mapstruct.AtchFileDtlMapstruct;
import io.nicheblog.dreamdiary.extension.file.model.AtchFileDtlDto;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * AtchFileDtlEntity
 * <pre>
 *  첨부파일 상세 Entity.
 *  ※첨부파일 상세(atch_file_dtl) = 실제 첨부파일 정보를 담고 있는 객체. 첨부파일(atch_file)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "atch_file_dtl")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE atch_file_dtl SET del_yn = 'Y' WHERE atch_file_dtl_no = ?")
public class AtchFileDtlEntity
        extends BaseCrudEntity {

    /** 첨부파일 상세 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "atch_file_dtl_no")
    private Integer atchFileDtlNo;

    /** 첨부파일 정보 */
    @Column(name = "atch_file_no")
    private Integer atchFileNo;

    /** 첨부파일 정보 */
    @ManyToOne
    @JoinColumn(name = "atch_file_no", insertable = false, updatable = false)
    private AtchFileEntity atchFileInfo;

    /** 파일 순번 */
    @Column(name = "file_sn")
    private Integer fileSn;

    /** 원본파일명 */
    @Column(name = "orgn_file_nm", length = 20)
    private String orgnFileNm;

    /** 저장파일명 */
    @Column(name = "stre_file_nm", length = 20)
    private String streFileNm;

    /** 파일 확장자 */
    @Column(name = "file_extn", length = 20)
    private String fileExtn;

    /** 컨텐츠 타입 */
    @Column(name = "content_type", length = 20)
    private String contentType;

    /** 파일 크기 */
    @Column(name = "file_size")
    private Long fileSize;

    /** 파일 저장 경로 */
    @Column(name = "file_stre_path")
    private String fileStrePath;

    /** URL (상대경로) */
    @Column(name = "url")
    private String url;

    /* ----- */

    /**
     * 현재 객체를 Dto로 변환하여 반환한다.
     *
     * @return AtchFileDtlDto -- 변환된 객체
     * @throws Exception 변환 과정에서 발생할 수 있는 예외
     */
    public AtchFileDtlDto asDto() throws Exception {
        return AtchFileDtlMapstruct.INSTANCE.toDto(this);
    }
}
