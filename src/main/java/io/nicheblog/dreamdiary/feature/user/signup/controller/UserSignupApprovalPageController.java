package io.nicheblog.dreamdiary.feature.user.signup.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.type.PageName;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.user.signup.entity.UserSignupRequestEntity;
import io.nicheblog.dreamdiary.feature.user.signup.repository.jpa.UserSignupRequestRepository;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * UserSignupApprovalPageController
 * <pre>
 *  사용자 관리 > 계정 신청 승인관리 화면 컨트롤러.
 * </pre>
 *
 * 변경 전: 계정 승인/반려 액션이 계정관리(user_list) 화면 상단 카드에 혼재되어 있었다.
 * 변경 후: 승인 대기 처리 흐름은 전용 페이지(`/app/user/signup/list.do`)로 분리하여, 계정관리(사용자 마스터 유지보수)와 역할을 구분한다.
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class UserSignupApprovalPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_SIGNUP_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_SIGNUP;      // 작업 카테고리 (로그 적재용)

    private final UserSignupRequestRepository userSignupRequestRepository;

    /**
     * 사용자 관리 > 계정 신청 승인관리 목록 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.USER_SIGNUP_LIST)
    @Secured(Constant.ROLE_MNGR)
    public String userSignupApprovalList(
            final ModelMap model
    ) {
        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.USER_SIGNUP_APPROVAL);
        model.addAttribute("pageName", PageName.LIST);

        final List<UserSignupRequestEntity> pendingList = userSignupRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        final List<UserSignupRequestEntity> recentList = userSignupRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("pendingSignupReqList", pendingList);
        model.addAttribute("recentSignupReqList", recentList.stream().limit(30).toList());

        return "/view/feature/user/signup/user_signup_approval_list";
    }
}
