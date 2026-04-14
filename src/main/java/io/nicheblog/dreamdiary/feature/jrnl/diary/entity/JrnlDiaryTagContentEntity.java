package io.nicheblog.dreamdiary.feature.jrnl.diary.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagSmpEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JrnlDiaryTagContentEntity
 * <pre>
 *  저널 일기 태그 Entity. (사용 용이성을 위해 엔티티 분리)
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "tag_content")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "ref_content_type='JRNL_DIARY' AND del_yn='N'")
@SQLDelete(sql = "UPDATE tag_content SET del_yn = 'Y' WHERE id = ?")
public class JrnlDiaryTagContentEntity
        extends BaseAuditRegEntity {

    /** 태그-컨텐츠 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("Tag content ID")
    private Integer id;

    /** 태그 ID */
    @Column(name = "tag_id")
    @Comment("태그 ID")
    private Integer tagId;

    /** 참조 글 번호 */
    @Column(name = "ref_id")
    @Comment("참조 글 번호")
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Column(name = "ref_content_type")
    @Comment("참조 컨텐츠 타입")
    private String refContentType;

    /** 태그 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id", referencedColumnName = "id", updatable = false, insertable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    private TagSmpEntity tag;

    /** 태그 이름 */
    @Transient
    private String tagNm;

    /** 태그 카테고리 */
    @Transient
    private String ctgr;

    /** 참조 컨텐츠 (저널 일기)  */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ref_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 일기 정보")
    private JrnlDiarySmpEntity jrnlDiary;
}
