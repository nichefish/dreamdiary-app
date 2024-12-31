package io.nicheblog.dreamdiary.domain.admin.lgnPolicy.controller;

import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.model.LgnPolicyDto;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.service.LgnPolicyService;
import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * LgnPolicyPageController
 * <pre>
 *  로그인 정책 관리 페이지 컨트롤러.
 * </pre>
 *
 * @see LogActvtyPageControllerAspect
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class LgnPolicyPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.LGN_POLICY_FORM;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LGN_POLICY;        // 작업 카테고리 (로그 적재용)

    private final LgnPolicyService lgnPolicyService;

    /**
     * 사이트 관리 > 로그인 설정 관리 > 로그인 설정 등록/수정 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.LGN_POLICY_FORM)
    @Secured(Constant.ROLE_MNGR)
    public String lgnPolicyForm(
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.LGN_POLICY);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // 항목 조회 및 모델에 추가 :: 현재는 항상 고정 ID(1L)로 조회한다.
        final LgnPolicyDto lgnPolicy = lgnPolicyService.getDtlDto();
        model.addAttribute("lgnPolicy", lgnPolicy);

        final boolean isSuccess = (lgnPolicy != null);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/admin/lgn_policy/lgn_policy_reg_form";
    }
}