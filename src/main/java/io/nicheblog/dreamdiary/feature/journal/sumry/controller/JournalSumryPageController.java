package io.nicheblog.dreamdiary.feature.journal.sumry.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.journal.sumry.model.JournalSumrySearchParam;
import io.nicheblog.dreamdiary.feature.journal.sumry.model.JournalSumryDto;
import io.nicheblog.dreamdiary.feature.journal.sumry.service.my.MyJournalSumryService;
import io.nicheblog.dreamdiary.feature.journal.sumry.type.JournalSumrySection;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import io.nicheblog.dreamdiary.infrastructure.cd.service.CdLookupService;
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
 * JournalSumryPageController
 * <pre>
 *  저널 결산 페이지 Controller.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
public class JournalSumryPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_SUMRY_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    private final MyJournalSumryService myJournalSumryService;
    private final CdLookupService cdLookupService;

    /**
     * 저널 결산 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.JOURNAL_SUMRY_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalSumryPage(
            @ModelAttribute("searchParam") JournalSumrySearchParam searchParam,
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JOURNAL_SUMRY);
        model.addAttribute("pageNm", PageNm.LIST);

        // 전체 통계 조회
        final JournalSumryDto totalSumry = myJournalSumryService.getMyTotalSumry();
        model.addAttribute("totalSumry", totalSumry);

        return "/view/feature/journal/sumry/journal_sumry_list";
    }

    /**
     * 저널 결산 상세 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 년도
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(value = Url.JOURNAL_SUMRY_VIEW)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalSumryView(
            final @PathVariable("yy") Integer yy,
            final @RequestParam("section") JournalSumrySection section,
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JOURNAL_SUMRY);
        model.addAttribute("pageNm", PageNm.DTL);

        model.addAttribute("section", section);

        // 코드 데이터 모델에 추가
        cdLookupService.setCdListToModel(Code.JOURNAL_SUMRY_TY_CD, model);

        return "/view/feature/journal/sumry/journal_sumry_dtl";
    }
}

