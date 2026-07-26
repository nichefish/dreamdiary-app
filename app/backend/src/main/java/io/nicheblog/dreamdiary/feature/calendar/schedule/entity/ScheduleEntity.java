package io.nicheblog.dreamdiary.feature.calendar.schedule.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbedModule;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ScheduleEntity
 * <pre>
 *  일정 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "schedule")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE schedule SET deleted_at = NOW() WHERE id = ?")
public class ScheduleEntity
        extends BaseAttachableEntity
        implements CommentEmbedModule, TagEmbedModule {

    @PostLoad
    private void onLoad() {
        this.scheduleNm = this.title;
        // 코드 이름 세팅
        if (!CollectionUtils.isEmpty(this.prtcpntList)) {
            this.prtcpntStr = this.prtcpntList.stream()
                    .filter(entity -> entity.getUser() != null)
                    .map(entity -> entity.getUser().getNickname())
                    .collect(Collectors.joining(", "));
        }
    }

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.SCHEDULE;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE.name() + "_CD";

    /** 글 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("공지사항 번호")
    private Integer id;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'SCHEDULE'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /** 글 분류 코드 */
    @Column(name = "category_code", length = 50)
    private String categoryCode;

    /** 글 분류 표시명 :: join을 제거하고 메모리 캐시 처리 */
    @Transient
    private String categoryName;

    /* ----- */

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 내용 */
    @Column(name = "content")
    private String content;

    /* ----- */

    /** 출처 (ex.KASI) */
    @Column(name = "src")
    @Comment("출처 (ex.KASI) ")
    private String src;

    /** 일정 코드 :: join을 제거하고 메모리 캐시 처리 */
    @Column(name = "schedule_cd")
    @Comment("일정분류 코드")
    private String scheduleCd;

    /** 휴가 구분 코드 :: scheduleCd=VCATN일 때만 사용 */
    @Column(name = "vcatn_cd")
    @Comment("휴가 구분 코드")
    private String vcatnCd;

    /** 일정 코드명 :: join을 제거하고 메모리 캐시 처리 */
    @Transient
    private String scheduleNm;

    /** 시작일 */
    @Column(name = "bgn_dt")
    @Comment("시작일")
    private LocalDateTime bgnDt;

    /** 일정 종료일 */
    @Column(name = "end_dt")
    @Comment("종료일")
    private LocalDateTime endDt;

    /** 개인일정 여부 (Y/N) */
    @Builder.Default
    @Column(name = "private_yn")
    @Comment("개인일정 여부 (Y/N)")
    private String privateYn = "N";

    /** 참여자 정보 */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "schedule_id", referencedColumnName = "id")
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("참여자 정보")
    private List<SchedulePrtcpntEntity> prtcpntList;

    @Transient
    private String prtcpntStr;

    /* ----- */

    /** 위임 :: 댓글 정보 모듈 */
    @Embedded
    public CommentEmbed comment;
    /** 위임 :: 태그 정보 모듈 */
    @Embedded
    public TagEmbed tag;
}
