package io.nicheblog.dreamdiary.domain.admin.menu.service.impl;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.mapstruct.MenuMapstruct;
import io.nicheblog.dreamdiary.domain.admin.menu.model.MenuDto;
import io.nicheblog.dreamdiary.domain.admin.menu.model.MenuSearchParam;
import io.nicheblog.dreamdiary.domain.admin.menu.repository.jpa.MenuRepository;
import io.nicheblog.dreamdiary.domain.admin.menu.repository.mybatis.MenuMapper;
import io.nicheblog.dreamdiary.domain.admin.menu.service.MenuService;
import io.nicheblog.dreamdiary.domain.admin.menu.spec.MenuSpec;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common._clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.model.SiteAcsInfo;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class MenuServiceImpl
        implements MenuService {

    @Getter
    private final MenuRepository repository;
    @Getter
    private final MenuSpec spec;
    @Getter
    private final MenuMapstruct mapstruct = MenuMapstruct.INSTANCE;

    private final MenuMapper menuMapper;

    private final ApplicationContext context;
    private MenuServiceImpl getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 등록 전처리. (override)
     *
     * @param dto 등록할 객체
     */
    @Override
    public void preRegist(final MenuDto dto) {
        if (dto.getState() == null) dto.setState(new StateCmpstn());
    }

    /**
     * 메인 메뉴(사용자, 관리자, 공통 포함) 목록 조회
     *
     * @param searchParam 검색 파라미터
     * @param pageRequest 페이징 요청 정보를 담고 있는 PageRequest 객체
     * @return {@link Page} 메인 메뉴 목록
     * @throws Exception 목록 조회 중 발생할 수 있는 예외
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MenuDto> getMainMenuList(
            final BaseSearchParam searchParam,
            final PageRequest pageRequest
    ) throws Exception {

        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        searchParamMap.put("menuTyCd", Constant.MENU_TY_MAIN);
        return this.getPageDto(searchParamMap, pageRequest);
    }

    /* ----- */

    /**
     * 사이드바 메뉴 (사용자, useYn=Y) 조회
     *
     * @return {@link List} 사용 가능한 포털 사이드바 메뉴 목록
     * @throws Exception 목록 조회 중 발생할 수 있는 예외
     */
    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> getUserMenuList() throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(MenuSearchParam.builder()
                .menuTyCd(Constant.MENU_TY_MAIN)
                .menuNm("사용자")
                .useYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "state.sortOrdr");
        return this.getListDto(searchParamMap, sort);
    }

    /**
     * 사이드바 메뉴 (관리자, useYn=Y) 조회
     *
     * @return {@link Page} 관리자 메인 메뉴 목록을 담고 있는 페이지 객체
     * @throws Exception 목록 조회 중 발생할 수 있는 예외
     */
    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> getMngrMenuList() throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(MenuSearchParam.builder()
                .menuTyCd(Constant.MENU_TY_MAIN)
                .menuNm("관리자")
                .useYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "state.sortOrdr");
        return this.getListDto(searchParamMap, sort);
    }

    /**
     * 라벨 정보로 메뉴 정보 조회
     *
     * @param label 메뉴 라벨 (컨트롤러에 대정)
     * @return MenuDto
     */
    public MenuDto getMenuByLabel(final SiteMenu label) throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>();
        searchParamMap.put("menuLabel", label.name());
        List<MenuDto> rsMenuList = this.getSelf().getListDto(searchParamMap);
        if (CollectionUtils.isEmpty(rsMenuList)) throw new Exception("메뉴 정보가 없습니다.");
        return this.getSelf().getListDto(searchParamMap).get(0);
    }

    /**
     * 주어진 메뉴 번호가 관리자 메뉴인지 여부를 반환합니다.
     *
     * @param menuNo 메뉴 번호
     * @return Boolean 관리자 메뉴인 경우 true, 그렇지 않은 경우 false
     */
    public Boolean getIsMngrMenu(final Integer menuNo) {
        return "Y".equals(menuMapper.getMngrYn(menuNo));
    }

    /**
     * 메뉴를 사이트 접근 정보로 반환
     * @param menu 메뉴 정보
     * @return SiteAcsInfo
     */
    public SiteAcsInfo getSiteAceInfoFromMenu(final MenuDto menu) {
        return mapstruct.toSiteAcsInfo(menu);
    }
}
