package io.nicheblog.dreamdiary.feature.journal.day.controller;

import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayService;
import io.nicheblog.dreamdiary.feature.journal.day.service.my.MyJournalDayCalService;
import io.nicheblog.dreamdiary.feature.journal.day.service.my.MyJournalDayQueryService;
import io.nicheblog.dreamdiary.feature.journal.day.service.my.MyJournalDayService;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDayViewType;
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
 * JournalDayRestController
 * <pre>
 *  저널 일자 RestController.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JournalDayRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    private final JournalDayService journalDayService;
    private final MyJournalDayQueryService myJournalDayQueryService;
    private final MyJournalDayCalService myJournalDayCalService;
    private final MyJournalDayService myJournalDayService;

    /**
     * 저널 일자 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_DAYS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayListAjax(
            final @RequestParam("viewType") JournalDayViewType viewType,
            final JournalDaySearchParam searchParam
    ) throws Exception {

        final Object list = switch (viewType) {
            case LIST -> myJournalDayQueryService.getMyYyMnthListDtoEnriched(searchParam);
            case CAL -> myJournalDayCalService.getSchdulTotalCalList(searchParam);
            case DAILY -> myJournalDayQueryService.getMyStdrdDaysDtoEnriched(searchParam);
            case WEEKLY -> myJournalDayQueryService.getMyWeeklyListDtoEnriched(searchParam);
            case SEARCH -> myJournalDayQueryService.getMyListDtoByMetaIdEnriched(searchParam);
        };
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList((List<?>) list));
    }

    /**
     * 저널 일자 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param journalDay 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @Operation(
            summary = "저널 일자 등록/수정",
            description = "저널 일자 정보를 등록/수정한다."
    )
    @PostMapping(value = {Url.JOURNAL_DAYS, Url.JOURNAL_DAY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayRegAjax(
            final @PathVariable(value = "id", required = false) Integer id,
            final @Valid JournalDayDto journalDay
    ) throws Exception {

        boolean isReg = id == null;
        if (isReg) {
            boolean isDup = myJournalDayService.dupChck(journalDay);
            if (isDup) {
                journalDay.setId(myJournalDayService.getDupKey(journalDay));
                isReg = false;      // 등록 대신 기존 데이터 수정
            }
        }
        final ServiceResponse result = isReg ? journalDayService.regist(journalDay) : journalDayService.modify(journalDay);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 저널 일자 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_DAY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayDtlAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final JournalDayDto retrievedDto = myJournalDayQueryService.getMyDtlDtoEnriched(id);
        final boolean isSuccess = (retrievedDto.getId() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 일자 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(value = {Url.JOURNAL_DAY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayDelAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final ServiceResponse result = journalDayService.delete(id);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}

