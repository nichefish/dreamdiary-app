package io.nicheblog.dreamdiary.feature.user.signup.controller;

import io.nicheblog.dreamdiary.feature.user.signup.model.UserSignupRequestDto;
import io.nicheblog.dreamdiary.feature.user.signup.service.UserSignupService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * UserSignupRestController
 * <pre>
 *  사용자 계정 신청 API 컨트롤러.
 * </pre>
 * TODO: 기능추가 예정
 *
 * 명명 규약: 기능·API 진입 계열은 {@code UserSignup*}, 요청 페이로드·저장 대상 신청 레코드는 {@code UserSignupRequest*} 로 구분한다.
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class UserSignupRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_SIGNUP_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_SIGNUP;      // 작업 카테고리 (로그 적재용)

    private final UserSignupService userSignupService;

    /**
     * 계정 정보 신청 (Ajax)
     * (비로그인 사용자도 외부에서 접근 가능.) (인증 없음)
     *
     * 변경 전: `POST /api/user/signup/signup-reg`.
     * 변경 후: `POST /api/user/signup-requests` (컬렉션에 대한 생성).
     *
     * @param signupRequest 등록/수정할 객체
     *                         변경 전: 파라미터·필드명이 userReqst 계열이었음.
     *                         변경 후: 동일한 multipart 필드 바인딩(UserSignupRequestDto)만 유지한다.
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.USER_SIGNUP_REQUESTS})
    @ResponseBody
    public ResponseEntity<AjaxResponse> userSignupRegAjax(
            final @Valid UserSignupRequestDto signupRequest
    ) throws Exception {

        final ServiceResponse result = userSignupService.regist(signupRequest);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? "신규계정이 성공적으로 신청되었습니다." : "신규계정 신청에 실패했습니다.";     // TODO: 메세지 변수로 빼기

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 사용자 관리 > 계정 및 권한 관리 > 사용자 승인. (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * 변경 전: `POST …?id=` 형태 또는 `/signup-cf` 등 비명사형 경로.
     * 변경 후: `POST …/user/signup-requests/{id}/approval` (경로 변수).
     *
     * @param id 신청 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.USER_SIGNUP_REQUEST_APPROVAL)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> userCfAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final ServiceResponse result = userSignupService.cf(id);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 사용자 관리 > 계정 및 권한 관리 > 사용자 거절(승인 취소) (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * 변경 전: `/signup-uncf` 등 비명사형 경로.
     * 변경 후: `POST …/user/signup-requests/{id}/rejection`.
     *
     * @param id 신청 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.USER_SIGNUP_REQUEST_REJECTION)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> userUncfAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final ServiceResponse result = userSignupService.uncf(id);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }
}
