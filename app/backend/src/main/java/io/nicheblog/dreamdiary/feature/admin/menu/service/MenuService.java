package io.nicheblog.dreamdiary.feature.admin.menu.service;

import io.nicheblog.dreamdiary.feature.admin.menu.entity.MenuEntity;
import io.nicheblog.dreamdiary.feature.admin.menu.exception.MenuNotExistsException;
import io.nicheblog.dreamdiary.feature.admin.menu.mapstruct.MenuMapstruct;
import io.nicheblog.dreamdiary.feature.admin.menu.model.*;
import io.nicheblog.dreamdiary.feature.admin.menu.repository.jpa.MenuRepository;
import io.nicheblog.dreamdiary.feature.admin.menu.repository.mybatis.MenuMapper;
import io.nicheblog.dreamdiary.feature.admin.menu.spec.MenuSpec;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SubmenuExpandType;
import io.nicheblog.dreamdiary.feature.admin.menu.type.MenuType;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseSortableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * MenuService
 * <pre>
 *  메뉴 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MenuService
        implements BaseDtoReadableService<MenuDto, Integer, MenuEntity>,
                   BaseDtoWritableService<MenuPostDto, MenuDto, Integer, MenuEntity>,
                   BaseSortableService<MenuSortOrderDto, Integer, MenuEntity> {

    @Getter
    private final MenuRepository repository;
    @Getter
    private final MenuSpec spec;
    @Getter
    private final MenuMapstruct mapstruct = MenuMapstruct.INSTANCE;

    public MenuMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public MenuMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final MenuMapper menuMapper;

    private final ApplicationContext context;
    private MenuService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 메인 메뉴(사용자, 관리자, 공통 포함) 목록 조회
     *
     * @param searchParam 검색 파라미터
     * @return {@link Page} 메인 메뉴 목록
     */
    @Transactional(readOnly = true)
    public List<MenuDto> getMainMenuList(
            final BaseSearchParam searchParam,
            final Sort sort
    ) throws Exception {

        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        searchParamMap.put("menuType", MenuType.MAIN.name());
        return this.getSelf().getListDto(searchParamMap, sort);
    }

    /* ----- */

    /**
     * 사이드바 메뉴 (사용자, useYn=Y) 조회
     *
     * @return {@link List} 사용 가능한 포털 사이드바 메뉴 목록
     */
    @Transactional(readOnly = true)
    @Cacheable(value="userMenuList")
    public List<MenuDto> getUserMenuList() throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(MenuSearchParam.builder()
                .menuType(MenuType.MAIN.name())
                .adminYn("N")
                .useYn("Y")
                .sidebarVisibleYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");

        return this.filterSidebarVisible(this.getListDto(searchParamMap, sort));
    }

    /**
     * 사이드바에 표시하지 않는 시스템 메뉴까지 포함한 사용자 메뉴 메타 조회.
     * 화면 breadcrumb/설명 원천으로 사용하며, 사이드바 렌더링에는 사용하지 않는다.
     *
     * @return {@link List} 사용자 메뉴 메타 목록
     */
    @Transactional(readOnly = true)
    @Cacheable(value="userMenuMetaList")
    public List<MenuDto> getUserMenuMetaList() throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(MenuSearchParam.builder()
                .menuType(MenuType.MAIN.name())
                .adminYn("N")
                .useYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");

        return this.getListDto(searchParamMap, sort);
    }

    /**
     * 사이드바 메뉴 (관리자, useYn=Y) 조회
     *
     * @return {@link Page} 관리자 메인 메뉴 목록을 담고 있는 페이지 객체
     */
    @Transactional(readOnly = true)
    @Cacheable(value="mngrMenuList")
    public List<MenuDto> getMngrMenuList() throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(MenuSearchParam.builder()
                .menuType(MenuType.MAIN.name())
                .adminYn("Y")
                .useYn("Y")
                .sidebarVisibleYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");

        return this.filterSidebarVisible(this.getListDto(searchParamMap, sort));
    }

    /**
     * 사이드바에 표시하지 않는 시스템 메뉴까지 포함한 관리자 메뉴 메타 조회.
     * 화면 breadcrumb/설명 원천으로 사용하며, 사이드바 렌더링에는 사용하지 않는다.
     *
     * @return {@link List} 관리자 메뉴 메타 목록
     */
    @Transactional(readOnly = true)
    @Cacheable(value="mngrMenuMetaList")
    public List<MenuDto> getMngrMenuMetaList() throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(MenuSearchParam.builder()
                .menuType(MenuType.MAIN.name())
                .adminYn("Y")
                .useYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");

        final List<MenuDto> menuList = this.getListDto(searchParamMap, sort);
        this.addSharedHiddenUserMenuMeta(menuList);
        return menuList;
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
        final List<MenuDto> rsMenuList = this.getSelf().getListDto(searchParamMap);
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

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final MenuDto updatedDto) throws Exception {
        EhCacheUtils.clearCache("userMenuList");
        EhCacheUtils.clearCache("mngrMenuList");
        EhCacheUtils.clearCache("userMenuMetaList");
        EhCacheUtils.clearCache("mngrMenuMetaList");
    }

    /**
     * 등록 전처리. 신규 메뉴는 기존 트리 아래의 SUB 메뉴로만 등록한다.
     *
     * @param registDto 등록할 메뉴 DTO
     */
    @Override
    public void preRegist(final MenuPostDto registDto) throws Exception {
        this.normalizeAndValidateMenuLabel(registDto);
        this.normalizeAndValidateUrl(registDto);

        if (registDto.getParentMenuId() == null) {
            log.warn("Menu registration without parent blocked. requestedMenuType={}", registDto.getMenuType());
            throw new BusinessException("Parent menu is required.");
        }

        final MenuEntity parentMenu = this.findDtlEntity(registDto.getParentMenuId());
        if (parentMenu == null) {
            log.warn("Menu registration under missing parent blocked. parentMenuId={}", registDto.getParentMenuId());
            throw new BusinessException("Target parent menu does not exist.");
        }
        if (!MenuType.MAIN.name().equals(parentMenu.getMenuType()) && !MenuType.SUB.name().equals(parentMenu.getMenuType())) {
            log.warn("Menu registration under invalid parent type blocked. parentMenuId={}, parentMenuType={}",
                    parentMenu.getId(), parentMenu.getMenuType());
            throw new BusinessException("Target parent type does not allow sub menus.");
        }
        if (SubmenuExpandType.NO_SUB.name().equals(parentMenu.getSubmenuExpandType())
                || SubmenuExpandType.BOARD.name().equals(parentMenu.getSubmenuExpandType())) {
            log.warn("Menu registration under non-expandable parent blocked. parentMenuId={}, submenuExpandType={}",
                    parentMenu.getId(), parentMenu.getSubmenuExpandType());
            throw new BusinessException("Target parent does not allow sub menus.");
        }

        registDto.setMenuType(MenuType.SUB.name());
        if (StringUtils.isBlank(registDto.getAdminYn())) {
            registDto.setAdminYn("N");
        }
        if (StringUtils.isBlank(registDto.getSidebarVisibleYn())) {
            registDto.setSidebarVisibleYn("Y");
        }
    }

    /**
     * 수정 전처리. 시스템 보호 메뉴 자체의 수정은 차단한다.
     *
     * @param postDto 수정할 메뉴 DTO
     * @param modifyEntity 수정 대상 메뉴 엔티티
     */
    @Override
    public void preModify(final MenuPostDto postDto, final MenuEntity modifyEntity) throws Exception {
        this.normalizeAndValidateMenuLabel(postDto);
        this.normalizeAndValidateUrl(postDto);

        if ("Y".equals(modifyEntity.getProtectedYn())) {
            log.warn("Protected menu modification blocked. menuId={}", modifyEntity.getId());
            throw new BusinessException(MessageUtils.getMessage("exception.menu-protected"));
        }
        if (StringUtils.isNotBlank(postDto.getMenuType()) && !Objects.equals(postDto.getMenuType(), modifyEntity.getMenuType())) {
            log.warn("Menu type modification blocked. menuId={}, currentMenuType={}, requestedMenuType={}",
                    modifyEntity.getId(), modifyEntity.getMenuType(), postDto.getMenuType());
            throw new BusinessException("Menu type cannot be changed.");
        }
        if (!Objects.equals(postDto.getParentMenuId(), modifyEntity.getParentMenuId())) {
            log.warn("Menu parent modification blocked outside tree move API. menuId={}, currentParentMenuId={}, requestedParentMenuId={}",
                    modifyEntity.getId(), modifyEntity.getParentMenuId(), postDto.getParentMenuId());
            throw new BusinessException(MessageUtils.getMessage("exception.menu-parent-change-blocked"));
        }
    }

    /**
     * 메뉴 라벨 필수 입력을 보장하고 저장 전 공백을 제거한다.
     *
     * @param postDto 등록/수정 요청 DTO
     */
    private void normalizeAndValidateMenuLabel(final MenuPostDto postDto) {
        final String menuLabel = StringUtils.trimToNull(postDto.getMenuLabel());
        if (menuLabel == null) {
            log.warn("Menu label validation failed. menuId={}", postDto.getId());
            throw new BusinessException("Menu label is required.");
        }
        postDto.setMenuLabel(menuLabel);
    }

    /**
     * 최종 이동 메뉴는 URL 필수 입력을 보장하고, 확장형 메뉴는 URL을 저장하지 않는다.
     *
     * @param postDto 등록/수정 요청 DTO
     */
    private void normalizeAndValidateUrl(final MenuPostDto postDto) {
        if (!SubmenuExpandType.NO_SUB.name().equals(postDto.getSubmenuExpandType())) {
            postDto.setUrl("");
            return;
        }

        final String url = StringUtils.trimToNull(postDto.getUrl());
        if (url == null) {
            log.warn("Menu URL validation failed. menuId={}, submenuExpandType={}", postDto.getId(), postDto.getSubmenuExpandType());
            throw new BusinessException("Menu URL is required.");
        }
        postDto.setUrl(url);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final MenuPostDto postDto, final MenuDto updatedDto) throws Exception {
        EhCacheUtils.clearCache("userMenuList");
        EhCacheUtils.clearCache("mngrMenuList");
        EhCacheUtils.clearCache("userMenuMetaList");
        EhCacheUtils.clearCache("mngrMenuMetaList");
        EhCacheUtils.clearCache("isMngrMenu");
        EhCacheUtils.evictCacheByKey("menuByLabel", updatedDto.getMenuLabel());
    }

    /**
     * 상태를 설정한다.
     *
     * @param id 대상 게시물 PK
     * @param patchDto 상태 Dto
     * @return collapsedYn 반영 성공 여부를 담은 ServiceResponse
     */
    @Transactional
    public ServiceResponse patch(final Integer id, final MenuPatchDto patchDto) throws Exception {
        if (StringUtils.isEmpty(patchDto.getUseYn())) {
            return ServiceResponse.builder()
                    .rslt(false)
                    .message(MessageUtils.getMessage("common.result.no-changes"))
                    .build();
        }

        final MenuEntity menu = this.getDtlEntity(id);
        if ("Y".equals(menu.getProtectedYn())) {
            log.warn("Protected menu useYn change blocked. menuId={}", id);
            throw new BusinessException(MessageUtils.getMessage("exception.menu-protected"));
        }

        return this.getSelf().setUse(id, patchDto.getUseYn());
    }
    
    /**
     * 변경 후처리. (override)
     *
     * @param updateEntity - 삭제된 객체
     */
    @Override
    public void postSetUse(final MenuEntity updateEntity) {
        if (this.getIsMngrMenu(updateEntity.getId())) {
            EhCacheUtils.clearCache("mngrMenuList");
            EhCacheUtils.clearCache("mngrMenuMetaList");
        } else {
            EhCacheUtils.clearCache("userMenuList");
            EhCacheUtils.clearCache("userMenuMetaList");
        }
        EhCacheUtils.evictCacheByKey("isMngrMenu", updateEntity.getId().toString());
        EhCacheUtils.evictCacheByKey("menuByLabel", updateEntity.getMenuLabel());
    }

    /**
     * 정렬 순서 변경 후처리.
     */
    @Override
    public void postSortOrder(final List<MenuSortOrderDto> sortOrders) {
        EhCacheUtils.clearCache("mngrMenuList");
        EhCacheUtils.clearCache("userMenuList");
        EhCacheUtils.clearCache("mngrMenuMetaList");
        EhCacheUtils.clearCache("userMenuMetaList");
    }

    /**
     * 서브메뉴 부모 이동 + 정렬 반영
     *
     * @param moveParam 이동 payload
     * @return {@link ServiceResponse}
     */
    @Transactional
    public ServiceResponse moveTree(final MenuTreeMoveParam moveParam) throws Exception {
        if (moveParam == null || moveParam.getMovedId() == null) {
            throw new BusinessException("Moved menu is required.");
        }

        final MenuEntity movedMenu = this.getDtlEntity(moveParam.getMovedId());
        if (movedMenu == null) {
            throw new MenuNotExistsException(MessageUtils.getExceptionMsg("MenuNotExistsException"));
        }
        if (!MenuType.SUB.name().equals(movedMenu.getMenuType())) {
            throw new BusinessException("Only sub menus can be moved.");
        }
        if ("Y".equals(movedMenu.getProtectedYn())) {
            throw new BusinessException(MessageUtils.getMessage("exception.menu-protected"));
        }
        if (!Objects.equals(movedMenu.getParentMenuId(), moveParam.getSourceParentMenuId())) {
            throw new BusinessException("Menu tree is stale. Reload and try again.");
        }

        final Integer targetParentMenuId = moveParam.getTargetParentMenuId();
        if (targetParentMenuId == null) {
            throw new BusinessException("Target parent menu is required.");
        }

        final MenuEntity targetParent = this.getDtlEntity(targetParentMenuId);
        if (targetParent == null) {
            throw new BusinessException("Target parent menu does not exist.");
        }
        if (!MenuType.MAIN.name().equals(targetParent.getMenuType()) && !MenuType.SUB.name().equals(targetParent.getMenuType())) {
            throw new BusinessException("Target parent type is not movable.");
        }
        if (SubmenuExpandType.NO_SUB.name().equals(targetParent.getSubmenuExpandType())
                || SubmenuExpandType.BOARD.name().equals(targetParent.getSubmenuExpandType())) {
            throw new BusinessException("Target parent does not allow sub menus.");
        }
        if (Objects.equals(movedMenu.getId(), targetParentMenuId) || this.isDescendantOf(targetParentMenuId, movedMenu.getId())) {
            throw new BusinessException("A menu cannot be moved into its own descendant.");
        }

        final LinkedHashMap<Integer, MenuTreeMoveGroupDto> groupMap = this.normalizeMoveGroups(moveParam);
        final MenuTreeMoveGroupDto targetGroup = groupMap.get(targetParentMenuId);
        if (targetGroup == null || targetGroup.getItems() == null || targetGroup.getItems().stream().noneMatch(item -> Objects.equals(item.getId(), movedMenu.getId()))) {
            throw new BusinessException("Moved menu is missing from the target group.");
        }

        for (final MenuTreeMoveGroupDto group : groupMap.values()) {
            final Integer parentMenuId = group.getParentMenuId();
            final List<MenuTreeMoveItemDto> items = group.getItems();
            if (parentMenuId == null || items == null) continue;

            for (final MenuTreeMoveItemDto item : items) {
                if (item == null || item.getId() == null) continue;

                final MenuEntity menu = this.getDtlEntity(item.getId());
                if (menu == null) {
                    throw new BusinessException("Menu item does not exist.");
                }
                if (!MenuType.SUB.name().equals(menu.getMenuType())) {
                    throw new BusinessException("Only sub menus can be included in tree move groups.");
                }

                menu.setParentMenuId(parentMenuId);
                menu.setSortOrder(item.getSortOrder());
                this.updt(menu);
            }
        }

        EhCacheUtils.clearCache("mngrMenuList");
        EhCacheUtils.clearCache("userMenuList");
        EhCacheUtils.clearCache("mngrMenuMetaList");
        EhCacheUtils.clearCache("userMenuMetaList");
        EhCacheUtils.clearCache("isMngrMenu");
        log.info("Menu tree moved. movedId={}, sourceParentMenuId={}, targetParentMenuId={}",
                moveParam.getMovedId(), moveParam.getSourceParentMenuId(), moveParam.getTargetParentMenuId());

        return ServiceResponse.builder()
                .rslt(true)
                .build();
    }

    /**
     * 메뉴 트리 이동 요청의 그룹 데이터를 정규화한다.
     * - null / invalid group 제거
     * - source 그룹이 누락된 경우 보정하여 추가
     *
     * @param moveParam 이동 요청 파라미터
     * @return parentMenuId 기준으로 정렬된 그룹 맵
     */
    private LinkedHashMap<Integer, MenuTreeMoveGroupDto> normalizeMoveGroups(final MenuTreeMoveParam moveParam) {
        final LinkedHashMap<Integer, MenuTreeMoveGroupDto> groupMap = new LinkedHashMap<>();
        if (moveParam.getGroups() != null) {
            for (final MenuTreeMoveGroupDto group : moveParam.getGroups()) {
                if (group == null || group.getParentMenuId() == null) continue;
                groupMap.put(group.getParentMenuId(), group);
            }
        }
        if (moveParam.getSourceParentMenuId() != null && !groupMap.containsKey(moveParam.getSourceParentMenuId())) {
            final MenuTreeMoveGroupDto sourceGroup = new MenuTreeMoveGroupDto();
            sourceGroup.setParentMenuId(moveParam.getSourceParentMenuId());
            groupMap.put(sourceGroup.getParentMenuId(), sourceGroup);
        }

        return groupMap;
    }

    /**
     * 특정 메뉴가 주어진 조상 메뉴의 하위인지 여부를 검사한다.
     * (트리 순환 방지용)
     *
     * @param id 검사 대상 메뉴
     * @param ancestorMenuId 조상 후보 메뉴
     * @return true: 하위 노드 / false: 아님
     */
    private boolean isDescendantOf(final Integer id, final Integer ancestorMenuId) throws Exception {
        Integer currentMenuId = id;
        while (currentMenuId != null) {
            if (Objects.equals(currentMenuId, ancestorMenuId)) {
                return true;
            }

            final MenuEntity currentMenu = this.getDtlEntity(currentMenuId);
            if (currentMenu == null) {
                return false;
            }
            currentMenuId = currentMenu.getParentMenuId();
        }
        return false;
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deleteEntity - 삭제된 객체
     */
    @Override
    public void preRemove(final MenuEntity deleteEntity) throws Exception {
        if ("Y".equals(deleteEntity.getProtectedYn())) {
            // 하위 메뉴 중 하나라도 보호라면 전체 롤백
            throw new BusinessException(MessageUtils.getMessage("exception.menu-protected"));
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final MenuDto deletedDto) throws Exception {
        // 서브메뉴 삭제. (재귀)
        final Map<String, Object> searchParamMap = new HashMap<>();
        searchParamMap.put("parentMenuId", deletedDto.getId());
        final List<MenuEntity> subMenuList = this.getSelf().getListEntity(searchParamMap);
        if (CollectionUtils.isNotEmpty(subMenuList)) {
            for (final MenuEntity subMenu : subMenuList) {
                this.getSelf().delete(subMenu.getId());
            }
        }

        EhCacheUtils.clearCache("userMenuList");
        EhCacheUtils.clearCache("mngrMenuList");
        EhCacheUtils.clearCache("userMenuMetaList");
        EhCacheUtils.clearCache("mngrMenuMetaList");
        EhCacheUtils.clearCache("isMngrMenu");
        EhCacheUtils.evictCacheByKey("menuByLabel", deletedDto.getMenuLabel());
    }
}
