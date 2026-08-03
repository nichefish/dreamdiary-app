package io.nicheblog.dreamdiary.feature.board.group.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

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
