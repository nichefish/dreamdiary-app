package io.nicheblog.dreamdiary.feature.jrnl.day.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.feature.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import io.nicheblog.dreamdiary.infrastructure.cd.service.CdLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * JrnlDayPageController
 * <pre>
 *  저널 일자 페이지 Controller.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
public class JrnlDayPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_DAY_WEEKLY;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;

    private final CdLookupService cdLookupService;

    /**
     * 저널 일자 (월간) 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.JRNL_DAY_MONTHLY)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String jrnlDayMonthlyPage(
            final @ModelAttribute("searchParam") JrnlDaySearchParam searchParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JRNL_DAY);
        model.addAttribute("pageNm", PageNm.LIST);

        // URL 파라미터가 전부 존재한다면 그대로 페이지 렌더링
        if (searchParam.getYy() != null && searchParam.getMnth() != null) {
            cdLookupService.setCdListToModel(Code.JRNL_ENTRY_CTGR_CD, model);
            return "/view/feature/jrnl/day/jrnl_day_monthly";
        }

        final String defaultYy = searchParam.getYy() == null ? DateUtils.getCurrYyStr() : searchParam.getYy().toString();
        final String defaultMnth = searchParam.getMnth() == null ? DateUtils.getCurrMnthStr() : searchParam.getMnth().toString();
        return "redirect:" + Url.JRNL_DAY_MONTHLY + "?yy=" + defaultYy + "&mnth=" + defaultMnth;
    }

    /**
     * 저널 일자 (주간) 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.JRNL_DAY_WEEKLY)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String jrnlDayWeeklyPage(
            final @ModelAttribute("searchParam") JrnlDaySearchParam searchParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JRNL_DAY);
        model.addAttribute("pageNm", PageNm.LIST);

        if (StringUtils.isNotBlank(searchParam.getStdrdDt())) {
            model.addAttribute("stdrdDt", searchParam.getStdrdDt());
            cdLookupService.setCdListToModel(Code.JRNL_ENTRY_CTGR_CD, model);
            return "/view/feature/jrnl/day/jrnl_day_weekly";
        }

        final String today = DateUtils.getCurrDateStr(DatePtn.DATE);
        return "redirect:" + Url.JRNL_DAY_WEEKLY + "?stdrdDt=" + today;
    }

    /**
     * 저널 일자 (하루) 화면 조회 (진입점)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.JRNL_DAY_DAILY_VIEW_TODAY)
    public String jrnlDayViewTodayPage() throws Exception {
        final String today = DateUtils.getCurrDateStr(DatePtn.DATE);

        return "redirect:" + Url.JRNL_DAY_WEEKLY + "?stdrdDt=" + today;
    }

    /**
     * 저널 일자 (하루) 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param stdrdDt 기준 일자
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.JRNL_DAY_DAILY_VIEW)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String jrnlDayViewDailyPage(
            final @PathVariable("stdrdDt") String stdrdDt,
            final @ModelAttribute("searchParam") JrnlDaySearchParam searchParam,
            final ModelMap model
    ) {
        return "redirect:" + Url.JRNL_DAY_WEEKLY + "?stdrdDt=" + stdrdDt;
    }
}
