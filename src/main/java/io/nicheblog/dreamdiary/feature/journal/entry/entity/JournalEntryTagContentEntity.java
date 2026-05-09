package io.nicheblog.dreamdiary.feature.journal.entry.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagSmpEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tag_content")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE tag_content SET deleted_at = NOW() WHERE id = ?")
public class JournalEntryTagContentEntity
        extends BaseAuditRegEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("tag content id")
    private Integer id;

    @Column(name = "tag_id")
    @Comment("tag id")
    private Integer tagId;

    @Column(name = "ref_id")
    @Comment("reference id")
    private Integer refId;

    @Column(name = "ref_content_type")
    @Comment("reference content type")
    private String refContentType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id", referencedColumnName = "id", updatable = false, insertable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    private TagSmpEntity tag;

    @Transient
    private String name;

    @Transient
    private String ctgr;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ref_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("journal entry")
    private JournalEntrySmpEntity journalEntry;
}
