package io.nicheblog.dreamdiary.domain.board.post.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.domain.board.def.model.BoardDefDto;
import io.nicheblog.dreamdiary.domain.board.def.service.BoardDefService;
import io.nicheblog.dreamdiary.domain.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.domain.board.post.model.BoardPostSearchParam;
import io.nicheblog.dreamdiary.domain.board.post.service.BoardPostService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common._clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.event.ViewerAddEvent;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.handler.ViewerEventListener;
import io.nicheblog.dreamdiary.global._common.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.handler.LogActvtyEventListener;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.PaginationInfo;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * BoardPostPageController
 * <pre>
 *  게시판 게시물 페이지 컨트롤러.
 *  화면단에선 boardDef, 어플리케이션 단에선 contentType으로 사용
 * </pre>
 *
 * @see LogActvtyPageControllerAspect
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class BoardPostPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.BOARD_POST_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.BOARD_POST;        // 작업 카테고리 (로그 적재용)

    private final BoardDefService boardDefService;
    private final BoardPostService boardPostService;
    private final DtlCdService dtlCdService;
    private final TagService tagService;

    /**
     * 게시판 게시물 목록 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.BOARD_POST_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostList(
            @ModelAttribute("searchParam") BoardPostSearchParam searchParam,
            final @ModelAttribute("boardDef") String boardDef,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.BOARD);
        model.addAttribute("pageNm", PageNm.LIST);

        // 상세/수정 화면에서 목록 화면 복귀시 세션에 목록 검색 인자 저장해둔 거 있는지 체크
        searchParam = (BoardPostSearchParam) CmmUtils.Param.checkPrevSearchParam(baseUrl, searchParam);
        // 상단 고정 목록 조회
        model.addAttribute("postFxdList", boardPostService.getFxdList(boardDef));
        // 페이징 정보 생성:: 공백시 pageSize=10, pageNo=1
        final PageRequest pageRequest = CmmUtils.Param.getPageRequest(searchParam, "managt.managtDt", model);
        // 목록 조회
        final Page<BoardPostDto.LIST> postList = boardPostService.getPageDto(searchParam, pageRequest);
        model.addAttribute("postList", postList.getContent());
        model.addAttribute(Constant.PAGINATION_INFO, new PaginationInfo(postList));
        // 컨텐츠 타입에 맞는 태그 목록 조회
        model.addAttribute("tagList", tagService.getContentSpecificTagList(boardDef));
        // 코드 정보 모델에 추가
        final BoardDefDto boardDefInfo = boardDefService.getDtlDto(boardDef);
        dtlCdService.setCdListToModel(boardDefInfo.getCtgrClCd(), model);
        // 목록 검색 URL + 파라미터 모델에 추가
        CmmUtils.Param.setModelAttrMap(searchParam, baseUrl, model);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/board/post/board_post_list";
    }

    /**
     * 게시판 게시물 등록 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param boardDef 게시판 정의
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.BOARD_POST_REG_FORM)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostRegForm(
            final @ModelAttribute("boardDef") String boardDef,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.BOARD);
        model.addAttribute("pageNm", PageNm.REG);

        // 빈 객체 주입 (freemarker error prevention)
        model.addAttribute("post", new BoardPostDto());         // 빈 객체 주입 (freemarker error prevention)
        // 등록/수정 화면 플래그 세팅
        model.addAttribute(Constant.FORM_MODE, "regist");
        // 코드 정보 모델에 추가
        final BoardDefDto boardDefInfo = boardDefService.getDtlDto(boardDef);
        dtlCdService.setCdListToModel(boardDefInfo.getCtgrClCd(), model);
        dtlCdService.setCdListToModel(Constant.MDFABLE_CD, model);
        dtlCdService.setCdListToModel(Constant.JANDI_TOPIC_CD, model);
        // CmmUtils.setModelFlsysPath(model);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/board/post/board_post_reg_form";
    }

    /**
     * 게시판 게시물 등록 전 미리보기 팝업 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param boardPost 작성 중인 게시물
     * @param boardDef 게시판 정의
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @PostMapping(Url.BOARD_POST_REG_PREVIEW_POP)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostRegPreviewPop(
            final BoardPostDto boardPost,
            final @ModelAttribute("boardDef") String boardDef,
            final LogActvtyParam logParam,
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.BOARD);
        model.addAttribute("pageNm", PageNm.PREVIEW);

        // 객체 정보 모델에 추가
        boardPost.setMarkdownCn(CmmUtils.markdown(boardPost.getCn()));
        model.addAttribute("post", boardPost);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/board/post/board_post_preview_pop";
    }

    /**
     * 게시판 게시물 상세 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 복합키 식별자
     * @param boardDef 게시판 정의
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see ViewerEventListener
     */
    @GetMapping(value = Url.BOARD_POST_DTL)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostDtl(
            final Integer postNo,
            final @ModelAttribute("boardDef") String boardDef,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.BOARD);
        model.addAttribute("pageNm", PageNm.DTL);

        // 객체 조회 및 모델에 추가
        final BoardPostDto rsDto = boardPostService.getDtlDto(postNo);
        model.addAttribute("post", rsDto);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 조회수 카운트 추가
        // TODO: AOP로 분리
        boardPostService.hitCntUp(postNo);
        // 열람자 추가 :: 메인 로직과 분리
        // TODO: AOP로 분리
        publisher.publishEvent(new ViewerAddEvent(this, rsDto.getClsfKey()));

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/board/post/board_post_dtl";
    }

    /**
     * 게시판 게시물 수정 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 복합키 식별자
     * @param boardDef 게시판 정의
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(value = Url.BOARD_POST_MDF_FORM)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostMdfForm(
            final Integer postNo,
            final @ModelAttribute("boardDef") String boardDef,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.BOARD);
        model.addAttribute("pageNm", PageNm.MDF);

        // 객체 조회 및 모델에 추가
        final BoardPostDto rsDto = boardPostService.getDtlDto(postNo);
        model.addAttribute("post", rsDto);
        // 등록/수정 화면 플래그 세팅
        // 등록/수정 화면 플래그 세팅
        model.addAttribute(Constant.FORM_MODE, "modify");
        // 코드 정보 모델에 추가
        final BoardDefDto boardDefInfo = boardDefService.getDtlDto(boardDef);
        dtlCdService.setCdListToModel(boardDefInfo.getCtgrClCd(), model);
        dtlCdService.setCdListToModel(Constant.MDFABLE_CD, model);
        dtlCdService.setCdListToModel(Constant.JANDI_TOPIC_CD, model);
        // CmmUtils.setModelFlsysPath(model);
        
        final boolean isSuccess = rsDto.getPostNo() != null;
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 로그 관련 세팅
        logParam.setCn("key: " + postNo);
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/board/post/board_post_reg_form";
    }
}
