package io.nicheblog.dreamdiary.feature.user.my.controller;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.user.account.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.account.model.UserPwChgParam;
import io.nicheblog.dreamdiary.feature.user.account.service.UserService;
import io.nicheblog.dreamdiary.feature.user.my.model.UserMyUpdateRequest;
import io.nicheblog.dreamdiary.feature.user.my.service.UserMyService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Nullable;
import javax.validation.Valid;

/**
 * UserMyRestController
 * <pre>
 *  내 정보 관리 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class UserMyRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_MY_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_MY;     // 작업 카테고리 (로그 적재용)

    private final UserMyService userMyService;
    private final UserService userService;

    @GetMapping(Url.USER_MY_INFO)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> myInfoAjax() throws Exception {

        final String loginUsername = AuthUtils.getLoginUsername();
        final UserDto retrievedDto = userService.getDtlDto(loginUsername);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(retrievedDto));
    }

    /**
     * 로그인 사용자의 개인 연락처·프로필 수정.
     * 계정 식별자·이메일·권한·허용 IP·재직 정보는 요청 계약에서 제외한다.
     *
     * @param request 수정할 개인 프로필 정보
     * @return 처리 결과
     */
    @PutMapping(Url.USER_MY_INFO)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> modifyMyInfo(
            final @Valid @RequestBody UserMyUpdateRequest request
    ) throws Exception {

        final boolean isSuccess = userMyService.modifyMyInfo(request);
        final String rsltMsg = MessageUtils.getMessage(
                isSuccess ? "common.result.success" : "common.result.failure"
        );

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 프로필 이미지 등록 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * 
     * @param request - Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.USER_MY_UPLOAD_PROFL_IMG_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> uploadProflImgAjax(
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isSuccess = userMyService.uploadProflImg(request);
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 프로필 이미지 제거 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * 
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.USER_MY_REMOVE_PROFL_IMG_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> removeProflImgAjax(
            //
    ) throws Exception {

        final boolean isSuccess = userMyService.removeProflImg();
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 내 비밀번호 확인 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param currPw 현재 비밀번호
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.USER_MY_PW_CF_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> myPwChkAjax(
            final @RequestParam("currPw") @Nullable String currPw
    ) throws Exception {

        final String loginUsername = AuthUtils.getLoginUsername();
        final boolean isSuccess = userMyService.myPwCf(loginUsername, currPw);
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 내 비밀번호 변경 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param pwChgParam 비밀번호 변경 파라미터
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.USER_MY_PW_CHG_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> myPwChgAjax(
            final UserPwChgParam pwChgParam
    ) throws Exception {

        final boolean isSuccess = userMyService.myPwChg(pwChgParam);
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }
}
