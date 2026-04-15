package io.nicheblog.dreamdiary.feature.journal.annual.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.journal.annual.model.JournalAnnualSearchParam;
import io.nicheblog.dreamdiary.feature.journal.annual.model.JournalAnnualDto;
import io.nicheblog.dreamdiary.feature.journal.annual.service.my.MyJournalAnnualService;
import io.nicheblog.dreamdiary.feature.journal.annual.type.JournalAnnualSection;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * JournalAnnualPageController
 * <pre>
 *  저널 결산 페이지 Controller.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
public class JournalAnnualPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_ANNUAL_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    private final MyJournalAnnualService myJournalAnnualService;
    private final CodeLookupService codeLookupService;

    /**
     * 저널 결산 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.JOURNAL_ANNUAL_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalAnnualPage(
            @ModelAttribute("searchParam") JournalAnnualSearchParam searchParam,
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JOURNAL_ANNUAL);
        model.addAttribute("pageNm", PageNm.LIST);

        // 전체 통계 조회
        final JournalAnnualDto totalAnnual = myJournalAnnualService.getMyTotalAnnual();
        model.addAttribute("totalAnnual", totalAnnual);

        return "/view/feature/journal/annual/journal_annual_list";
    }

    /**
     * 저널 결산 상세 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 년도
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(value = Url.JOURNAL_ANNUAL_VIEW)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalAnnualView(
            final @PathVariable("yy") Integer yy,
            final @RequestParam("section") JournalAnnualSection section,
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JOURNAL_ANNUAL);
        model.addAttribute("pageNm", PageNm.DTL);

        model.addAttribute("section", section);

        // 코드 데이터 모델에 추가
        codeLookupService.setCdListToModel(Code.JOURNAL_ANNUAL_TY_CD, model);

        return "/view/feature/journal/annual/journal_annual_dtl";
    }
}

