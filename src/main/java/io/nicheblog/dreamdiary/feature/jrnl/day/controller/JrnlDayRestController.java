package io.nicheblog.dreamdiary.feature.jrnl.day.controller;

import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.my.MyJrnlDayCalService;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.my.MyJrnlDayQueryService;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.my.MyJrnlDayService;
import io.nicheblog.dreamdiary.feature.jrnl.day.type.JrnlDayViewType;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * JrnlDayRestController
 * <pre>
 *  저널 일자 RestController.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JrnlDayRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    private final JrnlDayService jrnlDayService;
    private final MyJrnlDayQueryService myJrnlDayQueryService;
    private final MyJrnlDayCalService myJrnlDayCalService;
    private final MyJrnlDayService myJrnlDayService;

    /**
     * 저널 일자 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_DAYS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayListAjax(
            final @RequestParam("viewType") JrnlDayViewType viewType,
            final JrnlDaySearchParam searchParam
    ) throws Exception {

        final Object list = switch (viewType) {
            case LIST -> myJrnlDayQueryService.getMyYyMnthListDtoEnriched(searchParam);
            case CAL -> myJrnlDayCalService.getSchdulTotalCalList(searchParam);
            case DAILY -> myJrnlDayQueryService.getMyStdrdDaysDtoEnriched(searchParam);
            case WEEKLY -> myJrnlDayQueryService.getMyWeeklyListDtoEnriched(searchParam);
            case SEARCH -> myJrnlDayQueryService.getMyListDtoByMetaNoEnriched(searchParam);
        };
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList((List<?>) list));
    }

    /**
     * 저널 일자 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param jrnlDay 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @Operation(
            summary = "저널 일자 등록/수정",
            description = "저널 일자 정보를 등록/수정한다."
    )
    @PostMapping(value = {Url.JRNL_DAYS, Url.JRNL_DAY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayRegAjax(
            final @PathVariable(value = "postNo", required = false) Integer postNo,
            final @Valid JrnlDayDto jrnlDay
    ) throws Exception {

        boolean isReg = postNo == null;
        if (isReg) {
            boolean isDup = myJrnlDayService.dupChck(jrnlDay);
            if (isDup) {
                jrnlDay.setPostNo(myJrnlDayService.getDupKey(jrnlDay));
                isReg = false;      // 등록 대신 기존 데이터 수정
            }
        }
        final ServiceResponse result = isReg ? jrnlDayService.regist(jrnlDay) : jrnlDayService.modify(jrnlDay);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 저널 일자 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_DAY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayDtlAjax(
            final @PathVariable("postNo") Integer key
    ) throws Exception {

        final JrnlDayDto retrievedDto = myJrnlDayQueryService.getMyDtlDtoEnriched(key);
        final boolean isSuccess = (retrievedDto.getPostNo() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 일자 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(value = {Url.JRNL_DAY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayDelAjax(
            final @PathVariable("postNo") Integer key
    ) throws Exception {

        final ServiceResponse result = jrnlDayService.delete(key);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
