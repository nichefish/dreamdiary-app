package io.nicheblog.dreamdiary.domain.admin.menu.service.impl;

import io.nicheblog.dreamdiary.domain.admin.menu.entity.MenuEntity;
import io.nicheblog.dreamdiary.domain.admin.menu.mapstruct.MenuMapstruct;
import io.nicheblog.dreamdiary.domain.admin.menu.model.MenuDto;
import io.nicheblog.dreamdiary.domain.admin.menu.model.MenuSearchParam;
import io.nicheblog.dreamdiary.domain.admin.menu.repository.jpa.MenuRepository;
import io.nicheblog.dreamdiary.domain.admin.menu.service.MenuService;
import io.nicheblog.dreamdiary.domain.admin.menu.spec.MenuSpec;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common._clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 메인메뉴(사용자, 관리자, 공통 포함) 목록 조회
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
     * 사이드바 메뉴 (useYn=Y) 조회
     *
     * @return {@link List} 사용 가능한 포털 사이드바 메뉴 목록
     * @throws Exception 목록 조회 중 발생할 수 있는 예외
     */
    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> getUserMenuList() throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(MenuSearchParam.builder()
                .menuTyCd(Constant.MENU_TY_MAIN)
                .useYn("Y")
                .build());
        final Sort sort = Sort.by(Sort.Direction.ASC, "state.sortOrdr");
        return this.getListDto(searchParamMap, sort);
    }

    /**
     * 메인메뉴(관리자) 조회
     *
     * @return {@link Page} 관리자 메인 메뉴 목록을 담고 있는 페이지 객체
     * @throws Exception 목록 조회 중 발생할 수 있는 예외
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MenuDto> getMngrMenuList() throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("menuTyCd", "main");
            put("menuNo", "00000000");
        }};
        final Sort sort = Sort.by(Sort.Direction.ASC, "state.sortOrdr");
        final PageRequest pageRequest = PageRequest.of(0, 99, sort);
        final Page<MenuEntity> menuMainEntityPage = this.getPageEntity(searchParamMap, pageRequest);
        return this.pageEntityToDto(menuMainEntityPage);
    }
}
