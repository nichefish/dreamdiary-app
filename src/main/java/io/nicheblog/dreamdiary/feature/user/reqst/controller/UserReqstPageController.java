package io.nicheblog.dreamdiary.feature.user.reqst.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.user.info.model.UserDto;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CdLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UserReqstPageController
 * <pre>
 *  사용자 계정 신청 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class UserReqstPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_REQST_REG_FORM;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_REQST;      // 작업 카테고리 (로그 적재용)

    private final CdLookupService cdLookupService;

    /**
     * 계정 정보 신청 화면 조회
     * (비로그인 사용자도 외부에서 접근 가능.) (인증 없음)
     *
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.USER_REQST_REG_FORM)
    public String userReqstRegForm(
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.USER_REQST);
        model.addAttribute("pageNm", PageNm.REG);

        // 빈 객체 주입 (freemarker error prevention)
        model.addAttribute("user", new UserDto());
        // 등록/수정 화면 플래그 세팅
        model.addAttribute(Constant.FORM_MODE, "regist");
        // 코드 정보 모델에 추가
        cdLookupService.setCdListToModel(Code.AUTH_CD, model);
        cdLookupService.setCdListToModel(Code.CMPY_CD, model);
        cdLookupService.setCdListToModel(Code.TEAM_CD, model);
        cdLookupService.setCdListToModel(Code.EMPLYM_CD, model);
        cdLookupService.setCdListToModel(Code.RANK_CD, model);

        return "/view/feature/user/reqst/user_reqst_form";
    }
}
