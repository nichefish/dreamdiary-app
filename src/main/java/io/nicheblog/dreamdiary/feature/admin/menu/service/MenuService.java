package io.nicheblog.dreamdiary.feature.admin.menu.service;

import io.nicheblog.dreamdiary.feature.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.feature.admin.menu.entity.MenuEntity;
import io.nicheblog.dreamdiary.feature.admin.menu.exception.MenuNotExistsException;
import io.nicheblog.dreamdiary.feature.admin.menu.mapstruct.MenuMapstruct;
import io.nicheblog.dreamdiary.feature.admin.menu.model.*;
import io.nicheblog.dreamdiary.feature.admin.menu.repository.jpa.MenuRepository;
import io.nicheblog.dreamdiary.feature.admin.menu.repository.mybatis.MenuMapper;
import io.nicheblog.dreamdiary.feature.admin.menu.spec.MenuSpec;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseSortableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
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
@Service("menuService")
@RequiredArgsConstructor
@Log4j2
public class MenuService
        implements BaseDtoReadableService<MenuDto, Integer, MenuEntity>,
                   BaseDtoWritableService<MenuPostDto, MenuDto, Integer, MenuEntity>,
                   BaseSortableService<MenuSortIdxDto, Integer, MenuEntity> {

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
        searchParamMap.put("menuTyCd", Code.MENU_TY_MAIN);
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
                .menuTyCd(Code.MENU_TY_MAIN)
                .mngrYn("N")
                .useYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "idx");

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
                .menuTyCd(Code.MENU_TY_MAIN)
                .mngrYn("Y")
                .useYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "idx");

        return this.getListDto(searchParamMap, sort);
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
     * @param menuNo 메뉴 번호
     * @return Boolean 관리자 메뉴인 경우 true, 그렇지 않은 경우 false
     */
    @Cacheable(value="isMngrMenu", key="#menuNo.toString()")
    public Boolean getIsMngrMenu(final Integer menuNo) {
        return "Y".equals(menuMapper.getMngrYn(menuNo));
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
        EhCacheUtils.clearCache("isMngrMenu");
        EhCacheUtils.evictCacheByKey("menuByLabel", updatedDto.getMenuLabel());
    }

    /**
     * 상태를 설정한다.
     *
     * @param postNo 대상 게시물 PK
     * @param patchDto 상태 Dto
     * @return collapsedYn 반영 성공 여부를 담은 ServiceResponse
     */
    @Transactional
    public ServiceResponse patch(final Integer postNo, final MenuPatchDto patchDto) throws Exception {
        if (StringUtils.isEmpty(patchDto.getUseYn())) {
            return ServiceResponse.builder()
                    .rslt(false)
                    .message("변경할 항목이 없습니다.")
                    .build();
        }

        return this.getSelf().setUse(postNo, patchDto.getUseYn());
    }
    
    /**
     * 변경 후처리. (override)
     *
     * @param updateEntity - 삭제된 객체
     */
    @Override
    public void postSetUse(final MenuEntity updateEntity) {
        if ("Y".equals(updateEntity.getMngrYn())) {
            EhCacheUtils.clearCache("mngrMenuList");
        } else {
            EhCacheUtils.clearCache("userMenuList");
        }
        EhCacheUtils.evictCacheByKey("isMngrMenu", updateEntity.getMenuNo().toString());
        EhCacheUtils.evictCacheByKey("menuByLabel", updateEntity.getMenuLabel());
    }

    /**
     * 정렬 순서 변경 후처리.
     */
    @Override
    public void postSortIdx(final List<MenuSortIdxDto> idxs) {
        EhCacheUtils.clearCache("mngrMenuList");
        EhCacheUtils.clearCache("userMenuList");
    }

    /**
     * 서브메뉴 부모 이동 + 정렬 반영
     *
     * @param moveParam 이동 payload
     * @return {@link ServiceResponse}
     */
    @Transactional
    public ServiceResponse moveTree(final MenuTreeMoveParam moveParam) throws Exception {
        if (moveParam == null || moveParam.getMovedMenuNo() == null) {
            throw new BusinessException("Moved menu is required.");
        }

        final MenuEntity movedMenu = this.getDtlEntity(moveParam.getMovedMenuNo());
        if (movedMenu == null) {
            throw new MenuNotExistsException(MessageUtils.getExceptionMsg("MenuNotExistsException"));
        }
        if (!Code.MENU_TY_SUB.equals(movedMenu.getMenuTyCd())) {
            throw new BusinessException("Only sub menus can be moved.");
        }
        if ("Y".equals(movedMenu.getProtectedYn())) {
            throw new BusinessException(MessageUtils.getMessage("exception.MenuProtectedException"));
        }
        if (!Objects.equals(movedMenu.getUpperMenuNo(), moveParam.getSourceUpperMenuNo())) {
            throw new BusinessException("Menu tree is stale. Reload and try again.");
        }

        final Integer targetUpperMenuNo = moveParam.getTargetUpperMenuNo();
        if (targetUpperMenuNo == null) {
            throw new BusinessException("Target parent menu is required.");
        }

        final MenuEntity targetParent = this.getDtlEntity(targetUpperMenuNo);
        if (targetParent == null) {
            throw new BusinessException("Target parent menu does not exist.");
        }
        if ("Y".equals(targetParent.getProtectedYn())) {
            throw new BusinessException(MessageUtils.getMessage("exception.MenuProtectedException"));
        }
        if (!Code.MENU_TY_MAIN.equals(targetParent.getMenuTyCd()) && !Code.MENU_TY_SUB.equals(targetParent.getMenuTyCd())) {
            throw new BusinessException("Target parent type is not movable.");
        }
        if (Code.MenuSubExtendTy.NO_SUB.name().equals(targetParent.getMenuSubExtendTyCd())) {
            throw new BusinessException("Target parent does not allow sub menus.");
        }
        if (Objects.equals(movedMenu.getMenuNo(), targetUpperMenuNo) || this.isDescendantOf(targetUpperMenuNo, movedMenu.getMenuNo())) {
            throw new BusinessException("A menu cannot be moved into its own descendant.");
        }

        final LinkedHashMap<Integer, MenuTreeMoveGroupDto> groupMap = this.normalizeMoveGroups(moveParam);
        final MenuTreeMoveGroupDto targetGroup = groupMap.get(targetUpperMenuNo);
        if (targetGroup == null || targetGroup.getItems() == null || targetGroup.getItems().stream().noneMatch(item -> Objects.equals(item.getMenuNo(), movedMenu.getMenuNo()))) {
            throw new BusinessException("Moved menu is missing from the target group.");
        }

        for (final MenuTreeMoveGroupDto group : groupMap.values()) {
            final Integer upperMenuNo = group.getUpperMenuNo();
            final List<MenuTreeMoveItemDto> items = group.getItems();
            if (upperMenuNo == null || items == null) continue;

            for (int idx = 0; idx < items.size(); idx++) {
                final MenuTreeMoveItemDto item = items.get(idx);
                if (item == null || item.getMenuNo() == null) continue;

                final MenuEntity menu = this.getDtlEntity(item.getMenuNo());
                if (menu == null) {
                    throw new BusinessException("Menu item does not exist.");
                }
                if (!Code.MENU_TY_SUB.equals(menu.getMenuTyCd())) {
                    throw new BusinessException("Only sub menus can be included in tree move groups.");
                }

                menu.setUpperMenuNo(upperMenuNo);
                menu.setIdx(idx);
                this.updt(menu);
            }
        }

        EhCacheUtils.clearCache("mngrMenuList");
        EhCacheUtils.clearCache("userMenuList");

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
     * @return upperMenuNo 기준으로 정렬된 그룹 맵
     */
    private LinkedHashMap<Integer, MenuTreeMoveGroupDto> normalizeMoveGroups(final MenuTreeMoveParam moveParam) {
        final LinkedHashMap<Integer, MenuTreeMoveGroupDto> groupMap = new LinkedHashMap<>();
        if (moveParam.getGroups() != null) {
            for (final MenuTreeMoveGroupDto group : moveParam.getGroups()) {
                if (group == null || group.getUpperMenuNo() == null) continue;
                groupMap.put(group.getUpperMenuNo(), group);
            }
        }
        if (moveParam.getSourceUpperMenuNo() != null && !groupMap.containsKey(moveParam.getSourceUpperMenuNo())) {
            final MenuTreeMoveGroupDto sourceGroup = new MenuTreeMoveGroupDto();
            sourceGroup.setUpperMenuNo(moveParam.getSourceUpperMenuNo());
            groupMap.put(sourceGroup.getUpperMenuNo(), sourceGroup);
        }

        return groupMap;
    }

    /**
     * 특정 메뉴가 주어진 조상 메뉴의 하위인지 여부를 검사한다.
     * (트리 순환 방지용)
     *
     * @param menuNo 검사 대상 메뉴
     * @param ancestorMenuNo 조상 후보 메뉴
     * @return true: 하위 노드 / false: 아님
     */
    private boolean isDescendantOf(final Integer menuNo, final Integer ancestorMenuNo) throws Exception {
        Integer currentMenuNo = menuNo;
        while (currentMenuNo != null) {
            if (Objects.equals(currentMenuNo, ancestorMenuNo)) {
                return true;
            }

            final MenuEntity currentMenu = this.getDtlEntity(currentMenuNo);
            if (currentMenu == null) {
                return false;
            }
            currentMenuNo = currentMenu.getUpperMenuNo();
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
            throw new BusinessException(MessageUtils.getMessage("exception.MenuProtectedException"));
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
        searchParamMap.put("upperMenuNo", deletedDto.getMenuNo());
        final List<MenuEntity> subMenuList = this.getSelf().getListEntity(searchParamMap);
        if (CollectionUtils.isNotEmpty(subMenuList)) {
            for (final MenuEntity subMenu : subMenuList) {
                this.getSelf().delete(subMenu.getMenuNo());
            }
        }

        EhCacheUtils.clearCache("userMenuList");
        EhCacheUtils.clearCache("mngrMenuList");
        EhCacheUtils.evictCacheByKey("menuByLabel", deletedDto.getMenuLabel());
    }
}
