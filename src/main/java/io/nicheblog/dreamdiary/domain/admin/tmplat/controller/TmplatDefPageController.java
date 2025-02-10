package io.nicheblog.dreamdiary.domain.admin.tmplat.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.domain.admin.tmplat.model.TmplatDefDto;
import io.nicheblog.dreamdiary.domain.admin.tmplat.model.TmplatDefSearchParam;
import io.nicheblog.dreamdiary.domain.admin.tmplat.service.TmplatDefService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.PaginationInfo;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * TmplatDefPageController
 * <pre>
 *  템플릿 정의 관리 페이지 컨트롤러.
 * </pre>
 * TODO: 신규개발 예정
 *
 * @author nichefish
 * @see LogActvtyPageControllerAspect
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class TmplatDefPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.TMPLAT_DEF_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.TMPLAT;        // 작업 카테고리 (로그 적재용)

    private final TmplatDefService tmplatDefService;

    /**
     * 템플릿 정의 목록 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.TMPLAT_DEF_LIST)
    @Secured({Constant.ROLE_MNGR})
    public String tmplatDefList(
            @ModelAttribute("searchParam") TmplatDefSearchParam searchParam,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.TMPLAT);
        model.addAttribute("pageNm", PageNm.LIST);

        // 상세/수정 화면에서 목록 화면 복귀시 :: 세션에 목록 검색 인자 저장해둔 거 있는지 체크
        searchParam = (TmplatDefSearchParam) CmmUtils.Param.checkPrevSearchParam(baseUrl, searchParam);

        // 목록 조회
        final PageRequest pageRequest = CmmUtils.Param.getPageRequest(searchParam, "regDt", model);
        final Page<TmplatDefDto> tmplatList = tmplatDefService.getPageDto(searchParam, pageRequest);
        model.addAttribute("tmplatList", tmplatList.getContent());
        model.addAttribute(Constant.PAGINATION_INFO, new PaginationInfo(tmplatList));

        // 목록 검색 URL + 파라미터 모델에 추가
        CmmUtils.Param.setModelAttrMap(searchParam, baseUrl, model);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/admin/tmplat/def/tmplat_def_list";
    }
}
