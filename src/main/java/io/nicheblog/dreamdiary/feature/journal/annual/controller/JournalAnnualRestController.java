package io.nicheblog.dreamdiary.feature.journal.annual.controller;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.journal.annual.model.JournalAnnualDto;
import io.nicheblog.dreamdiary.feature.journal.annual.model.JournalAnnualSearchParam;
import io.nicheblog.dreamdiary.feature.journal.annual.service.JournalAnnualService;
import io.nicheblog.dreamdiary.feature.journal.annual.service.my.MyJournalAnnualService;
import io.nicheblog.dreamdiary.feature.journal.annual.service.policy.JournalAnnualTagResolver;
import io.nicheblog.dreamdiary.feature.journal.annual.type.JournalAnnualTagType;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntrySearchParam;
import io.nicheblog.dreamdiary.feature.journal.entry.service.my.JournalEntryMyViewService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * JournalAnnualRestController
 * <pre>
 *  저널 연간 API Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JournalAnnualRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_ANNUAL_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    private final JournalAnnualService journalAnnualService;
    private final MyJournalAnnualService myJournalAnnualService;
    private final JournalEntryMyViewService journalEntryMyViewService;
    private final JournalAnnualTagResolver journalAnnualTagResolver;

    /**
     * 저널 연간 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_ANNUALS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalAnnualListAjax(
            final JournalAnnualSearchParam searchParam
    ) throws Exception {

        final List<JournalAnnualDto> journalAnnualList = myJournalAnnualService.getMyListDto(searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(journalAnnualList));
    }

    /**
     * 저널 연간 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 연도
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_ANNUAL})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalAnnualDtlAjax(
            final @PathVariable Integer yy
    ) throws Exception {

        final JournalAnnualDto retrievedDto = myJournalAnnualService.getMyDtlDtoByYy(yy);
        final boolean isSuccess = (retrievedDto.getId() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 연간 중요 일기 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 연도
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_ANNUAL_DIARIES})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalAnnualImprtcDiaryListAjax(
            final @PathVariable Integer yy,
            final @RequestParam(defaultValue = "true") boolean showImprtc,
            final @RequestParam(defaultValue = "false") boolean showRefrnc,
            final JournalEntrySearchParam searchParam
    ) throws Exception {

        searchParam.setYy(yy);
        searchParam.resolveStates(showImprtc, showRefrnc);
        final List<JournalEntryDto> journalEntryYyAnnualStatedListByUser = journalEntryMyViewService.getMyAnnualList(searchParam, ContentType.JOURNAL_DIARY);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(journalEntryYyAnnualStatedListByUser));
    }

    /**
     * 저널 연간 중요 꿈 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 연도
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_ANNUAL_DREAMS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalAnnualImprtcDreamListAjax(
            final @PathVariable Integer yy,
            final @RequestParam(defaultValue = "true") boolean showImprtc,
            final @RequestParam(defaultValue = "false") boolean showRefrnc,
            final JournalEntrySearchParam searchParam
    ) throws Exception {

        searchParam.setYy(yy);
        searchParam.resolveStates(showImprtc, showRefrnc);
        final List<JournalEntryDto> imprtcDreamList = journalEntryMyViewService.getMyAnnualList(searchParam, ContentType.JOURNAL_DREAM);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(imprtcDreamList));
    }

    /**
     * 저널 연간 태그 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 연도
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_ANNUAL_TAGS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalAnnualTagListAjax(
            final @PathVariable Integer yy,
            final @RequestParam("type") JournalAnnualTagType type
    ) throws Exception {

        final List<TagDto> tagList = journalAnnualTagResolver.resolveTagList(yy, type);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(tagList));
    }

    /**
     * 특정 연도 저널 연간 생성 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 연도
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.JOURNAL_ANNUAL_MAKE_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalAnnualMakeAjax(
            final @RequestParam("yy") Integer yy
    ) throws Exception {

        final boolean isSuccess = myJournalAnnualService.makeMyYyAnnual(yy);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 전체 연도 저널 연간 생성 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.JOURNAL_ANNUAL_MAKE_TOTAL_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalAnnualMakeTotalAjax(
            //
    ) throws Exception {

        final boolean isSuccess = myJournalAnnualService.makeMyTotalYyAnnual();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 저널 연간 꿈 기록 완료 처리 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.JOURNAL_ANNUAL_DREAM_COMPT_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalAnnualDreamComptAjax(
            final @RequestParam("id") Integer id
    ) throws Exception {

        final boolean isSuccess = journalAnnualService.dreamCompt(id);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 저널 연간 내용 수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param journalAnnual 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.JOURNAL_ANNUAL_REG_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalAnnualRegAjax(
            final @Valid JournalAnnualDto journalAnnual
    ) throws Exception {

        final ServiceResponse result = journalAnnualService.modify(journalAnnual);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
