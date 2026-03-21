package io.nicheblog.dreamdiary.feature.jrnl.day.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.feature.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.Constant;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

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
public class JrnlDayCalPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    /**
     * 저널 달력 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.JRNL_DAY_CAL)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String jrnlDayCal(
            @ModelAttribute("searchParam") JrnlDaySearchParam searchParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.JRNL_DAY);
        model.addAttribute("pageNm", PageNm.CAL);

        // URL 파라미터가 전부 존재한다면 그대로 페이지 렌더링
        if (searchParam.getYy() != null && searchParam.getMnth() != null) {
            return "/view/feature/jrnl/day/jrnl_day_cal";
        }

        // 요건 기본값 생성 (오늘 날짜 기반)
        String defaultYy = searchParam.getYy() == null ? DateUtils.getCurrYyStr() : searchParam.getYy().toString();
        String defaultMnth = searchParam.getMnth() == null ? DateUtils.getCurrMnthStr() : searchParam.getMnth().toString();

        // 없으면 redirect 로 URL 보정
        return "redirect:" + Url.JRNL_DAY_CAL + "?yy=" + defaultYy + "&mnth=" + defaultMnth;
    }
}
