package io.nicheblog.dreamdiary.global.intrfc.entity;

import io.nicheblog.dreamdiary.extension.file.entity.AtchFileEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

/**
 * BaseAtchEntity
 * <pre>
 *  (공통/상속) 첨부파일 정보 추가 Entity.
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
public class BaseAtchEntity
        extends BaseAuditEntity {

    /** 첨부파일 번호 */
    @Column(name = "atch_file_no")
    protected Integer atchFileNo;

    /** 첨부파일 정보 */
    @OneToOne
    @JoinColumn(name = "atch_file_no", referencedColumnName = "atch_file_no", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    protected AtchFileEntity atchFileInfo;
}

