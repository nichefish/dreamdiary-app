package io.nicheblog.dreamdiary.feature.jrnl.chapter.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.feature.clsf.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDaySmpEntity;
import io.nicheblog.dreamdiary.feature.jrnl.diary.entity.JrnlDiaryEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;

/**
 * JrnlChapterEntity
 * <pre>
 *  저널 챕터 Entity.
 *  Entity that contains each distinct chapter.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "jrnl_chapter")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE jrnl_chapter SET del_yn = 'Y' WHERE id = ?")
public class JrnlChapterEntity
        extends BaseClsfEntity
        implements TagEmbedModule, StateEmbedModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_CHAPTER;

    /** 저널 챕터 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 챕터 고유 번호")
    private Integer id;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JRNL_CHAPTER'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 글분류 코드 :: join을 제거하고 메모리 캐시 처리 */
    @Column(name = "ctgr_cd", length = 50)
    @Comment("저널 일기 글분류 코드 정보")
    private String ctgrCd;

    /** 글분류 코드 이름 :: join을 제거하고 메모리 캐시 처리 */
    @Transient
    private String ctgrNm;

    /* ----- */

    /** 저널 일자 번호  */
    @Column(name = "jrnl_day_id")
    @Comment("저널 일자 번호")
    private Integer jrnlDayId;

    /** 저널 일자 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jrnl_day_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 일자 정보")
    private JrnlDaySmpEntity jrnlDay;

    /** 순번 */
    @Column(name = "idx", columnDefinition = "INT DEFAULT 1")
    private Integer idx;

    /** 저널 일기 목록 */
    @OneToMany(mappedBy = "jrnlChapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("idx ASC")
    @Comment("저널 일기 목록")
    private List<JrnlDiaryEntity> jrnlDiaryList;

    /** 인덱스 변경 여부 */
    @Builder.Default
    @Transient
    private Boolean isIdxChanged = false;

    /* ----- */

    /** 위임 :: 태그 정보 모듈 */
    @Embedded
    public TagEmbed tag;
    /** 위임 :: 상태 정보 모듈 */
    @Embedded
    public StateEmbed state;
}
