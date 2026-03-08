package io.nicheblog.dreamdiary.domain.jrnl.entry.controller;

import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntrySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.entry.service.JrnlEntryExportService;
import io.nicheblog.dreamdiary.domain.jrnl.entry.service.JrnlEntryService;
import io.nicheblog.dreamdiary.extension.clsf.tag.handler.TagProcEventListener;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JrnlEntryRestController
 * <pre>
 *  저널 항목 API Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JrnlEntryRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    private final JrnlEntryService jrnlEntryService;
    private final JrnlEntryExportService jrnlEntryExportService;

    /**
     * 저널 항목 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_ENTRIES})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlEntryListAjax(
            JrnlEntrySearchParam searchParam
    ) throws Exception {

        final List<JrnlEntryDto> jrnlEntryList = jrnlEntryService.getListDto(searchParam);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(jrnlEntryList));
    }

    /**
     * 저널 항목 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param jrnlEntry 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see TagProcEventListener
     */
    @Operation(
            summary = "저널 항목 등록/수정",
            description = "저널 항목 정보를 등록/수정한다."
    )
    @PostMapping(value = {Url.JRNL_ENTRIES, Url.JRNL_ENTRY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlEntryRegAjax(
            final @PathVariable(value = "postNo", required = false) Integer postNo,
            final @Valid JrnlEntryDto jrnlEntry
    ) throws Exception {

        final boolean isMdf = postNo != null;
        if (isMdf) jrnlEntry.setPostNo(postNo);

        final ServiceResponse result = isMdf ? jrnlEntryService.modify(jrnlEntry) : jrnlEntryService.regist(jrnlEntry);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 저널 항목 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_ENTRY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlEntryDtlAjax(
            final @PathVariable("postNo") Integer key
    ) throws Exception {

        final JrnlEntryDto retrievedDto = jrnlEntryService.getDtlDto(key);
        final boolean isSuccess = (retrievedDto.getPostNo() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 항목 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see TagProcEventListener
     */
    @DeleteMapping(value = {Url.JRNL_ENTRY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlEntryDelAjax(
            final @PathVariable("postNo") Integer postNo
    ) throws Exception {

        final ServiceResponse result = jrnlEntryService.delete(postNo);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
    
    /**
     * 저널 항목 텍스트 내보내기
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see TagProcEventListener
     */
    @GetMapping(value = {Url.JRNL_ENTRY_EXPORT})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<byte[]> jrnlEntryExportTxtAjax(
            final @PathVariable("postNo") Integer postNo
    ) throws Exception {

        final JrnlEntryDto retrievedDto = jrnlEntryService.getDtlDto(postNo);
        final String text = jrnlEntryExportService.buildTxt(retrievedDto);
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        final String filename = "entry_" + DateUtils.asStr(retrievedDto.getStdrdDt(), DatePtn.PDATE) + "_@" + DateUtils.getCurrDateStr(DatePtn.PDATE) + ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}
