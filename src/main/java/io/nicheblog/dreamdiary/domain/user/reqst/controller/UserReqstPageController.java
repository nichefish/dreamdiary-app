package io.nicheblog.dreamdiary.domain.user.reqst.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.domain.user.info.model.UserDto;
import io.nicheblog.dreamdiary.extension.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
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

    private final DtlCdService dtlCdService;

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
        dtlCdService.setCdListToModel(Constant.AUTH_CD, model);
        dtlCdService.setCdListToModel(Constant.CMPY_CD, model);
        dtlCdService.setCdListToModel(Constant.TEAM_CD, model);
        dtlCdService.setCdListToModel(Constant.EMPLYM_CD, model);
        dtlCdService.setCdListToModel(Constant.RANK_CD, model);

        return "/view/domain/user/reqst/user_reqst_form";
    }
}
