package io.nicheblog.dreamdiary.feature.jrnl.sbjct.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.feature.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.feature.clsf.viewer.handler.ViewerEventListener;
import io.nicheblog.dreamdiary.feature.jrnl.sbjct.model.JrnlSbjctDto;
import io.nicheblog.dreamdiary.feature.jrnl.sbjct.model.JrnlSbjctSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.sbjct.service.JrnlSbjctService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.PaginationInfo;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.infrastructure.web.util.ParamUtils;
import io.nicheblog.dreamdiary.infrastructure.Constant;
import io.nicheblog.dreamdiary.infrastructure.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

/**
 * JrnlSbjctPageController
 * <pre>
 *  저널 주제 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class JrnlSbjctPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_SBJCT_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.NOTICE;      // 작업 카테고리 (로그 적재용)

    private final JrnlSbjctService jrnlSbjctService;
    private final DtlCdService dtlCdService;
    private final TagService tagService;

    /**
     * 저널 주제 목록 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.JRNL_SBJCT_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String jrnlSbjctList(
            @ModelAttribute("searchParam") JrnlSbjctSearchParam searchParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JRNL_SBJCT);
        model.addAttribute("pageNm", PageNm.LIST);

        // 상세/수정 화면에서 목록 화면 복귀시 :: 세션에 목록 검색 인자 저장해둔 거 있는지 체크
        searchParam = (JrnlSbjctSearchParam) ParamUtils.checkPrevSearchParam(baseUrl, searchParam);
        // 상단 고정 목록 조회
        // model.addAttribute("jrnlSbjctFxdList", jrnlSbjctService.getFxdList());
        // 페이징 정보 생성:: 공백시 pageSize=10, pageNo=1
        final PageRequest pageRequest = ParamUtils.getPageRequest(searchParam, "regDt", model);
        // 목록 조회 및 모델에 추가
        final Page<JrnlSbjctDto> jrnlSbjctList = jrnlSbjctService.getPageDto(searchParam, pageRequest);
        model.addAttribute("jrnlSbjctList", jrnlSbjctList.getContent());
        model.addAttribute(Constant.PAGINATION_INFO, new PaginationInfo(jrnlSbjctList));
        // 컨텐츠 타입에 맞는 태그 목록 조회
        model.addAttribute("tagList", tagService.getContentSpecificSizedTagList(ContentType.JRNL_SBJCT));
        // 코드 정보 모델에 추가
        dtlCdService.setCdListToModel(Constant.JRNL_SBJCT_CTGR_CD, model);
        // 목록 검색 URL + 파라미터 모델에 추가
        ParamUtils.setModelAttrMap(searchParam, baseUrl, model);

        return "/view/feature/jrnl/sbjct/jrnl_sbjct_list";
    }

    /**
     * 저널 주제 등록 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.JRNL_SBJCT_REG_FORM)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String jrnlSbjctRegForm(
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JRNL_SBJCT);
        model.addAttribute("pageNm", PageNm.REG);

        // 빈 객체 주입 (freemarker error prevention)
        model.addAttribute("post", new JrnlSbjctDto());
        // 등록/수정 화면 플래그 세팅
        model.addAttribute(Constant.FORM_MODE, "regist");
        // 코드 정보 모델에 추가
        dtlCdService.setCdListToModel(Constant.JRNL_SBJCT_CTGR_CD, model);
        dtlCdService.setCdListToModel(Constant.MDFABLE_CD, model);
        dtlCdService.setCdListToModel(Constant.JANDI_TOPIC_CD, model);

        return "/view/feature/jrnl/sbjct/jrnl_sbjct_reg_form";
    }

    /**
     * 저널 주제 등록 전 미리보기 팝업 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param jrnlSbjct 작성 중인 게시물
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @PostMapping(Url.JRNL_SBJCT_REG_PREVIEW_POP)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String jrnlSbjctRegPreviewPop(
            final @Valid JrnlSbjctDto jrnlSbjct,
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JRNL_SBJCT);
        model.addAttribute("pageNm", PageNm.PREVIEW);

        // 객체 정보 모델에 추가
        jrnlSbjct.setMarkdownCn(MarkdownUtils.markdown(jrnlSbjct.getCn()));
        model.addAttribute("post", jrnlSbjct);

        return "/view/feature/jrnl/sbjct/jrnl_sbjct_preview_pop";
    }

    /**
     * 저널 주제 상세 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @see ViewerEventListener
     */
    @GetMapping(Url.JRNL_SBJCT_DTL)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String jrnlSbjctDtl(
            final @RequestParam("postNo") Integer key,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JRNL_SBJCT);
        model.addAttribute("pageNm", PageNm.DTL);

        // 객체 조회 및 모델에 추가
        final JrnlSbjctDto retrievedDto = jrnlSbjctService.viewDtlPage(key);
        model.addAttribute("post", retrievedDto);

        return "/view/feature/jrnl/sbjct/jrnl_sbjct_dtl";
    }

    /**
     * 저널 주제 수정 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.JRNL_SBJCT_MDF_FORM)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String jrnlSbjctMdfForm(
            final @RequestParam("postNo") Integer key,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JRNL_SBJCT);
        model.addAttribute("pageNm", PageNm.MDF);

        // 객체 조회 및 모델에 추가
        final JrnlSbjctDto retrievedDto = jrnlSbjctService.getDtlDto(key);
        model.addAttribute("post", retrievedDto);
        // 등록/수정 화면 플래그 세팅
        model.addAttribute(Constant.FORM_MODE, "modify");
        // 코드 정보 모델에 추가
        dtlCdService.setCdListToModel(Constant.JRNL_SBJCT_CTGR_CD, model);
        dtlCdService.setCdListToModel(Constant.MDFABLE_CD, model);
        dtlCdService.setCdListToModel(Constant.JANDI_TOPIC_CD, model);

        return "/view/feature/jrnl/sbjct/jrnl_sbjct_reg_form";
    }
}
