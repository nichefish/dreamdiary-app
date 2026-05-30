package io.nicheblog.dreamdiary.feature.journal.day.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * JournalDayMetaEntity
 * <pre>
 *  저널 일자 메타 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "meta")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE meta SET deleted_at = NOW() WHERE id = ?")
public class JournalDayMetaEntity
        extends BaseCrudEntity {

    /** 메타 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("메타 ID")
    private Integer id;

    /** 메타 카테고리 */
    @Column(name = "ctgr")
    @Comment("메타 카테고리")
    private String ctgr;

    /** 메타 */
    @Column(name = "name")
    @Comment("메타")
    private String name;

    /** 저널 일기 메타 */
    @OneToMany(mappedBy = "meta", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    private List<JournalDayMetaContentEntity> journalDayMetaList;
}

