package io.nicheblog.dreamdiary.feature.board.post.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.type.PageName;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.attachable.tag.service.TagService;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardDto;
import io.nicheblog.dreamdiary.feature.board.group.service.BoardService;
import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostSearchParam;
import io.nicheblog.dreamdiary.feature.board.post.service.BoardPostService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.PaginationInfo;
import io.nicheblog.dreamdiary.infrastructure.web.util.ParamUtils;
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

@Controller
@RequiredArgsConstructor
@Log4j2
public class BoardPostPageController extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.BOARD_POST_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.BOARD_POST;

    private final BoardService boardService;
    private final BoardPostService boardPostService;
    private final CodeLookupService codeLookupService;
    private final TagService tagService;

    @GetMapping(Url.BOARD_POST_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostList(
            @ModelAttribute("searchParam") BoardPostSearchParam searchParam,
            final @ModelAttribute("contentType") String contentType,
            final ModelMap model
    ) throws Exception {

        model.addAttribute("menuLabel", SiteMenu.BOARD);
        model.addAttribute("pageName", PageName.LIST);

        searchParam = (BoardPostSearchParam) ParamUtils.checkPrevSearchParam(baseUrl, searchParam);
        final PageRequest pageRequest = ParamUtils.getPageRequest(searchParam, "createdAt", model);
        final Page<BoardPostDto> postList = boardPostService.getPageDto(searchParam, pageRequest);
        model.addAttribute("postList", postList.getContent());
        model.addAttribute(Constant.PAGINATION_INFO, new PaginationInfo(postList));
        model.addAttribute("tagList", tagService.getContentSpecificTagList(contentType));

        final BoardDto board = boardService.getDtlDtoByBoardKey(contentType);
        codeLookupService.setCdListToModel(board.getCategoryGroupCode(), model);
        ParamUtils.setModelAttrMap(searchParam, baseUrl, model);

        return "/view/feature/board/post/board_post_list";
    }

    @GetMapping(Url.BOARD_POST_REGIST_FORM)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostRegistForm(
            final @ModelAttribute("contentType") String contentType,
            final ModelMap model
    ) throws Exception {

        model.addAttribute("menuLabel", SiteMenu.BOARD);
        model.addAttribute("pageName", PageName.REG);
        model.addAttribute("post", new BoardPostDto());
        model.addAttribute(Constant.FORM_MODE, "regist");

        final BoardDto board = boardService.getDtlDtoByBoardKey(contentType);
        codeLookupService.setCdListToModel(board.getCategoryGroupCode(), model);
        codeLookupService.setCdListToModel(Code.JANDI_TOPIC_CD, model);

        return "/view/feature/board/post/board_post_regist_form";
    }

    @PostMapping(Url.BOARD_POST_REGIST_PREVIEW_POP)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostRegistPreviewPop(
            final BoardPostDto boardPost,
            final @ModelAttribute("contentType") String contentType,
            final ModelMap model
    ) {

        model.addAttribute("menuLabel", SiteMenu.BOARD);
        model.addAttribute("pageName", PageName.PREVIEW);
        model.addAttribute("contentType", contentType);

        boardPost.setMarkdownContent(MarkdownUtils.markdown(boardPost.getContent()));
        model.addAttribute("post", boardPost);

        return "/view/board/post/board_post_preview_pop";
    }

    @GetMapping(value = Url.BOARD_POST_DETAIL)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostDetail(
            final Integer id,
            final @ModelAttribute("contentType") String contentType,
            final ModelMap model
    ) throws Exception {

        model.addAttribute("menuLabel", SiteMenu.BOARD);
        model.addAttribute("pageName", PageName.DTL);
        model.addAttribute("contentType", contentType);

        final BoardPostDto rsDto = boardPostService.viewDtlPage(id);
        model.addAttribute("post", rsDto);

        return "/view/feature/board/post/board_post_detail";
    }

    @GetMapping(value = Url.BOARD_POST_MODIFY_FORM)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String boardPostModifyForm(
            final Integer id,
            final @ModelAttribute("contentType") String contentType,
            final ModelMap model
    ) throws Exception {

        model.addAttribute("menuLabel", SiteMenu.BOARD);
        model.addAttribute("pageName", PageName.MDF);
        model.addAttribute("contentType", contentType);

        final BoardPostDto rsDto = boardPostService.getDtlDto(id);
        model.addAttribute("post", rsDto);
        model.addAttribute(Constant.FORM_MODE, "modify");

        final BoardDto board = boardService.getDtlDtoByBoardKey(contentType);
        codeLookupService.setCdListToModel(board.getCategoryGroupCode(), model);
        codeLookupService.setCdListToModel(Code.JANDI_TOPIC_CD, model);

        return "/view/feature/board/post/board_post_regist_form";
    }
}
