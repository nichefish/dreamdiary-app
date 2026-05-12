package io.nicheblog.dreamdiary.feature.journal.entry.controller;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntrySearchParam;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.my.JournalEntryMyViewService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypeResolver;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
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

@RestController
@RequiredArgsConstructor
public class JournalEntryRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DAY_MONTHLY;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;

    private final JournalEntryService journalEntryService;
    private final JournalEntryMyViewService journalEntryMyViewService;
    private final JournalEntryTypeResolver typeResolver;

    /**
     * 내 엔트리 목록을 타입 조건으로 조회한다.
     *
     * @param searchParam 검색 조건
     * @param type 엔트리 타입(DIARY/DREAM 또는 JOURNAL_DIARY/JOURNAL_DREAM)
     * @return Ajax 응답
     * @throws Exception 조회 중 예외
     */
    @GetMapping(value = {Url.JOURNAL_ENTRIES})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalEntryListAjax(
            final JournalEntrySearchParam searchParam,
            final @RequestParam(value = "type", required = false) String type
    ) throws Exception {
        final ContentType contentType = typeResolver.resolveByRawTypeOrFallback(type, searchParam.getContentType());
        searchParam.setContentType(contentType.key);
        return listAjax(searchParam, contentType);
    }

    /**
     * 단건 엔트리 상세를 조회한다.
     *
     * @param id 엔트리 ID
     * @return Ajax 응답
     * @throws Exception 조회 중 예외
     */
    @GetMapping(value = {Url.JOURNAL_ENTRY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalEntryDtlAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {
        final ContentType contentType = typeResolver.resolveByEntryId(id);
        return detailAjax(id, contentType);
    }

    /**
     * 엔트리 등록/수정을 처리한다.
     *
     * @param id 엔트리 ID(null 이면 등록)
     * @param journalEntry 저장 DTO
     * @param request 멀티파트 요청
     * @return Ajax 응답
     * @throws Exception 저장 중 예외
     */
    @Operation(summary = "journal entry regist/modify", description = "Regist or modify an entry.")
    @PostMapping(value = {Url.JOURNAL_ENTRIES, Url.JOURNAL_ENTRY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalEntryRegAjax(
            final @PathVariable(value = "id", required = false) Integer id,
            final @Valid JournalEntryPostDto journalEntry,
            final MultipartHttpServletRequest request
    ) throws Exception {
        return saveAjax(id, journalEntry, request);
    }

    /**
     * 엔트리 단건 삭제를 처리한다.
     *
     * @param id 엔트리 ID
     * @return Ajax 응답
     * @throws Exception 삭제 중 예외
     */
    @DeleteMapping(value = {Url.JOURNAL_ENTRY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalEntryDelAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {
        return deleteAjax(id);
    }

    /**
     * 비어 있는 검색 파라미터를 방어한다.
     *
     * @param searchParam 검색 조건
     */
    private void assertSearchParam(final JournalEntrySearchParam searchParam) {
        if (searchParam.isEmpty()) throw new IllegalArgumentException("Search parameter is empty.");
    }

    /**
     * 콘텐츠 타입별 목록 응답을 공통 구성한다.
     *
     * @param searchParam 검색 조건
     * @param contentType 콘텐츠 타입
     * @return Ajax 응답
     * @throws Exception 조회 중 예외
     */
    private ResponseEntity<AjaxResponse> listAjax(
            final JournalEntrySearchParam searchParam,
            final ContentType contentType
    ) throws Exception {
        assertSearchParam(searchParam);
        final List<JournalEntryDto> listDto = journalEntryMyViewService.getMyList(searchParam, contentType);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withList(listDto));
    }

    /**
     * 콘텐츠 타입별 상세 응답을 공통 구성한다.
     *
     * @param id 엔트리 ID
     * @param contentType 콘텐츠 타입
     * @return Ajax 응답
     * @throws Exception 조회 중 예외
     */
    private ResponseEntity<AjaxResponse> detailAjax(
            final Integer id,
            final ContentType contentType
    ) throws Exception {
        final JournalEntryDto retrievedDto = journalEntryMyViewService.getMyDetail(id, contentType);
        final boolean retrieved = retrievedDto != null && retrievedDto.getId() != null;
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(retrieved, MessageUtils.RSLT_SUCCESS).withObj(retrievedDto));
    }

    /**
     * 등록/수정 여부를 판단해 저장을 위임한다.
     *
     * @param id 엔트리 ID
     * @param postDto 저장 DTO
     * @param request 멀티파트 요청
     * @return Ajax 응답
     * @throws Exception 저장 중 예외
     */
    private ResponseEntity<AjaxResponse> saveAjax(
            final Integer id,
            final JournalEntryPostDto postDto,
            final MultipartHttpServletRequest request
    ) throws Exception {
        final boolean isMdf = id != null;
        if (isMdf) postDto.setId(id);
        final ServiceResponse result = isMdf
                ? journalEntryService.modify(postDto, request)
                : journalEntryService.regist(postDto, request);
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, result.getRslt() ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE));
    }

    /**
     * 엔트리 삭제 응답을 공통 구성한다.
     *
     * @param id 엔트리 ID
     * @return Ajax 응답
     * @throws Exception 삭제 중 예외
     */
    private ResponseEntity<AjaxResponse> deleteAjax(final Integer id) throws Exception {
        final ServiceResponse result = journalEntryService.delete(id);
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, MessageUtils.RSLT_SUCCESS));
    }

}
