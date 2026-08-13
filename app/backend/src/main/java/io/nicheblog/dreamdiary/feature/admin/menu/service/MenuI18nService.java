package io.nicheblog.dreamdiary.feature.admin.menu.service;

import io.nicheblog.dreamdiary.feature.admin.menu.entity.MenuI18nEntity;
import io.nicheblog.dreamdiary.feature.admin.menu.model.MenuDto;
import io.nicheblog.dreamdiary.feature.admin.menu.model.MenuI18nDto;
import io.nicheblog.dreamdiary.feature.admin.menu.repository.jpa.MenuI18nRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MenuI18nService
 * <pre>
 *  메뉴 다국어(지역화) 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MenuI18nService {

    private final MenuI18nRepository menuI18nRepository;

    /** 기준 로케일. 이 로케일의 메뉴명/설명은 menu.menu_name/menu_description 이 단일 원천이라 menu_i18n 에 저장하지 않는다. */
    public static final String BASE_LOCALE = "ko";

    /**
     * 요청 locale({@link LocaleContextHolder}) 기준으로 메뉴 트리의 menuName/menuDescription 을
     * 번역값으로 덮는다. 번역이 없는 메뉴는 기본값(menu_name/menu_description)을 그대로 둔다.
     * <p>
     * 기준 로케일(ko)이면 번역 조회 없이 원본을 반환한다. 사이드바 조회 메서드가 locale 별로 캐시되므로
     * (캐시 key = locale) 이 메서드는 캐시 미스 시에만 실행된다. locale 변경 시 프론트가 메뉴를 재조회해야
     * 새 언어가 반영된다.
     *
     * @param menuList 지역화할 메뉴 트리 (제자리 수정)
     * @return 지역화된 메뉴 트리 (입력과 동일 인스턴스)
     */
    public List<MenuDto> localizeMenuTree(final List<MenuDto> menuList) {
        final String locale = LocaleContextHolder.getLocale().getLanguage();
        if (BASE_LOCALE.equals(locale) || CollectionUtils.isEmpty(menuList)) return menuList;

        final Map<Integer, MenuI18nEntity> i18nMap = new HashMap<>();
        for (final MenuI18nEntity entity : menuI18nRepository.findByLocale(locale)) {
            i18nMap.put(entity.getMenuId(), entity);
        }
        if (i18nMap.isEmpty()) return menuList;

        this.applyMenuLocale(menuList, i18nMap);
        return menuList;
    }

    /** 메뉴 트리를 재귀 순회하며 번역이 있는 노드의 menuName/menuDescription 을 덮는다. */
    void applyMenuLocale(final List<MenuDto> menuList, final Map<Integer, MenuI18nEntity> i18nMap) {
        if (CollectionUtils.isEmpty(menuList)) return;
        for (final MenuDto menu : menuList) {
            if (menu == null) continue;
            final MenuI18nEntity translated = i18nMap.get(menu.getId());
            if (translated != null) {
                if (StringUtils.isNotBlank(translated.getMenuName())) menu.setMenuName(translated.getMenuName());
                if (StringUtils.isNotBlank(translated.getMenuDescription())) menu.setMenuDescription(translated.getMenuDescription());
            }
            this.applyMenuLocale(menu.getSubMenuList(), i18nMap);
        }
    }

    /**
     * 메뉴 다국어 저장. 기존 번역을 전부 삭제하고 전달된 목록으로 교체한다.
     * ko 는 menu.menu_name/menu_description 이 기준이라 저장에서 제외한다.
     * 번역명이 비어 있는 행은 건너뛴다(menu_name 이 NOT NULL 이라 고아 행을 만들지 않기 위함).
     *
     * @param menuId 저장 대상 메뉴 ID
     * @param i18nList 번역 목록 (없으면 전체 삭제만 수행)
     */
    public void saveMenuI18n(final Integer menuId, final List<MenuI18nDto> i18nList) {
        if (menuId == null) return;
        menuI18nRepository.deleteByMenuId(menuId);
        if (CollectionUtils.isEmpty(i18nList)) return;

        for (final MenuI18nDto dto : i18nList) {
            if (dto == null) continue;
            final String locale = StringUtils.trimToNull(dto.getLocale());
            final String menuName = StringUtils.trimToNull(dto.getMenuName());
            if (locale == null || menuName == null) continue;
            if (BASE_LOCALE.equals(locale)) {
                log.warn("[saveMenuI18n] ko 는 menu.menu_name 이 기준이라 i18n 저장에서 제외. menuId={}", menuId);
                continue;
            }
            menuI18nRepository.save(
                    MenuI18nEntity.builder()
                            .menuId(menuId)
                            .locale(locale)
                            .menuName(menuName)
                            .menuDescription(StringUtils.trimToNull(dto.getMenuDescription()))
                            .build()
            );
        }
    }

    /**
     * 메뉴 id 기준 다국어 번역 목록 조회 (관리 화면 상세용).
     * ko 는 기준값이라 결과에 포함되지 않는다.
     *
     * @param menuId 메뉴 ID
     * @return 번역 목록 (없으면 빈 목록)
     */
    @Transactional(readOnly = true)
    public List<MenuI18nDto> getMenuI18nList(final Integer menuId) {
        final List<MenuI18nDto> result = new ArrayList<>();
        if (menuId == null) return result;
        for (final MenuI18nEntity entity : menuI18nRepository.findByMenuId(menuId)) {
            result.add(MenuI18nDto.builder()
                    .locale(entity.getLocale())
                    .menuName(entity.getMenuName())
                    .menuDescription(entity.getMenuDescription())
                    .build());
        }
        return result;
    }
}
