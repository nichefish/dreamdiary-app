package io.nicheblog.dreamdiary.feature.admin.menu.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

/**
 * MenuI18nDto
 * <pre>
 *  메뉴 다국어 번역 한 건 (menu_i18n 한 행에 대응).
 *
 *  code_item 다국어는 번역 필드가 {@code codeName} 하나뿐이라 {@code Map<locale, 번역명>} 으로 다루지만,
 *  메뉴는 {@code menuName} 과 {@code menuDescription} 두 필드를 함께 가지므로 레코드 목록으로 다룬다.
 *  맵 두 개로 쪼개면 두 맵의 locale 키 집합이 어긋날 수 있어(예: 설명만 있고 이름이 없는 고아 행)
 *  한 레코드로 묶는다.
 *
 *  폼 바인딩은 인덱스 방식을 사용한다: {@code i18nList[0].locale}, {@code i18nList[0].menuName} ...
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuI18nDto {

    /** 언어 코드 (en, ja 등). ko 는 menu.menu_name 이 기준이라 사용하지 않는다. */
    @Size(max = 10)
    private String locale;

    /** 번역된 메뉴명 */
    @Size(max = 200)
    private String menuName;

    /** 번역된 메뉴 설명 (선택) */
    @Size(max = 1000)
    private String menuDescription;
}
