package io.nicheblog.dreamdiary.feature.user.account.controller;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.user.account.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.account.model.UserSearchParam;
import io.nicheblog.dreamdiary.feature.user.account.service.UserService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * UserRestController
 * <pre>
 *  사용자 관리 > 계정 및 권한 관리 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class UserRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER;      // 작업 카테고리 (로그 적재용)

    private final UserService userService;

    /**
     * 사용자 아이디 중복 체크 (Ajax)
     * 사용자 계정 신청시 사용해야 하므로 인증 없이 접근 가능
     *
     * @param username 중복 체크를 할 사용자 아이디
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.USERNAME_DUP_CHK_AJAX)
    @ResponseBody
    public ResponseEntity<AjaxResponse> usernameDupChckAjax(
            final @RequestParam("username") String username
    ) {

        final Boolean isUsernameDup = userService.usernameDupChck(username);
        final boolean isSuccess = !isUsernameDup;
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? "msg.user.id.usable" : "msg.user.id.duplicated");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 사용자 이메일 중복 체크 (Ajax)
     * 사용자 계정 신청시 사용해야 하므로 인증 없이 접근 가능
     *
     * @param email 중복 체크를 할 사용자 아이디
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.USER_EMAIL_DUP_CHK_AJAX)
    @ResponseBody
    public ResponseEntity<AjaxResponse> userEmailDupChckAjax(
            final @RequestParam("email") String email
    ) {

        final Boolean isEmailDup = userService.emailDupChck(email);
        final boolean isSuccess = !isEmailDup;
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? "msg.user.email.usable" : "msg.user.email.duplicated");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 사용자 등록/수정 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param user 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.USER_REG_AJAX, Url.USER_MDF_AJAX})
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> userRegAjax(
            final @Valid UserDto user
    ) throws Exception {

        final boolean isReg = (user.getKey() == null);
        final ServiceResponse result = isReg ? userService.regist(user) : userService.modify(user);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 사용자 관리 > 계정 및 권한 관리 > 사용자 패스워드 초기화 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param id 패스워드를 초기화할 사용자 아이디
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.USER_PW_RESET_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> passwordResetAjax(
            final @RequestParam("id") Integer id
    ) throws Exception {

        final ServiceResponse result = userService.passwordReset(id);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS_PW_RESET : MessageUtils.RSLT_FAILURE);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 사용자 관리 > 계정 및 권한 관리 > 사용자 삭제 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.USER_DEL_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> userDelAjax(
            final @RequestParam("id") Integer id
    ) throws Exception {

        final UserDto user = userService.getDtlDto(id);
        // 내 정보인지 비교 :: "내 정보는 삭제할 수 없습니다."
        final boolean isMyInfo = AuthUtils.isMyInfo(user.getUsername());
        if (!isMyInfo) {
            final String rsltMsg = MessageUtils.NOT_DELABLE_OWN_ID;
            return ResponseEntity.ok(AjaxResponse.withAjaxResult(false, rsltMsg));
        }

        final ServiceResponse result = userService.delete(id);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 사용자 관리 > 계정 및 권한 관리 > 사용자 목록 엑셀 다운로드
     * (관리자MNGR만 접근 가능.)
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * TODO: 더 일반화하기
     */
    @GetMapping(Url.USER_LIST_XLSX_DOWNLOAD)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> userListXlsxDownload(
            final @ModelAttribute("searchParam") UserSearchParam searchParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        boolean isSuccess = false;
        String rsltMsg = "";
        try {
            // List<Object> userListXlsx = userService.userListXlsx(searchParamMap);
            // xlsxUtils.listXlxsDownload(Constant.user_profl, userListXlsx);
        } catch (final Exception e) {
            rsltMsg = MessageUtils.getExceptionMsg(e);
            MessageUtils.alertMessage(rsltMsg, baseUrl);
        }

        return ResponseEntity.ok(ajaxResponse);
    }
}
