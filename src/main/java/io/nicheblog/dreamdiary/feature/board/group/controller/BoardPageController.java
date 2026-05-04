package io.nicheblog.dreamdiary.feature.board.group.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.type.PageName;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardDto;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardSearchParam;
import io.nicheblog.dreamdiary.feature.board.group.service.BoardService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
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

@Controller
@RequiredArgsConstructor
@Log4j2
public class BoardPageController extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.BOARD_GROUP_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.BOARD_GROUP;

    private final BoardService boardService;

    @GetMapping(Url.BOARD_GROUP_LIST)
    @Secured({Constant.ROLE_MNGR})
    public String boardList(
            @ModelAttribute("searchParam") BoardSearchParam searchParam,
            final ModelMap model
    ) throws Exception {

        model.addAttribute("menuLabel", SiteMenu.BOARD_GROUP);
        model.addAttribute("pageName", PageName.DEFAULT);

        searchParam = (BoardSearchParam) ParamUtils.checkPrevSearchParam(baseUrl, searchParam);
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        final PageRequest pageRequest = ParamUtils.getPageRequest(searchParam, sort, model);
        final Page<BoardDto> boardList = boardService.getPageDto(searchParam, pageRequest);
        model.addAttribute("boardList", boardList.getContent());
        model.addAttribute(Constant.PAGINATION_INFO, new PaginationInfo(boardList));
        ParamUtils.setModelAttrMap(searchParam, baseUrl, model);

        return "/view/feature/board/group/board_list";
    }
}
