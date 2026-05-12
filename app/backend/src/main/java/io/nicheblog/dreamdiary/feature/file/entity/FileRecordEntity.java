package io.nicheblog.dreamdiary.feature.file.entity;

import io.nicheblog.dreamdiary.feature.file.mapstruct.FileRecordMapstruct;
import io.nicheblog.dreamdiary.feature.file.model.FileRecordDto;
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
 * FileRecordEntity
 * <pre>
 *  첨부파일 상세 Entity.
  *  ※첨부파일 상세(file_record) = 실제 첨부파일 정보를 담고 있는 객체. 첨부파일(file_group)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "file_record")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE file_record SET deleted_at = NOW() WHERE id = ?")
public class FileRecordEntity
        extends BaseCrudEntity {

    /** 첨부파일 상세 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 첨부파일 번호 (FK) */
    @Column(name = "file_group_id")
    private Integer fileGroupId;

    /** 첨부파일 정보 */
    @ManyToOne
    @JoinColumn(name = "file_group_id", insertable = false, updatable = false)
    private FileGroupEntity fileGroupInfo;

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
     * @return FileRecordDto -- 변환된 객체
     */
    public FileRecordDto asDto() throws Exception {
        return FileRecordMapstruct.INSTANCE.toDto(this);
    }
}
