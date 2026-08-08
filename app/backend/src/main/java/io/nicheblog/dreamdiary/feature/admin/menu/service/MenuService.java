package io.nicheblog.dreamdiary.feature.admin.menu.service;

import io.nicheblog.dreamdiary.feature.admin.menu.entity.MenuEntity;
import io.nicheblog.dreamdiary.feature.admin.menu.mapstruct.MenuMapstruct;
import io.nicheblog.dreamdiary.feature.admin.menu.model.*;
import io.nicheblog.dreamdiary.feature.admin.menu.repository.jpa.MenuRepository;
import io.nicheblog.dreamdiary.feature.admin.menu.spec.MenuSpec;
import io.nicheblog.dreamdiary.feature.admin.menu.type.MenuType;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SubmenuExpandType;
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
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * MenuService
 * <pre>
 *  메뉴 관리 서비스 모듈. CRUD·정렬과 함께 사이트 조회({@link MenuSiteMenuService}),
 *  i18n({@link MenuI18nService}), 트리 이동({@link MenuTreeService})을 위임하는 파사드다.
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

    private final MenuI18nService menuI18nService;
    private final MenuSiteMenuService menuSiteMenuService;
    private final MenuTreeService menuTreeService;

    private final ApplicationContext context;
    private MenuService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 메인 메뉴(사용자, 관리자, 공통 포함) 목록 조회. 메뉴 관리 트리 렌더링용.
     * <p>
     * 표시명은 요청 locale 로 지역화한다. 편집 폼은 이 목록이 아니라 상세 조회
     * ({@code GET /menu/{id}}, 지역화하지 않음)를 원천으로 쓰므로, 트리를 번역해도
     * 수정 대상(ko 원본)과 어긋나지 않는다.
     * 캐시가 없어 locale 별 캐시 key 를 둘 필요도 없다.
     *
     * @param searchParam 검색 파라미터
     * @return {@link Page} 메인 메뉴 목록 (요청 locale 로 지역화됨)
     */
    @Transactional(readOnly = true)
    public List<MenuDto> getMainMenuList(
            final BaseSearchParam searchParam,
            final Sort sort
    ) throws Exception {

        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        searchParamMap.put("menuType", MenuType.MAIN.name());
        return menuI18nService.localizeMenuTree(this.getSelf().getListDto(searchParamMap, sort));
    }

    /**
     * 사이드바 메뉴 (사용자, useYn=Y) 조회
     *
     * @return {@link List} 사용 가능한 포털 사이드바 메뉴 목록
     */
    @Transactional(readOnly = true)
    public List<MenuDto> getUserMenuList() throws Exception {
        return menuSiteMenuService.getUserMenuList();
    }

    /**
     * 사이드바에 표시하지 않는 시스템 메뉴까지 포함한 사용자 메뉴 메타 조회.
     * 화면 breadcrumb/설명 원천으로 사용하며, 사이드바 렌더링에는 사용하지 않는다.
     *
     * @return {@link List} 사용자 메뉴 메타 목록
     */
    @Transactional(readOnly = true)
    public List<MenuDto> getUserMenuMetaList() throws Exception {
        return menuSiteMenuService.getUserMenuMetaList();
    }

    /**
     * 사이드바 메뉴 (관리자, useYn=Y) 조회
     *
     * @return {@link Page} 관리자 메인 메뉴 목록을 담고 있는 페이지 객체
     */
    @Transactional(readOnly = true)
    public List<MenuDto> getMngrMenuList() throws Exception {
        return menuSiteMenuService.getMngrMenuList();
    }

    /**
     * 사이드바에 표시하지 않는 시스템 메뉴까지 포함한 관리자 메뉴 메타 조회.
     * 화면 breadcrumb/설명 원천으로 사용하며, 사이드바 렌더링에는 사용하지 않는다.
     *
     * @return {@link List} 관리자 메뉴 메타 목록
     */
    @Transactional(readOnly = true)
    public List<MenuDto> getMngrMenuMetaList() throws Exception {
        return menuSiteMenuService.getMngrMenuMetaList();
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
        return menuI18nService.getMenuI18nList(menuId);
    }

    /**
     * 라벨 정보로 메뉴 정보 조회
     *
     * @param label 메뉴 라벨 (컨트롤러에 대정)
     * @return MenuDto
     */
    public MenuDto getMenuByLabel(final SiteMenu label) throws Exception {
        return menuSiteMenuService.getMenuByLabel(label);
    }

    /**
     * 주어진 메뉴 번호가 관리자 메뉴인지 여부를 반환합니다.
     *
     * @param id 메뉴 번호
     * @return Boolean 관리자 메뉴인 경우 true, 그렇지 않은 경우 false
     */
    public Boolean getIsMngrMenu(final Integer id) {
        return menuSiteMenuService.getIsMngrMenu(id);
    }

    /**
     * 메뉴를 사이트 접근 정보로 반환 (DTO → SiteAcsInfo 매핑)
     * @param menu 메뉴 정보
     * @return SiteAcsInfo
     */
    public SiteAcsInfo getSiteAceInfoFromMenu(final MenuDto menu) {
        return menuSiteMenuService.getSiteAceInfoFromMenu(menu);
    }

    /**
     * 등록 (dto level) override.
     * <p>
     * i18nList 는 폼 전용 필드라 엔티티에 매핑되지 않는다. 상위 {@code regist} 는 후처리에
     * 엔티티에서 만든 {@code updatedDto} 를 넘겨 {@code postRegist} 에서는 폼의 i18nList 를 볼 수 없다.
     * 반대로 폼 DTO({@code registDto})에는 등록 전 id 가 없다. 두 값(폼 i18n + 새 id)을 모두 확보하려면
     * 이 레벨에서 처리해야 한다. 상위 등록으로 새 id 를 받은 뒤 그 id 로 번역을 저장한다.
     *
     * @param registDto 등록할 폼 DTO (i18nList 보유)
     * @return {@link ServiceResponse} 등록 결과
     */
    @Override
    @Transactional
    public ServiceResponse regist(final MenuPostDto registDto) throws Exception {
        final ServiceResponse response = BaseDtoWritableService.super.regist(registDto);
        if (response.getRsltObj() instanceof MenuDto saved && saved.getId() != null) {
            menuI18nService.saveMenuI18n(saved.getId(), registDto.getI18nList());
        }
        return response;
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
        // 폼 값(i18nList)을 가진 postDto 로 저장한다. updatedDto 는 엔티티 기반이라 i18n 이 비어 있다.
        menuI18nService.saveMenuI18n(updatedDto.getId(), postDto.getI18nList());
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
        if (menuSiteMenuService.getIsMngrMenu(updateEntity.getId())) {
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
        return menuTreeService.moveTree(moveParam);
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
