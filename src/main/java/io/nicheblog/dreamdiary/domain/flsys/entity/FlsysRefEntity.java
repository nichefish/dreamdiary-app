package io.nicheblog.dreamdiary.domain.flsys.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;

/**
 * FlsysRefEntity
 * <pre>
 *  파일시스템 참조 정보 Entity
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "flsys_ref")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE flsys_meta SET del_yn = 'Y' WHERE flsys_ref_id = ?")
public class FlsysRefEntity
        implements Serializable {

    /** 파일시스템 참조 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flsys_ref_no")
    @Comment("파일시스템 참조 번호")
    private Integer flsysRefNo;

    /** 글 번호 */
    @Column(name = "ref_post_no")
    @Comment("글 번호")
    private Integer refPostNo;

    /** 게시판 분류 코드 */
    @Column(name = "ref_content_type")
    @Comment("게시판 분류 코드")
    private String refContentType;

    /** 파일 절대경로 */
    @Column(name = "file_path", length = 500)
    @Comment("파일절대경로")
    private String filePath;
}
