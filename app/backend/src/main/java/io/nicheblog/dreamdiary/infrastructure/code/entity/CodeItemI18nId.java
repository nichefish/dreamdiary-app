package io.nicheblog.dreamdiary.infrastructure.code.entity;

import lombok.*;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * CodeItemI18nId
 * <pre>
 *  code_item_i18n 복합 PK.
 *  (code_item_id, locale) 조합으로 유일성을 보장한다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CodeItemI18nId implements Serializable {

    /** 상세 코드 ID (FK) */
    @Column(name = "code_item_id")
    private Integer codeItemId;

    /** 언어 코드 (en, ja 등) */
    @Column(name = "locale", length = 10)
    private String locale;
}
