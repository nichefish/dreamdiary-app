package io.nicheblog.dreamdiary.domain.board.def.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.domain.board.def.model.BoardDefDto;
import io.nicheblog.dreamdiary.domain.board.def.model.BoardDefSearchParam;
import io.nicheblog.dreamdiary.domain.board.def.service.BoardDefService;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.PaginationInfo;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
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
 * BoardDefPageController
 * <pre>
 *  게시판 정의 관리 페이지 컨트롤러.
 *  ※게시판 정의(board_def) = 게시판 분류. 게시판 게시물(board_post)을 1:N으로 관리한다.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyPageControllerAspect
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class BoardDefPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.BOARD_DEF_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.BOARD_DEF;        // 작업 카테고리 (로그 적재용)

    private final BoardDefService boardDefService;

    /**
     * 게시판 정의 목록 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면의 뷰 이름
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.BOARD_DEF_LIST)
    @Secured({Constant.ROLE_MNGR})
    public String boardDefList(
            @ModelAttribute("searchParam") BoardDefSearchParam searchParam,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.BOARD_DEF);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // 상세/수정 화면에서 목록 화면 복귀시 세션에 목록 검색 인자 저장해둔 거 있는지 체크
        searchParam = (BoardDefSearchParam) CmmUtils.Param.checkPrevSearchParam(baseUrl, searchParam);
        // 페이징 정보 생성:: 공백시 pageSize=10, pageNo=1
        final Sort sort = Sort.by(Sort.Direction.ASC, "state.sortOrdr");
        final PageRequest pageRequest = CmmUtils.Param.getPageRequest(searchParam, sort, model);
        // 목록 조회
        final Page<BoardDefDto> boardDefMngList = boardDefService.getPageDto(searchParam, pageRequest);
        model.addAttribute("boardDefMngList", boardDefMngList.getContent());
        model.addAttribute(Constant.PAGINATION_INFO, new PaginationInfo(boardDefMngList));

        // 목록 검색 URL + 파라미터 모델에 추가
        CmmUtils.Param.setModelAttrMap(searchParam, baseUrl, model);
        // 코드 정보 모델에 추가

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/board/def/board_def_list";
    }
}
