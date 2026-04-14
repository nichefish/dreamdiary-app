package io.nicheblog.dreamdiary.global.entity;

import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Sequence
 * <pre>
 *  (공통) 시퀀스 Entity
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "cmm_sequence")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class SequenceEntity
        implements Serializable {

    /** 시퀀스 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqId")
    private Integer seqId;

    /** 시퀀스 이름 */
    @Column(name = "seq_nm", length = 20)
    private String seqNm;

    /** 시퀀스 값 */
    @Column(name = "seq_val")
    private Integer seqVal;
}
