package io.nicheblog.dreamdiary.feature.admin.auth.policy.controller;

import io.nicheblog.dreamdiary.feature.admin.auth.policy.model.AuthPolicyDto;
import io.nicheblog.dreamdiary.feature.admin.auth.policy.service.AuthPolicyService;
import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * AuthPolicyPageController
 * <pre>
 *  인증 정책 관리 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class AuthPolicyPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.AUTH_POLICY_FORM;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.AUTH_POLICY;        // 작업 카테고리 (로그 적재용)

    private final AuthPolicyService authPolicyService;

    /**
     * 인증 정책 등록/수정 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.AUTH_POLICY_FORM)
    @Secured(Constant.ROLE_MNGR)
    public String authPolicyForm(
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.AUTH_POLICY);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // 항목 조회 및 모델에 추가 :: 현재는 항상 고정 ID(1L)로 조회한다.
        final AuthPolicyDto authPolicy = authPolicyService.getDtlDto();
        model.addAttribute("authPolicy", authPolicy);

        return "/view/feature/admin/auth/policy/auth_policy_reg_form";
    }
}
