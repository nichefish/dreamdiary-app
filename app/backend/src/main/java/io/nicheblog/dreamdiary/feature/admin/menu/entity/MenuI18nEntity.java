package io.nicheblog.dreamdiary.feature.admin.menu.entity;

import lombok.*;

import javax.persistence.*;

/**
 * MenuI18nEntity
 * <pre>
 *  메뉴 다국어(menu_i18n) Entity.
 *  menu 한 건당 언어별 번역명/설명을 저장한다.
 *  한국어(ko)는 menu.menu_name / menu.menu_description 이 단일 원천이므로 저장하지 않는다.
 *  조회 시 locale 에 맞는 번역이 없으면 menu 의 기본값을 fallback 으로 사용한다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "menu_i18n")
@IdClass(MenuI18nId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuI18nEntity {

    /** 메뉴 ID (복합 PK, FK → menu.id) */
    @Id
    @Column(name = "menu_id")
    private Integer menuId;

    /** 언어 코드 (복합 PK) */
    @Id
    @Column(name = "locale", length = 10)
    private String locale;

    /** 번역된 메뉴명 */
    @Column(name = "menu_name", length = 200, nullable = false)
    private String menuName;

    /** 번역된 메뉴 설명 (선택) */
    @Column(name = "menu_description", length = 1000)
    private String menuDescription;
}
