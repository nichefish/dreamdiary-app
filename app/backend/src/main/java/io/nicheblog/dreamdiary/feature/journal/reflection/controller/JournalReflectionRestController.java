package io.nicheblog.dreamdiary.feature.journal.reflection.controller;

import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.reflection.model.JournalReflectionPostDto;
import io.nicheblog.dreamdiary.feature.journal.reflection.service.JournalReflectionService;
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

/**
 * Reflection(Commentary) 쓰기 REST 컨트롤러.
 *
 * <p>Reflection 은 별도 Aggregate({@code journal_reflection})이라 Entry 와 분리된 쓰기 엔드포인트를 갖는다.
 * 등록/수정은 대상 필수(About-A)이며 서비스에서 검증한다. 읽기는 대상 엔트리 목록에 embed 로 실린다.</p>
 */
@RestController
@RequiredArgsConstructor
public class JournalReflectionRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DAY_MONTHLY;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;

    private final JournalReflectionService journalReflectionService;

    /**
     * Reflection 단건 상세를 조회한다(수정 모달 로드용).
     *
     * @param id Reflection ID
     * @return Ajax 응답
     * @throws Exception 조회 중 예외
     */
    @GetMapping(value = {Url.JOURNAL_REFLECTION})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalReflectionDtlAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {
        final JournalEntryDto retrievedDto = journalReflectionService.getDtlDtoByUser(id);
        final boolean retrieved = retrievedDto != null && retrievedDto.getId() != null;
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(retrieved, MessageUtils.getMessage("common.result.success")).withObj(retrievedDto));
    }

    /**
     * Reflection 등록/수정을 처리한다.
     *
     * @param id Reflection ID(null 이면 등록)
     * @param reflection 저장 DTO
     * @param request 멀티파트 요청
     * @return Ajax 응답
     * @throws Exception 저장 중 예외
     */
    @Operation(summary = "journal reflection regist/modify", description = "Regist or modify a reflection.")
    @PostMapping(value = {Url.JOURNAL_REFLECTIONS, Url.JOURNAL_REFLECTION})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalReflectionRegAjax(
            final @PathVariable(value = "id", required = false) Integer id,
            final @Valid JournalReflectionPostDto reflection,
            final MultipartHttpServletRequest request
    ) throws Exception {
        final boolean isMdf = id != null;
        if (isMdf) reflection.setId(id);
        final ServiceResponse result = isMdf
                ? journalReflectionService.modify(reflection, request)
                : journalReflectionService.regist(reflection, request);
        final boolean isSuccess = Boolean.TRUE.equals(result.getRslt());
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(
                result,
                isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure")
        ));
    }

    /**
     * Reflection 단건 삭제를 처리한다.
     *
     * @param id Reflection ID
     * @return Ajax 응답
     * @throws Exception 삭제 중 예외
     */
    @DeleteMapping(value = {Url.JOURNAL_REFLECTION})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalReflectionDelAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {
        final ServiceResponse result = journalReflectionService.delete(id);
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, MessageUtils.getMessage("common.result.success")));
    }
}
