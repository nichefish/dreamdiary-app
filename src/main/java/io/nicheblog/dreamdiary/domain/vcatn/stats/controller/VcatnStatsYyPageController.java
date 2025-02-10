package io.nicheblog.dreamdiary.domain.vcatn.stats.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.domain.vcatn.papr.service.VcatnPaprService;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsYyDto;
import io.nicheblog.dreamdiary.domain.vcatn.stats.service.VcatnStatsYyService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nullable;

/**
 * VcatnStatsYyPageController
 * <pre>
 *  휴가관리 > 년도별 휴가관리 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyPageControllerAspect
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class VcatnStatsYyPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.VCATN_STATS_YY;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.VCATN_STATS;      // 작업 카테고리 (로그 적재용)

    private final VcatnPaprService vcatnPaprService;
    private final VcatnStatsYyService vcatnStatsYyService;

    /**
     * 휴가관리 > 년도별 휴가관리 > 목록 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.VCATN_STATS_YY)
    @Secured(Constant.ROLE_MNGR)
    public String vcatnStatsList(
            final @RequestParam("statsYy") @Nullable String yyStrParam,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.VCATN_STATS);
        model.addAttribute("pageNm", PageNm.CAL);

        // 휴가계획서 년도 정보 조회 (시작일자~종료일자 정보)
        VcatnStatsYyDto statsYy = null;
        String yyStr = yyStrParam;
        if (StringUtils.isEmpty(yyStrParam)) {
            statsYy = vcatnStatsYyService.getCurrVcatnYyDt();
            yyStr = statsYy.getStatsYy();
        }
        if (statsYy == null) statsYy = vcatnStatsYyService.getVcatnYyDtDto(yyStr);
        model.addAttribute("vcatnYy", statsYy);
        // 휴가계획서 최저년도~올해 년도 (year) 목록 조회
        model.addAttribute("yyList", vcatnPaprService.getVcatnYyList());
        // 해당년도에 근무이력이 있는(중도퇴사 포함) 모든 직원(재직+프리랜서) 전원에 대하여 산정
        // List<VcatnStatsDto> statsList = vcatnStatsServicea.getVcatnStatsList(statsYy);
        // model.addAttribute("statsList", statsList);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/vcatn/stats/vcatn_stats";
    }
}
