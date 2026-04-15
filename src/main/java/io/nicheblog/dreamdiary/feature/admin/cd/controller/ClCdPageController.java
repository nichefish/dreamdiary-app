package io.nicheblog.dreamdiary.feature.admin.cd.controller;

import io.nicheblog.dreamdiary.feature.admin.cd.model.ClCdDto;
import io.nicheblog.dreamdiary.feature.admin.cd.model.ClCdSearchParam;
import io.nicheblog.dreamdiary.feature.admin.cd.service.ClCdService;
import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import io.nicheblog.dreamdiary.infrastructure.cd.service.CdLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.PaginationInfo;
import io.nicheblog.dreamdiary.infrastructure.web.util.ParamUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * ClCdPageController
 * <pre>
 *  분류 코드 정보 관리 페이지 컨트롤러.
 *  ※분류 코드(cl_cd) = 상위 분류 코드. 상세 코드(dtl_cd)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class ClCdPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.CL_CD_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.CD;        // 작업 카테고리 (로그 적재용)

    private final ClCdService clCdService;
    private final CdLookupService cdLookupService;

    /**
     * 분류 코드(CL_CD) 관리(useYn=N 포함) 목록 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.CL_CD_LIST)
    @Secured({Constant.ROLE_MNGR})
    public String clCdList(
            @ModelAttribute("searchParam") ClCdSearchParam searchParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.CD);
        model.addAttribute("pageNm", PageNm.LIST);

        // 상세/수정 화면에서 목록 화면 복귀시 세션에 목록 검색 인자 저장해둔 거 있는지 체크
        searchParam = (ClCdSearchParam) ParamUtils.checkPrevSearchParam(baseUrl, searchParam);
        // 페이징 정보 생성:: 공백시 pageSize=10, pageNo=1
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        final PageRequest pageRequest = ParamUtils.getPageRequest(searchParam, sort, model);
        // 목록 조회
        final Page<ClCdDto> clCdList = clCdService.getPageDto(searchParam, pageRequest);
        model.addAttribute("clCdList", clCdList.getContent());
        model.addAttribute(Constant.PAGINATION_INFO, new PaginationInfo(clCdList));
        // 목록 검색 URL + 파라미터 모델에 추가
        ParamUtils.setModelAttrMap(searchParam, baseUrl, model);
        // 코드 데이터 모델에 추가
        cdLookupService.setCdListToModel(Code.CL_CTGR_CD, model);

        return "/view/feature/admin/cd/cl_cd_list";
    }
}
