package io.nicheblog.dreamdiary.domain.board.notice.entity;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.global.intrfc.entity.BasePostEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * NoticeSmpEntity
 * <pre>
 *  공지사항 간소화 Entity
 *  (NoticeEntity 태그 관련 정보 제거 = 연관관계 순환참조 방지 위함. 나머지는 동일)
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "notice")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE notice SET del_yn = 'Y' WHERE post_no = ?")
public class NoticeSmpEntity
        extends BasePostEntity {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.NOTICE;

    /** 글 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("공지사항 번호")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'NOTICE'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /** 팝업 노출 여부 (Y/N) */
    @Builder.Default
    @Column(name = "popup_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("팝업 노출여부")
    private String popupYn = "N";
}
