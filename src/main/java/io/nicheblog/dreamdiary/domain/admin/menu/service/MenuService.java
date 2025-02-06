package io.nicheblog.dreamdiary.domain.admin.menu.service;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.entity.MenuEntity;
import io.nicheblog.dreamdiary.domain.admin.menu.mapstruct.MenuMapstruct;
import io.nicheblog.dreamdiary.domain.admin.menu.model.MenuDto;
import io.nicheblog.dreamdiary.domain.admin.menu.repository.jpa.MenuRepository;
import io.nicheblog.dreamdiary.domain.admin.menu.spec.MenuSpec;
import io.nicheblog.dreamdiary.extension.state.service.BaseStateService;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import io.nicheblog.dreamdiary.global.model.SiteAcsInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * MenuService
 * <pre>
 *  메뉴 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
public interface MenuService
        extends BaseCrudService<MenuDto, MenuDto, Integer, MenuEntity, MenuRepository, MenuSpec, MenuMapstruct>,
                BaseStateService<MenuDto, MenuDto, Integer, MenuEntity, MenuRepository, MenuSpec, MenuMapstruct> {

    /**
     * 메인메뉴(사용자, 관리자, 공통 포함) 목록 조회
     *
     * @param searchParam 검색 파라미터
     * @param pageRequest 페이징 요청 정보를 담고 있는 PageRequest 객체
     * @return {@link Page} 메인 메뉴 목록
     * @throws Exception 목록 조회 중 발생할 수 있는 예외
     */
    Page<MenuDto> getMainMenuList(final BaseSearchParam searchParam, final PageRequest pageRequest) throws Exception;

    /**
     * 사이드바 메뉴 (useYn=Y) 조회
     *
     * @return {@link List} 사용 가능한 포털 사이드바 메뉴 목록
     * @throws Exception 목록 조회 중 발생할 수 있는 예외
     */
    List<MenuDto> getUserMenuList() throws Exception;

    /**
     * 메인메뉴(관리자) 조회
     *
     * @return {@link Page} 관리자 메인 메뉴 목록을 담고 있는 페이지 객체
     * @throws Exception 목록 조회 중 발생할 수 있는 예외
     */
    List<MenuDto> getMngrMenuList() throws Exception;

    /**
     * 라벨 정보로 메뉴 정보 조회
     * @param label 메뉴 라벨 (컨트롤러에 대정)
     * @return MenuDto
     */
    MenuDto getMenuByLabel(final SiteMenu label) throws Exception;

    /**
     * 주어진 메뉴 번호가 관리자 메뉴인지 여부를 반환합니다.
     *
     * @param menuNo 메뉴 번호
     * @return Boolean 관리자 메뉴인 경우 true, 그렇지 않은 경우 false
     */
    Boolean getIsMngrMenu(final Integer menuNo);

    /**
     * 메뉴를 사이트 접근 정보로 반환
     * @param menu 메뉴 정보
     * @return SiteAcsInfo
     */
    SiteAcsInfo getSiteAceInfoFromMenu(final MenuDto menu);
}
