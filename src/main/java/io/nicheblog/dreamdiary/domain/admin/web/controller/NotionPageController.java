package io.nicheblog.dreamdiary.domain.admin.web.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.domain.flsys.model.FlsysSearchParam;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nullable;

/**
 * NotionPageController
 * <pre>
 *  노션 페이지 컨트롤러.
 * </pre>
 * TODO: 보완 예정
 *
 * @author nichefish
 * @see LogActvtyPageControllerAspect
 */
@Controller
@Log4j2
public class NotionPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.NOTION_HOME;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.NOTION;      // 작업 카테고리 (로그 적재용)

    private final String NOTION_PUBLIC = "";

    /**
     * 노션 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * 
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param notionPageIdParam 노션 페이지 ID
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.NOTION_HOME)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String notionPage(
            @ModelAttribute("searchParam") FlsysSearchParam searchParam,
            final @RequestParam("notionPageId") @Nullable String notionPageIdParam,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.MAIN);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // NotionRetriever notionRetriever = new NotionRetriever(notionPageId);
        // String notionPage = notionRetriever.render();
        // model.addAttribute("notionPage", notionPage);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/global/_common/notion/notion_home";
    }
}
