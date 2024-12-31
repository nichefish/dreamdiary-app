package io.nicheblog.dreamdiary.domain.vcatn.papr.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.domain.user.info.model.UserDto;
import io.nicheblog.dreamdiary.domain.user.info.service.UserService;
import io.nicheblog.dreamdiary.domain.vcatn.papr.service.VcatnPaprService;
import io.nicheblog.dreamdiary.domain.vcatn.papr.service.VcatnSchdulService;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsYyDto;
import io.nicheblog.dreamdiary.domain.vcatn.stats.service.VcatnStatsYyService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyPageControllerAspect;
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
import java.util.List;

/**
 * VcatnSchdulPageController
 * <pre>
 *  휴가관리 > 휴가사용일자 페이지 컨트롤러.
 * </pre>
 *
 * @see LogActvtyPageControllerAspect
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class VcatnSchdulPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.VCATN_SCHDUL_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.VCATN_SCHDUL;      // 작업 카테고리 (로그 적재용)

    private final VcatnPaprService vcatnPaprService;
    private final VcatnStatsYyService vcatnStatsYyService;
    private final VcatnSchdulService vcatnSchdulService;
    private final UserService userService;
    private final DtlCdService dtlCdService;

    /**
     * 휴가관리 > 휴가사용일자 > 휴가사용일자 목록 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param yyStrParam 년도 파라미터
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 전달할 데이터를 저장하는 ModelMap 객체
     * @return {@link String} -- 뷰 이름을 나타내는 문자열
     */
    @GetMapping(Url.VCATN_SCHDUL_LIST)
    @Secured(Constant.ROLE_MNGR)
    public String vcatnSchdulList(
            final @RequestParam("statsYy") @Nullable String yyStrParam,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.VCATN_SCHDUL);
        model.addAttribute("pageNm", PageNm.CAL);

        // 휴가계획서 년도 정보 조회 (시작일자~종료일자 세팅 정보)
        VcatnStatsYyDto statsYy = null;
        String yyStr = yyStrParam;
        if (StringUtils.isEmpty(yyStrParam)) {
            statsYy = vcatnStatsYyService.getCurrVcatnYyDt();
            yyStr = statsYy.getStatsYy();
        }
        if (statsYy == null) statsYy = vcatnStatsYyService.getVcatnYyDtDto(yyStr);
        model.addAttribute("vcatnYy", statsYy);
        // 휴가계획서 최저년도~올해 년도(year) 목록 조회
        model.addAttribute("yyList", vcatnPaprService.getVcatnYyList());
        // 직원 목록 조회 (등록에 쓰임)
        final List<UserDto.LIST> crtdUserList = userService.getCrdtUserList(statsYy.getBgnDt(), statsYy.getEndDt());
        model.addAttribute("crtdUserList", crtdUserList);
        // 일반 휴가(날짜범위)를 하루하루로 다 쪼개야 한다.
        model.addAttribute("vcatnSchdulList", vcatnSchdulService.getListDto(statsYy));
        dtlCdService.setCdListToModel(Constant.VCATN_CD, model);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/vcatn/schdul/vcatn_schdul_list";
    }
}