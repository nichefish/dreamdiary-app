package io.nicheblog.dreamdiary.feature.board.post.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * BoardPostPageController
 * <pre>
 *  게시판 게시물 페이지 컨트롤러.
 *  변경(board-3): FTL 렌더 → Vue SPA 리다이렉트로 전환.
 *  contentType(boardKey) 파라미터를 추출하여 /vue-app/board/{contentType} 으로 리다이렉트한다.
 * </pre>
 *
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
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.BOARD_POST;      // 작업 카테고리 (로그 적재용)

    /**
     * 게시판 게시물 목록 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param contentType 게시판 boardKey
     * @return {@link String} -- Vue SPA 리다이렉트
     */
    @GetMapping(Url.BOARD_POST_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostList(
            final @RequestParam(value = "contentType", defaultValue = "") String contentType
    ) {
        return "redirect:/vue-app/board/" + contentType;
    }

    /**
     * 게시판 게시물 등록 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param contentType 게시판 boardKey
     * @return {@link String} -- Vue SPA 리다이렉트
     */
    @GetMapping(Url.BOARD_POST_REGIST_FORM)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostRegistForm(
            final @RequestParam(value = "contentType", defaultValue = "") String contentType
    ) {
        return "redirect:/vue-app/board/" + contentType;
    }

    /**
     * 게시판 게시물 상세 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param contentType 게시판 boardKey
     * @return {@link String} -- Vue SPA 리다이렉트
     */
    @GetMapping(Url.BOARD_POST_DETAIL)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostDetail(
            final @RequestParam(value = "contentType", defaultValue = "") String contentType
    ) {
        return "redirect:/vue-app/board/" + contentType;
    }

    /**
     * 게시판 게시물 수정 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param contentType 게시판 boardKey
     * @return {@link String} -- Vue SPA 리다이렉트
     */
    @GetMapping(Url.BOARD_POST_MODIFY_FORM)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostModifyForm(
            final @RequestParam(value = "contentType", defaultValue = "") String contentType
    ) {
        return "redirect:/vue-app/board/" + contentType;
    }
}
