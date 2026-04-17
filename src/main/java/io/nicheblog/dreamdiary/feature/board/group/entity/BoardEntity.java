package io.nicheblog.dreamdiary.feature.board.group.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "board")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE board SET deleted_at = NOW() WHERE id = ?")
public class BoardEntity extends BaseAuditEntity implements Usable, Sortable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("board id")
    private Integer id;

    @Column(name = "board_key", length = 30, nullable = false, unique = true)
    @Comment("board key")
    private String boardKey;

    @Column(name = "board_name", length = 120, nullable = false)
    @Comment("board name")
    private String boardName;

    @Column(name = "category_group_code", length = 30)
    @Comment("category group code")
    private String categoryGroupCode;

    @Column(name = "description", length = 2000)
    @Comment("description")
    private String description;

    @Builder.Default
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    @Comment("sort order")
    private Integer sortOrder = 0;

    @Builder.Default
    @Column(name = "use_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    @Comment("use yn")
    private String useYn = "Y";
}
