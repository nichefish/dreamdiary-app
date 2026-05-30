package io.nicheblog.dreamdiary.global.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * NtnCdEntity
 * <pre>
 *  국가코드 Entity
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name="NTN_CD")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NtnCdEntity implements Serializable {

    /** 국가코드 (PK) */
    @Id
    @Column(name="NTN_CD")
    private String ntnCd;

    /** 국가명 */
    @Column(name="NTN_NM")
    private String ntnNm;
}
