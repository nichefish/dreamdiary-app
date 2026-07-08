package io.nicheblog.dreamdiary.global.intrfc.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

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

    /** 삭제 일시 (NULL이면 미삭제) */
    @Column(name = "deleted_at")
    @Comment("삭제 일시")
    protected LocalDateTime deletedAt;
}
