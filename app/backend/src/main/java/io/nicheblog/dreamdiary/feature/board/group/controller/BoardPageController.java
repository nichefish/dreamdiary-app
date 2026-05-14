package io.nicheblog.dreamdiary.feature.board.group.controller;

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

@Controller
@RequiredArgsConstructor
@Log4j2
public class BoardPageController extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.BOARD_ADMIN_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.BOARD;

    @GetMapping(Url.BOARD_ADMIN_PAGE)
    @Secured({Constant.ROLE_MNGR})
    public String boardList() {
        return "redirect:/vue-app/admin/board-group";
    }
}
