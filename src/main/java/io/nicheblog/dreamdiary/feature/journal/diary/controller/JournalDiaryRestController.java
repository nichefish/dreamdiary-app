package io.nicheblog.dreamdiary.feature.journal.diary.controller;

import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryPostDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiarySearchParam;
import io.nicheblog.dreamdiary.feature.journal.diary.service.JournalDiaryService;
import io.nicheblog.dreamdiary.feature.journal.diary.service.my.MyJournalDiaryService;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;
import java.util.List;

/**
 * JournalDiaryRestController
 * <pre>
 *  저널 일기 RestController.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JournalDiaryRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    private final JournalDiaryService journalDiaryService;
    private final MyJournalDiaryService myJournalDiaryService;

    /**
     * 저널 일기 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_DIARIES})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDiaryListAjax(
            final JournalDiarySearchParam searchParam
    ) throws Exception {

        if (searchParam.isEmpty()) throw new IllegalArgumentException("검색 조건 필요");

        final List<JournalDiaryDto> journalDiaryList = myJournalDiaryService.getMyListDto(searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(journalDiaryList));
    }

    /**
     * 저널 일기 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_DIARY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDiaryDtlAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final JournalDiaryDto retrievedDto = myJournalDiaryService.getMyDtlDtoWithCache(id);
        final boolean isSuccess = (retrievedDto.getId() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 일기 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param journalDiary 등록/수정 처리할 객체
     * @param request - Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @Operation(
            summary = "저널 일기 등록/수정",
            description = "저널 일기 정보를 등록/수정한다."
    )
    @PostMapping(value = {Url.JOURNAL_DIARIES, Url.JOURNAL_DIARY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDiaryRegAjax(
            final @PathVariable(value = "id", required = false) Integer id,
            final @Valid JournalDiaryPostDto journalDiary,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isMdf = id != null;
        if (isMdf) journalDiary.setId(id);

        final ServiceResponse result = isMdf ? journalDiaryService.modify(journalDiary, request) : journalDiaryService.regist(journalDiary, request);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 저널 일기 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(value = {Url.JOURNAL_DIARY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDiaryDelAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final ServiceResponse result = journalDiaryService.delete(id);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}

