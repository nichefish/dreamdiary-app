package io.nicheblog.dreamdiary.feature.admin.menu.entity;

import lombok.*;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * MenuI18nId
 * <pre>
 *  menu_i18n 복합 PK.
 *  (menu_id, locale) 조합으로 유일성을 보장한다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MenuI18nId implements Serializable {

    /** 메뉴 ID (FK) */
    @Column(name = "menu_id")
    private Integer menuId;

    /** 언어 코드 (en, ja 등) */
    @Column(name = "locale", length = 10)
    private String locale;
}
