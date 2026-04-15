package io.nicheblog.dreamdiary.feature.journal.chapter.controller;

import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterSearchParam;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.JournalChapterExportService;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.JournalChapterService;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.my.MyJournalChapterService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
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
 * JournalChapterRestController
 * <pre>
 *  저널 챕터 API Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JournalChapterRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    private final JournalChapterService journalChapterService;
    private final MyJournalChapterService myJournalChapterService;
    private final JournalChapterExportService journalChapterExportService;

    /**
     * 저널 챕터 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_CHAPTERS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalChapterListAjax(
            JournalChapterSearchParam searchParam
    ) throws Exception {

        final List<JournalChapterDto> journalChapterList = myJournalChapterService.getMyListDto(searchParam);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(journalChapterList));
    }

    /**
     * 저널 챕터 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param journalChapter 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @Operation(
            summary = "저널 챕터 등록/수정",
            description = "저널 챕터 정보를 등록/수정한다."
    )
    @PostMapping(value = {Url.JOURNAL_CHAPTERS, Url.JOURNAL_CHAPTER})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalChapterRegAjax(
            final @PathVariable(value = "id", required = false) Integer id,
            final @Valid JournalChapterDto journalChapter
    ) throws Exception {

        final boolean isMdf = id != null;
        if (isMdf) journalChapter.setId(id);

        final ServiceResponse result = isMdf ? journalChapterService.modify(journalChapter) : journalChapterService.regist(journalChapter);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 저널 챕터 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_CHAPTER})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalChapterDtlAjax(
            final @PathVariable("id") Integer key
    ) throws Exception {

        final JournalChapterDto retrievedDto = myJournalChapterService.getMyDtlDtoWithCache(key);
        final boolean isSuccess = (retrievedDto.getId() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 챕터 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(value = {Url.JOURNAL_CHAPTER})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalChapterDelAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final ServiceResponse result = journalChapterService.delete(id);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
    
    /**
     * 저널 챕터 텍스트 내보내기
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_CHAPTER_EXPORT})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<byte[]> journalChapterExportTxtAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final JournalChapterDto retrievedDto = myJournalChapterService.getMyDtlDtoWithCache(id);
        final String text = journalChapterExportService.buildTxt(retrievedDto);
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        final String filename = "chapter_" + DateUtils.asStr(retrievedDto.getStdrdDt(), DatePtn.PDATE) + "_@" + DateUtils.getCurrDateStr(DatePtn.PDATE) + ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}

