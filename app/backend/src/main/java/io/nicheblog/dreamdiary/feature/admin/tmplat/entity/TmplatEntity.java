package io.nicheblog.dreamdiary.feature.admin.tmplat.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * TmplatEntity
 * <pre>
 *  저널 엔트리 작성용 사전입력 템플릿 Entity.
 *  전역 공용 평면 단일 목록으로, {@code title} + HTML {@code content} 한 쌍을 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "tmplat")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Where(clause = "deleted_at IS NULL")
public class TmplatEntity
        extends BaseAuditEntity
        implements Usable {

    /** 템플릿 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("템플릿 ID")
    private Integer id;

    /** 제목 (드롭다운 표시명) */
    @Column(name = "title", length = 1000)
    @Comment("제목")
    private String title;

    /** 내용 (에디터에 삽입되는 HTML 본문) */
    @Column(name = "content", columnDefinition = "TEXT")
    @Comment("내용")
    private String content;

    /** 정렬 순서 (드롭다운/목록 노출 순서) */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    /** 사용 여부 (Y/N) */
    @Builder.Default
    @Column(name = "use_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    private String useYn = "Y";
}