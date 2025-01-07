package io.nicheblog.dreamdiary.global._common.cd.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.cd.entity.DtlCdKey;
import io.nicheblog.dreamdiary.global._common.cd.model.DtlCdDto;
import io.nicheblog.dreamdiary.global._common.cd.model.DtlCdParam;
import io.nicheblog.dreamdiary.global._common.cd.model.DtlCdSearchParam;
import io.nicheblog.dreamdiary.global._common.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * DtlCdRestController
 * <pre>
 *  상세 코드(dtlCd) 정보 관리 API 컨트롤러.
 *  ※상세 코드(dtl_cd) = 분류 코드 하위의 상세 코드. 분류 코드(cl_cd)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class DtlCdRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.CL_CD_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.CD;        // 작업 카테고리 (로그 적재용)

    private final DtlCdService dtlCdService;

    /**
     * 분류 코드로 상세 코드 관리(useYn=N 포함) 목록 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param clCd: 구분 코드 (대분류)
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.DTL_CD_LIST_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> dtlCdListAjax(
            final @RequestParam("clCd") String clCd,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final DtlCdSearchParam searchParam = DtlCdSearchParam.builder().clCd(clCd).build();
        final List<DtlCdDto> dtlCdList = dtlCdService.getListDto(searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setRsltList(dtlCdList);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 상세 코드 관리(useYn=N 포함) 등록/수정 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param dtlCd 등록/수정 처리할 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(value = {Url.DTL_CD_REG_AJAX})
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> dtlCdRegAjax(
            final @Valid DtlCdDto dtlCd,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final DtlCdDto result = dtlCdService.regist(dtlCd);
        final boolean isSuccess = (result.getDtlCd() != null);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(result);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 상세 코드 관리(useYn=N 포함) 등록/수정 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param dtlCd 등록/수정 처리할 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(value = {Url.DTL_CD_MDF_AJAX})
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> dtlCdMdfAjax(
            final @Valid DtlCdDto dtlCd,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final DtlCdDto result = dtlCdService.modify(dtlCd);
        final boolean isSuccess = (result.getDtlCd() != null);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(result);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 상세 코드 관리(useYn=N 포함) 상세 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.DTL_CD_DTL_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> dtlCdDtlAjax(
            final DtlCdKey key,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final DtlCdDto dtlCdDto = dtlCdService.getDtlDto(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(dtlCdDto);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 상세 코드 관리(useYn=N 포함) '사용'으로 변경 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.DTL_CD_USE_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> dtlCdUseAjax(
            final DtlCdKey key,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isSuccess = dtlCdService.setStateUse(key);
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 상세 코드 관리(useYn=N 포함) '미사용'으로 변경 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.DTL_CD_UNUSE_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> dtlCdUnuseAjax(
            final DtlCdKey key,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isSuccess = dtlCdService.setStateUnuse(key);
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 상세 코드 관리(useYn=N 포함) 삭제 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.DTL_CD_DEL_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> dtlCdDelAjax(
            final DtlCdKey key,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isSuccess = dtlCdService.delete(key);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 관리자 > 메뉴 관리 > 정렬 순서 저장 (드래그앤드랍 결과 반영) (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param dtlCdParam 키+정렬 순서 목록을 담은 파라미터
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.DTL_CD_SORT_ORDR_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> dtlCdSortOrdrAjax(
            final @RequestBody DtlCdParam dtlCdParam,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isSuccess = dtlCdService.sortOrdr(dtlCdParam.getSortOrdr());
        String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // logParam.setCn("key: " + menuNo);
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }
}
