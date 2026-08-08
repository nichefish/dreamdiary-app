package io.nicheblog.dreamdiary.feature.admin.menu.service;

import io.nicheblog.dreamdiary.feature.admin.menu.entity.MenuEntity;
import io.nicheblog.dreamdiary.feature.admin.menu.exception.MenuNotExistsException;
import io.nicheblog.dreamdiary.feature.admin.menu.model.MenuTreeMoveGroupDto;
import io.nicheblog.dreamdiary.feature.admin.menu.model.MenuTreeMoveItemDto;
import io.nicheblog.dreamdiary.feature.admin.menu.model.MenuTreeMoveParam;
import io.nicheblog.dreamdiary.feature.admin.menu.type.MenuType;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SubmenuExpandType;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * MenuTreeService
 * <pre>
 *  메뉴 트리 이동 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@Log4j2
public class MenuTreeService {

    private final MenuService menuService;

    public MenuTreeService(final @Lazy MenuService menuService) {
        this.menuService = menuService;
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

        final MenuEntity movedMenu = menuService.getDtlEntity(moveParam.getMovedId());
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

        final MenuEntity targetParent = menuService.getDtlEntity(targetParentMenuId);
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

                final MenuEntity menu = menuService.getDtlEntity(item.getId());
                if (menu == null) {
                    throw new BusinessException("Menu item does not exist.");
                }
                if (!MenuType.SUB.name().equals(menu.getMenuType())) {
                    throw new BusinessException("Only sub menus can be included in tree move groups.");
                }

                menu.setParentMenuId(parentMenuId);
                menu.setSortOrder(item.getSortOrder());
                menuService.updt(menu);
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

            final MenuEntity currentMenu = menuService.getDtlEntity(currentMenuId);
            if (currentMenu == null) {
                return false;
            }
            currentMenuId = currentMenu.getParentMenuId();
        }
        return false;
    }
}
