package io.nicheblog.dreamdiary.infrastructure.cd.controller;

import io.nicheblog.dreamdiary.infrastructure.cd.model.ClCdDto;
import io.nicheblog.dreamdiary.infrastructure.cd.model.ClCdPatchDto;
import io.nicheblog.dreamdiary.infrastructure.cd.service.ClCdService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.infrastructure.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * ClCdRestController
 * <pre>
 *  분류 코드 정보 관리 API 컨트롤러.
 *  ※분류 코드(cl_cd) = 상위 분류 코드. 상세 코드(dtl_cd)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class ClCdRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.CL_CD_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.CD;        // 작업 카테고리 (로그 적재용)

    private final ClCdService clCdService;

    /**
     * 분류 코드(CL_CD) 관리(useYn=N 포함) 등록 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param clCd 등록/수정 처리할 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.CD_CLS})
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clCdRegAjax(
            final @Valid ClCdDto clCd,
            final LogActvtyParam logParam
    ) throws Exception {

        final ServiceResponse result = clCdService.regist(clCd);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 분류 코드(CL_CD) 관리(useYn=N 포함) 수정 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param clCd 등록/수정 처리할 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.CD_CL})
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clCdMdfAjax(
            final @PathVariable("clCd") String clCd,
            final @Valid ClCdDto clCdDto,
            final LogActvtyParam logParam
    ) throws Exception {

        final ServiceResponse result = clCdService.modify(clCdDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 분류 코드 관리(useYn=N 포함) 상세 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param clCd 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.CD_CL)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clCdDtlAjax(
            final @PathVariable("clCd") String clCd,
            final LogActvtyParam logParam
    ) throws Exception {

        final ClCdDto cmmClCd = clCdService.getDtlDto(clCd);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(cmmClCd));
    }

    /**
     * 코드 상태 변경 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param clCd 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PatchMapping(Url.CD_CL)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuPatchAjax(
            final @PathVariable("clCd") String clCd,
            final @RequestBody ClCdPatchDto patchDto,
            final LogActvtyParam logParam
    ) throws Exception {

        final ServiceResponse result = clCdService.patch(clCd, patchDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 분류 코드(CL_CD) 관리(useYn=N 포함) 삭제 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param clCd 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(Url.CD_CL)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clCdDelAjax(
            final @PathVariable("clCd") String clCd,
            final LogActvtyParam logParam
    ) throws Exception {

        final ServiceResponse result = clCdService.delete(clCd);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(AjaxResponse.fromResponse(result, rsltMsg));
    }
}
