package io.nicheblog.dreamdiary.extension.log.stats.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.extension.log.stats.model.LogStatsSearchParam;
import io.nicheblog.dreamdiary.extension.log.stats.model.LogStatsUserDto;
import io.nicheblog.dreamdiary.extension.log.stats.service.LogStatsUserService;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * LogStatsPageController
 * <pre>
 *  로그 관리 > 로그 통계 관리 페이지 컨트롤러.
 * </pre>
 * TODO: 통계 고도화 예정
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class LogStatsPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.LOG_STATS_USER_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LOG_STATS;        // 작업 카테고리 (로그 적재용)

    private final LogStatsUserService logStatsUserService;

    /**
     * 활동 로그 > 활동 로그 목록 (전체) 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.LOG_STATS_USER_LIST)
    @Secured(Constant.ROLE_MNGR)
    public String logStatsUserList(
            @ModelAttribute("searchParam") LogStatsSearchParam searchParam,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.LOG_STATS);
        model.addAttribute("pageNm", PageNm.LIST);

        // 활동 로그 목록 조회
        // 상세/수정 화면에서 목록 화면 복귀시 세션에 목록 검색 인자 저장해둔 거 있는지 체크
        searchParam = (LogStatsSearchParam) CmmUtils.Param.checkPrevSearchParam(baseUrl, searchParam);
        // 목록 조회
        List<LogStatsUserDto> logStatsUserList = logStatsUserService.logStatsUserDtoList(searchParam, Pageable.unpaged());
        if (logStatsUserList != null) model.addAttribute("logStatsUserList", logStatsUserList);
        List<LogStatsUserDto> logStatsNotUserList = logStatsUserService.logStatsNotUserDtoList(searchParam, Pageable.unpaged());
        if (logStatsUserList != null) model.addAttribute("logStatsNotUserList", logStatsNotUserList);
        // 목록 검색 URL + 파라미터 모델에 추가
        CmmUtils.Param.setModelAttrMap(searchParam, baseUrl, model);

        boolean isSuccess = true;
        String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return "/view/domain/admin/log/stats/log_stats_user_list";
    }
}
