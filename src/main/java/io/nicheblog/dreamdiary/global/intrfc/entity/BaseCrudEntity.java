package io.nicheblog.dreamdiary.global.intrfc.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * BaseCrudEntity
 * <pre>
 *  (공통/상속) 기본 CRUD Entity.
 *  "All classes in the hierarchy must be annotated with @SuperBuilder."
 * </pre>
 *
 * @author nichefish
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BaseCrudEntity
        implements Serializable {

    /** 삭제 여부 (Y/N) */
    @Builder.Default
    @Column(name = "del_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("삭제 여부 (Y/N)")
    protected String delYn = "N";
}
