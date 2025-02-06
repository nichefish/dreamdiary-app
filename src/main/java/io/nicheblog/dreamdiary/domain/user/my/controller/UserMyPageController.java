package io.nicheblog.dreamdiary.domain.user.my.controller;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.domain.user.info.model.UserDto;
import io.nicheblog.dreamdiary.domain.user.info.service.UserService;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnPaprDto;
import io.nicheblog.dreamdiary.domain.vcatn.papr.service.VcatnPaprService;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsYyDto;
import io.nicheblog.dreamdiary.domain.vcatn.stats.service.VcatnStatsYyService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * UserPageController
 * <pre>
 *  내 정보 관리 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyPageControllerAspect
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class UserMyPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_MY_DTL;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_MY;     // 작업 카테고리 (로그 적재용)

    private final UserService userService;
    private final VcatnStatsYyService vcatnStatsYyService;
    private final VcatnPaprService vcatnPaprService;

    /**
     * 내 정보 (상세) 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * 
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.USER_MY_DTL)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String myInfoDtl(
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.USER_MY);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // 내 정보 조회 및 모델에 추가
        final String lgnUserId = AuthUtils.getLgnUserId();
        final UserDto retrievedDto = userService.getDtlDto(lgnUserId);
        model.addAttribute("user", retrievedDto);

        // 휴가계획서 년도 정보 조회 (시작일자~종료일자)
        try {
            //if (AuthService.hasEcnyDt()) {
            final VcatnStatsYyDto statsYy = vcatnStatsYyService.getCurrVcatnYyDt();
            model.addAttribute("vcatnYy", statsYy);
            final String userId = AuthUtils.getLgnUserId();
            // VcatnStatsDto vcatnStatsDtl = vcatnStatsService.getVcatnStatsDtl(statsYy, userId);
            //  model.addAttribute("vcatnStats", vcatnStatsDtl);
            // 올해 사용 휴가 목록 조회
            final BaseSearchParam param = BaseSearchParam.builder()
                    .regstrId(lgnUserId)
                    .searchStartDt(statsYy.getBgnDt())
                    .searchEndDt(statsYy.getEndDt())
                    .build();
            final List<VcatnPaprDto.LIST> vcatnPaprList = vcatnPaprService.getListDto(param);
            model.addAttribute("vcatnPaprList", vcatnPaprList);
            // }
        } catch (final Exception e) {
            log.info("휴가계획서 정보를 조회하지 못했습니다.");
        }

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/user/my/user_my_dtl";
    }
}
