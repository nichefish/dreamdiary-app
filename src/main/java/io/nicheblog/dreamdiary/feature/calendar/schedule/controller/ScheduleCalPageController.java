package io.nicheblog.dreamdiary.feature.calendar.schedule.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleSearchParam;
import io.nicheblog.dreamdiary.feature.user.info.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.info.service.UserService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CdLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * ScheduleCalPageController
 * <pre>
 *  일정 달력 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class ScheduleCalPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.SCHEDULE_CAL;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.SCHEDULE;      // 작업 카테고리 (로그 적재용)

    private final CdLookupService cdLookupService;
    private final UserService userService;

    /**
     * 일정 > 전체 일정 (달력) 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.SCHEDULE_CAL)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String scheduleCal(
            @ModelAttribute("searchParam") ScheduleSearchParam searchParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.SCHEDULE_CAL);
        model.addAttribute("pageNm", PageNm.CAL);

        // 재직자 목록 조회 및 모델에 추가 :: (일정 등록 참가자용)
        final List<UserDto> crtdUserList = userService.getCrdtUserList(DateUtils.getCurrDateAddDayStr(-40), DateUtils.getCurrDateAddDayStr(40));
        model.addAttribute("crtdUserList", crtdUserList);
        // 코드 데이터 모델에 추가
        cdLookupService.setCdListToModel(Code.SCHEDULE_CD, model);
        cdLookupService.setCdListToModel(Code.JANDI_TOPIC_CD, model);

        return "/view/feature/schedule/schedule_cal";
    }
}

