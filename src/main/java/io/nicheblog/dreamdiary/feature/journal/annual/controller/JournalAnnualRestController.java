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
 *  ???寃곗궛 API Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JournalAnnualRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_ANNUAL_LIST;             // 湲곕낯 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // ?묒뾽 移댄뀒怨좊━ (濡쒓렇 ?곸옱??

    private final JournalAnnualService journalAnnualService;
    private final MyJournalAnnualService myJournalAnnualService;
    private final JournalEntryMyViewService journalEntryMyViewService;
    private final JournalAnnualTagResolver journalAnnualTagResolver;

    /**
     * ???寃곗궛 紐⑸줉 議고쉶 (Ajax)
     * (?ъ슜?륶SER, 愿由ъ옄MNGR留??묎렐 媛??)
     *
     * @param searchParam 寃??議곌굔???댁? ?뚮씪誘명꽣 媛앹껜
     * @return {@link ResponseEntity} -- 泥섎━ 寃곌낵? 硫붿떆吏
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
     * ???寃곗궛 ?곸꽭 議고쉶 (Ajax)
     * (?ъ슜?륶SER, 愿由ъ옄MNGR留??묎렐 媛??)
     *
     * @param yy ?꾨룄
     * @return {@link ResponseEntity} -- 泥섎━ 寃곌낵? 硫붿떆吏
     */
    @GetMapping(value = {Url.JOURNAL_ANNUAL})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalAnnualDtlAjax(
            final @PathVariable Integer yy
    ) throws Exception {

        // 媛앹껜 議고쉶 諛?紐⑤뜽??異붽?
        final JournalAnnualDto retrievedDto = myJournalAnnualService.getMyDtlDtoByYy(yy);
        final boolean isSuccess = (retrievedDto.getId() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * ???寃곗궛 以묒슂 ?쇨린 紐⑸줉 議고쉶 (Ajax)
     * (?ъ슜?륶SER, 愿由ъ옄MNGR留??묎렐 媛??)
     *
     * @param yy ?꾨룄
     * @return {@link ResponseEntity} -- 泥섎━ 寃곌낵? 硫붿떆吏
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

        // 以묒슂 ?쇨린 紐⑸줉 議고쉶
        searchParam.setYy(yy);
        searchParam.resolveStates(showImprtc, showRefrnc);
        final List<JournalEntryDto> journalEntryYyAnnualStatedListByUser = journalEntryMyViewService.getMyAnnualList(searchParam, ContentType.JOURNAL_DIARY);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(journalEntryYyAnnualStatedListByUser));
    }

    /**
     * ???寃곗궛 以묒슂 轅?紐⑸줉  議고쉶 (Ajax)
     * (?ъ슜?륶SER, 愿由ъ옄MNGR留??묎렐 媛??)
     *
     * @param yy ?꾨룄
     * @return {@link ResponseEntity} -- 泥섎━ 寃곌낵? 硫붿떆吏
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

        // 以묒슂 轅?紐⑸줉 議고쉶
        searchParam.setYy(yy);
        searchParam.resolveStates(showImprtc, showRefrnc);
        final List<JournalEntryDto> imprtcDreamList = journalEntryMyViewService.getMyAnnualList(searchParam, ContentType.JOURNAL_DREAM);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(imprtcDreamList));
    }

    /**
     * ???寃곗궛 ?쒓렇 紐⑸줉 議고쉶 (Ajax)
     * (?ъ슜?륶SER, 愿由ъ옄MNGR留??묎렐 媛??)
     *
     * @param yy ?꾨룄
     * @return {@link ResponseEntity} -- 泥섎━ 寃곌낵? 硫붿떆吏
     */
    @GetMapping(value = {Url.JOURNAL_ANNUAL_TAGS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalAnnualTagListAjax(
            final @PathVariable Integer yy,
            final @RequestParam("type") JournalAnnualTagType type
    ) throws Exception {

        // ?쒓렇 紐⑸줉 議고쉶
        final List<TagDto> tagList = journalAnnualTagResolver.resolveTagList(yy, type);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(tagList));
    }

    /**
     * ?뱀젙 ?꾨룄????????寃곗궛 ?앹꽦 (Ajax)
     * (?ъ슜?륶SER, 愿由ъ옄MNGR留??묎렐 媛??)
     *
     * @param yy ?꾨룄
     * @return {@link ResponseEntity} -- 泥섎━ 寃곌낵? 硫붿떆吏
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
     * ?꾩껜 ?꾨룄????????寃곗궛 ?앹꽦 (Ajax)
     * (?ъ슜?륶SER, 愿由ъ옄MNGR留??묎렐 媛??)
     *
     * @return {@link ResponseEntity} -- 泥섎━ 寃곌낵? 硫붿떆吏
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
     * ???寃곗궛 轅?湲곕줉 ?꾨즺 泥섎━ (Ajax)
     * (?ъ슜?륶SER, 愿由ъ옄MNGR留??묎렐 媛??)
     *
     * @param id ?앸퀎??     * @return {@link ResponseEntity} -- 泥섎━ 寃곌낵? 硫붿떆吏
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
     * ???寃곗궛 ?댁슜 ?섏젙 (Ajax)
     * (?ъ슜?륶SER, 愿由ъ옄MNGR留??묎렐 媛??)
     *
     * @param journalAnnual ?깅줉/?섏젙 泥섎━??媛앹껜
     * @return {@link ResponseEntity} -- 泥섎━ 寃곌낵? 硫붿떆吏
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
