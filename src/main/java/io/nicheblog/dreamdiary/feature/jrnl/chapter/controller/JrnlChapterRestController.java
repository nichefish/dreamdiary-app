package io.nicheblog.dreamdiary.feature.jrnl.chapter.controller;

import io.nicheblog.dreamdiary.feature.jrnl.chapter.model.JrnlChapterDto;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.model.JrnlChapterSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.service.JrnlChapterExportService;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.service.JrnlChapterService;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.service.my.MyJrnlChapterService;
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
 * JrnlChapterRestController
 * <pre>
 *  저널 챕터 API Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JrnlChapterRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    private final JrnlChapterService jrnlChapterService;
    private final MyJrnlChapterService myJrnlChapterService;
    private final JrnlChapterExportService jrnlChapterExportService;

    /**
     * 저널 챕터 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_CHAPTERS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlChapterListAjax(
            JrnlChapterSearchParam searchParam
    ) throws Exception {

        final List<JrnlChapterDto> jrnlChapterList = myJrnlChapterService.getMyListDto(searchParam);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(jrnlChapterList));
    }

    /**
     * 저널 챕터 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param jrnlChapter 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @Operation(
            summary = "저널 챕터 등록/수정",
            description = "저널 챕터 정보를 등록/수정한다."
    )
    @PostMapping(value = {Url.JRNL_CHAPTERS, Url.JRNL_CHAPTER})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlChapterRegAjax(
            final @PathVariable(value = "postNo", required = false) Integer postNo,
            final @Valid JrnlChapterDto jrnlChapter
    ) throws Exception {

        final boolean isMdf = postNo != null;
        if (isMdf) jrnlChapter.setPostNo(postNo);

        final ServiceResponse result = isMdf ? jrnlChapterService.modify(jrnlChapter) : jrnlChapterService.regist(jrnlChapter);
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
    @GetMapping(value = {Url.JRNL_CHAPTER})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlChapterDtlAjax(
            final @PathVariable("postNo") Integer key
    ) throws Exception {

        final JrnlChapterDto retrievedDto = myJrnlChapterService.getMyDtlDtoWithCache(key);
        final boolean isSuccess = (retrievedDto.getPostNo() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 챕터 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(value = {Url.JRNL_CHAPTER})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlChapterDelAjax(
            final @PathVariable("postNo") Integer postNo
    ) throws Exception {

        final ServiceResponse result = jrnlChapterService.delete(postNo);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
    
    /**
     * 저널 챕터 텍스트 내보내기
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_CHAPTER_EXPORT})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<byte[]> jrnlChapterExportTxtAjax(
            final @PathVariable("postNo") Integer postNo
    ) throws Exception {

        final JrnlChapterDto retrievedDto = myJrnlChapterService.getMyDtlDtoWithCache(postNo);
        final String text = jrnlChapterExportService.buildTxt(retrievedDto);
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        final String filename = "chapter_" + DateUtils.asStr(retrievedDto.getStdrdDt(), DatePtn.PDATE) + "_@" + DateUtils.getCurrDateStr(DatePtn.PDATE) + ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}
