package io.nicheblog.dreamdiary.feature.admin.menu.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.admin.menu.exception.MenuNotExistsException;
import io.nicheblog.dreamdiary.feature.admin.menu.mapstruct.MenuMapstruct;
import io.nicheblog.dreamdiary.feature.admin.menu.model.*;
import io.nicheblog.dreamdiary.feature.admin.menu.repository.mybatis.MenuMapper;
import io.nicheblog.dreamdiary.feature.admin.menu.type.MenuType;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SubmenuExpandType;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.board.group.jpa.BoardRepository;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MenuSiteMenuService
 * <pre>
 *  사이드바/메타 메뉴 조회 및 BOARD 확장 주입 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@Log4j2
public class MenuSiteMenuService {

    private final MenuService menuService;
    /** BOARD 확장 메뉴의 하위 항목(게시판) 구성용. 게시판은 menu 가 아니라 board 테이블이 소유한다. */
    private final BoardRepository boardRepository;
    private final MenuI18nService menuI18nService;
    private final MenuMapper menuMapper;
    private final MenuMapstruct mapstruct = MenuMapstruct.INSTANCE;
    private final ApplicationContext context;

    public MenuSiteMenuService(
            final @Lazy MenuService menuService,
            final BoardRepository boardRepository,
            final MenuI18nService menuI18nService,
            final MenuMapper menuMapper,
            final ApplicationContext context
    ) {
        this.menuService = menuService;
        this.boardRepository = boardRepository;
        this.menuI18nService = menuI18nService;
        this.menuMapper = menuMapper;
        this.context = context;
    }

    private MenuSiteMenuService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 사이드바 메뉴 (사용자, useYn=Y) 조회
     *
     * @return {@link List} 사용 가능한 포털 사이드바 메뉴 목록
     */
    @Transactional(readOnly = true)
    @Cacheable(value="userMenuList", key="T(org.springframework.context.i18n.LocaleContextHolder).getLocale().getLanguage() + '::' + T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getPermissionCacheKey()")
    public List<MenuDto> getUserMenuList() throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(MenuSearchParam.builder()
                .menuType(MenuType.MAIN.name())
                .adminYn("N")
                .useYn("Y")
                .sidebarVisibleYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");

        return this.filterByRequiredPermission(menuI18nService.localizeMenuTree(this.attachBoardSubMenus(this.filterSidebarVisible(menuService.getListDto(searchParamMap, sort)))));
    }

    /**
     * 사이드바에 표시하지 않는 시스템 메뉴까지 포함한 사용자 메뉴 메타 조회.
     * 화면 breadcrumb/설명 원천으로 사용하며, 사이드바 렌더링에는 사용하지 않는다.
     *
     * @return {@link List} 사용자 메뉴 메타 목록
     */
    @Transactional(readOnly = true)
    @Cacheable(value="userMenuMetaList", key="T(org.springframework.context.i18n.LocaleContextHolder).getLocale().getLanguage() + '::' + T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getPermissionCacheKey()")
    public List<MenuDto> getUserMenuMetaList() throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(MenuSearchParam.builder()
                .menuType(MenuType.MAIN.name())
                .adminYn("N")
                .useYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");

        return this.filterByRequiredPermission(menuI18nService.localizeMenuTree(this.attachBoardSubMenus(menuService.getListDto(searchParamMap, sort))));
    }

    /**
     * 사이드바 메뉴 (관리자, useYn=Y) 조회
     *
     * @return {@link Page} 관리자 메인 메뉴 목록을 담고 있는 페이지 객체
     */
    @Transactional(readOnly = true)
    @Cacheable(value="mngrMenuList", key="T(org.springframework.context.i18n.LocaleContextHolder).getLocale().getLanguage() + '::' + T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getPermissionCacheKey()")
    public List<MenuDto> getMngrMenuList() throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(MenuSearchParam.builder()
                .menuType(MenuType.MAIN.name())
                .adminYn("Y")
                .useYn("Y")
                .sidebarVisibleYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");

        return this.filterByRequiredPermission(menuI18nService.localizeMenuTree(this.attachBoardSubMenus(this.filterSidebarVisible(menuService.getListDto(searchParamMap, sort)))));
    }

    /**
     * 사이드바에 표시하지 않는 시스템 메뉴까지 포함한 관리자 메뉴 메타 조회.
     * 화면 breadcrumb/설명 원천으로 사용하며, 사이드바 렌더링에는 사용하지 않는다.
     *
     * @return {@link List} 관리자 메뉴 메타 목록
     */
    @Transactional(readOnly = true)
    @Cacheable(value="mngrMenuMetaList", key="T(org.springframework.context.i18n.LocaleContextHolder).getLocale().getLanguage() + '::' + T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getPermissionCacheKey()")
    public List<MenuDto> getMngrMenuMetaList() throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(MenuSearchParam.builder()
                .menuType(MenuType.MAIN.name())
                .adminYn("Y")
                .useYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");

        final List<MenuDto> menuList = menuService.getListDto(searchParamMap, sort);
        this.addSharedHiddenUserMenuMeta(menuList);
        return this.filterByRequiredPermission(menuI18nService.localizeMenuTree(this.attachBoardSubMenus(menuList)));
    }

    /**
     * Filter menu tree by required_perm_key against current user permissions.
     * Blank required key passes. Folder nodes with no URL and no remaining children are dropped
     * (except BOARD expand type which may still receive injected boards).
     */
    private List<MenuDto> filterByRequiredPermission(final List<MenuDto> menuList) {
        if (CollectionUtils.isEmpty(menuList)) return menuList;
        final List<MenuDto> filtered = new ArrayList<>();
        for (final MenuDto menu : menuList) {
            final MenuDto node = filterByRequiredPermission(menu);
            if (node != null) filtered.add(node);
        }
        return filtered;
    }


    private MenuDto filterByRequiredPermission(final MenuDto menu) {
        if (menu == null) return null;
        final String required = menu.getRequiredPermKey();
        if (StringUtils.isNotBlank(required) && !Boolean.TRUE.equals(AuthUtils.hasPermission(required))) {
            return null;
        }
        if (CollectionUtils.isNotEmpty(menu.getSubMenuList())) {
            menu.setSubMenuList(this.filterByRequiredPermission(menu.getSubMenuList()));
        }
        final boolean noUrl = StringUtils.isBlank(menu.getUrl());
        final boolean noChildren = CollectionUtils.isEmpty(menu.getSubMenuList());
        if (noUrl && noChildren && menu.getSubmenuExpandType() != null
                && !"NO_SUB".equals(menu.getSubmenuExpandType())
                && !"BOARD".equals(menu.getSubmenuExpandType())) {
            return null;
        }
        return menu;
    }

    private List<MenuDto> filterSidebarVisible(final List<MenuDto> menuList) {
        if (CollectionUtils.isEmpty(menuList)) return menuList;

        final List<MenuDto> filtered = new ArrayList<>();
        for (final MenuDto menu : menuList) {
            final MenuDto filteredMenu = filterSidebarVisible(menu);
            if (filteredMenu != null) filtered.add(filteredMenu);
        }
        return filtered;
    }


    private MenuDto filterSidebarVisible(final MenuDto menu) {
        if (menu == null) return null;
        if (!"Y".equals(menu.getSidebarVisibleYn())) return null;

        final List<MenuDto> subMenuList = menu.getSubMenuList();
        if (CollectionUtils.isNotEmpty(subMenuList)) {
            menu.setSubMenuList(this.filterSidebarVisible(subMenuList));
        }
        return menu;
    }

    /**
     * BOARD 확장 메뉴({@code submenuExpandType=BOARD})에 사용중 게시판을 하위 항목으로 주입한다.
     * <p>
     * 게시판은 menu 테이블이 아니라 board 테이블이 소유하므로 메뉴 트리에 자식 행이 없다.
     * 그래서 주입 전에는 BOARD 메뉴가 자식도 URL 도 없는 빈 메뉴여서, 게시판을 등록해도
     * 사이드바에 아무것도 나타나지 않았다. 프론트는 {@code subMenuList} 가 있어야 아코디언으로 펼친다.
     * <p>
     * 각 게시판은 {@code /app/board/post/list.do?contentType=<boardKey>} URL 을 가진 메뉴로 변환한다.
     * 이 URL 은 프론트 {@code urlMapping} 이 {@code /board/<boardKey>} 라우트로 흡수한다.
     * 게시판 자체는 다국어 대상이 아니므로 {@code localizeMenuTree} 이후에 주입해도 무방하지만,
     * 호출 순서에 의존하지 않도록 지역화 대상에서 제외되는 이름(board_name)을 그대로 쓴다.
     *
     * @param menuList 주입 대상 메뉴 트리 (제자리 수정)
     * @return 입력과 동일 인스턴스
     */
    private List<MenuDto> attachBoardSubMenus(final List<MenuDto> menuList) {
        if (CollectionUtils.isEmpty(menuList)) return menuList;
        if (!this.hasBoardExpandMenu(menuList)) return menuList;

        final List<MenuDto> boardMenus = new ArrayList<>();
        for (final BoardEntity board : boardRepository.findByUseYnOrderBySortOrderAscIdAsc("Y")) {
            if (board == null || StringUtils.isBlank(board.getBoardKey())) continue;
            boardMenus.add(MenuDto.builder()
                    .id(board.getId())
                    .menuType(MenuType.SUB.name())
                    .menuName(board.getBoardName())
                    .url("/app/board/post/list.do?contentType=" + board.getBoardKey())
                    .submenuExpandType(SubmenuExpandType.NO_SUB.name())
                    .sortOrder(board.getSortOrder())
                    .useYn("Y")
                    .sidebarVisibleYn("Y")
                    .build());
        }
        this.applyBoardSubMenus(menuList, boardMenus);
        return menuList;
    }

    /** 트리에 BOARD 확장 메뉴가 하나라도 있는지 (없으면 게시판 조회 자체를 건너뛴다) */
    private boolean hasBoardExpandMenu(final List<MenuDto> menuList) {
        if (CollectionUtils.isEmpty(menuList)) return false;
        for (final MenuDto menu : menuList) {
            if (menu == null) continue;
            if (SubmenuExpandType.BOARD.name().equals(menu.getSubmenuExpandType())) return true;
            if (this.hasBoardExpandMenu(menu.getSubMenuList())) return true;
        }
        return false;
    }

    /** 트리를 재귀 순회하며 BOARD 확장 메뉴의 subMenuList 를 게시판 목록으로 채운다. */
    private void applyBoardSubMenus(final List<MenuDto> menuList, final List<MenuDto> boardMenus) {
        if (CollectionUtils.isEmpty(menuList)) return;
        for (final MenuDto menu : menuList) {
            if (menu == null) continue;
            if (SubmenuExpandType.BOARD.name().equals(menu.getSubmenuExpandType())) {
                menu.setSubMenuList(new ArrayList<>(boardMenus));
                continue;
            }
            this.applyBoardSubMenus(menu.getSubMenuList(), boardMenus);
        }
    }

    /**
     * 관리자 메뉴 모드에서도 공통 계정 화면 breadcrumb 메타를 사용할 수 있도록 숨김 사용자 메뉴를 메타 목록에 포함한다.
     *
     * @param menuList 관리자 메뉴 메타 목록
     */
    private void addSharedHiddenUserMenuMeta(final List<MenuDto> menuList) throws Exception {
        if (menuList == null) return;
        final MenuDto userMyMenu = this.getSelf().getMenuByLabel(SiteMenu.USER_MY);
        if (userMyMenu == null) return;
        if (!"Y".equals(userMyMenu.getProtectedYn()) || !"N".equals(userMyMenu.getSidebarVisibleYn())) {
            log.warn("Shared user menu meta is not protected hidden. menuLabel={}, protectedYn={}, sidebarVisibleYn={}",
                    userMyMenu.getMenuLabel(), userMyMenu.getProtectedYn(), userMyMenu.getSidebarVisibleYn());
        }
        menuList.add(userMyMenu);
    }

    /**
     * 라벨 정보로 메뉴 정보 조회
     *
     * @param label 메뉴 라벨 (컨트롤러에 대정)
     * @return MenuDto
     */
    @Cacheable(value="menuByLabel", key="#label.name()")
    public MenuDto getMenuByLabel(final SiteMenu label) throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>();
        searchParamMap.put("menuLabel", label.name());
        final List<MenuDto> rsMenuList = menuService.getListDto(searchParamMap);
        if (CollectionUtils.isEmpty(rsMenuList)) throw new MenuNotExistsException(MessageUtils.getExceptionMsg("MenuNotExistsException"));

        return rsMenuList.get(0);
    }

    /**
     * 주어진 메뉴 번호가 관리자 메뉴인지 여부를 반환합니다.
     *
     * @param id 메뉴 번호
     * @return Boolean 관리자 메뉴인 경우 true, 그렇지 않은 경우 false
     */
    @Cacheable(value="isMngrMenu", key="#id.toString()")
    public Boolean getIsMngrMenu(final Integer id) {
        return "Y".equals(menuMapper.getAdminYn(id));
    }

    /**
     * 메뉴를 사이트 접근 정보로 반환 (DTO → SiteAcsInfo 매핑)
     * @param menu 메뉴 정보
     * @return SiteAcsInfo
     */
    public SiteAcsInfo getSiteAceInfoFromMenu(final MenuDto menu) {
        return mapstruct.toSiteAcsInfo(menu);
    }
}
