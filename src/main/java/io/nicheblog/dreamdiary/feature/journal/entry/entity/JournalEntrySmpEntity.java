package io.nicheblog.dreamdiary.feature.journal.entry.entity;

import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterSmpEntity;
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
@Table(name = "journal_entry")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_entry SET deleted_at = NOW() WHERE id = ?")
public class JournalEntrySmpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("journal entry id")
    private Integer id;

    @Column(name = "content_type", nullable = false, length = 32)
    private String contentType;

    @Column(name = "journal_chapter_id")
    private Integer journalChapterId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "journal_chapter_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    private JournalChapterSmpEntity journalChapter;

    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 1")
    private Integer sortOrder;

    @Column(name = "else_dream_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String elseDreamYn;
}
