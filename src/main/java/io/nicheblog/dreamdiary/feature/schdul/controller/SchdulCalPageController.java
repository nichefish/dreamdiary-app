package io.nicheblog.dreamdiary.feature.schdul.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.feature.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.feature.schdul.model.SchdulSearchParam;
import io.nicheblog.dreamdiary.feature.user.info.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.info.service.UserService;
import io.nicheblog.dreamdiary.infrastructure.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
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
 * SchdulCalPageController
 * <pre>
 *  일정 달력 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class SchdulCalPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.SCHDUL_CAL;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.SCHDUL;      // 작업 카테고리 (로그 적재용)

    private final DtlCdService dtlCdService;
    private final UserService userService;

    /**
     * 일정 > 전체 일정 (달력) 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.SCHDUL_CAL)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String schdulCal(
            @ModelAttribute("searchParam") SchdulSearchParam searchParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.SCHDUL_CAL);
        model.addAttribute("pageNm", PageNm.CAL);

        // 재직자 목록 조회 및 모델에 추가 :: (일정 등록 참가자용)
        final List<UserDto> crtdUserList = userService.getCrdtUserList(DateUtils.getCurrDateAddDayStr(-40), DateUtils.getCurrDateAddDayStr(40));
        model.addAttribute("crtdUserList", crtdUserList);
        // 코드 데이터 모델에 추가
        dtlCdService.setCdListToModel(Constant.SCHDUL_CD, model);
        dtlCdService.setCdListToModel(Constant.JANDI_TOPIC_CD, model);

        return "/view/feature/schdul/schdul_cal";
    }
}
