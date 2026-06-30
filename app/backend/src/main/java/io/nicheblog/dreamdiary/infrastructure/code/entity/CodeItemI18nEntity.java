package io.nicheblog.dreamdiary.infrastructure.code.entity;

import lombok.*;

import javax.persistence.*;

/**
 * CodeItemI18nEntity
 * <pre>
 *  상세 코드 다국어(code_item_i18n) Entity.
 *  code_item 한 건당 언어별 번역명을 저장한다.
 *  조회 시 locale 에 맞는 번역이 없으면 code_item.code_name(한국어) 을 fallback 으로 사용한다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "code_item_i18n")
@IdClass(CodeItemI18nId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeItemI18nEntity {

    /** 상세 코드 ID (복합 PK, FK → code_item.id) */
    @Id
    @Column(name = "code_item_id")
    private Integer codeItemId;

    /** 언어 코드 (복합 PK) */
    @Id
    @Column(name = "locale", length = 10)
    private String locale;

    /** 번역된 코드명 */
    @Column(name = "code_name", length = 50, nullable = false)
    private String codeName;
}
