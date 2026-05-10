package io.nicheblog.dreamdiary.feature.board.group.adapter;

import io.nicheblog.dreamdiary.feature.admin.menu.model.SiteAcsInfo;
import io.nicheblog.dreamdiary.feature.admin.menu.type.PageName;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.board.group.service.BoardService;
import io.nicheblog.dreamdiary.infrastructure.freemarker.model.FreemarkerModelContext;
import io.nicheblog.dreamdiary.infrastructure.freemarker.port.FreemarkerModelContributor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
@RequiredArgsConstructor
public class BoardFreemarkerModelContributor implements FreemarkerModelContributor {

    private final BoardService boardService;

    @Override
    public void contribute(final FreemarkerModelContext context) throws Exception {
        final Object menuLabel = context.getModelAttr("menuLabel");
        if (menuLabel == SiteMenu.BOARD) {
            final String contentType = (String) context.getModelAttr("contentType");
            final SiteAcsInfo acsInfo = boardService.getMenuByBoardKey(contentType);
            final Object pageName = context.getModelAttr("pageName");
            if (pageName instanceof PageName page) {
                acsInfo.setAcsPageInfo(page);
            }
            context.addObject("siteAcsInfo", acsInfo);
        }

        context.addObject("boardMenuList", boardService.boardMenuList());
    }
}
