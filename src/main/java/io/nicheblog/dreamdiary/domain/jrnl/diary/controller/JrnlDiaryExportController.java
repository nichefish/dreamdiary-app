package io.nicheblog.dreamdiary.domain.jrnl.diary.controller;

import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiarySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.JrnlDiaryExportService;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.JrnlDiaryService;
import io.nicheblog.dreamdiary.extension.clsf.tag.handler.TagProcEventListener;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JrnlDiaryRestController
 * <pre>
 *  저널 일기 RestController.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyRestControllerAspect
 */
@RestController
@RequiredArgsConstructor
public class JrnlDiaryExportController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    private final JrnlDiaryService jrnlDiaryService;
    private final JrnlDiaryExportService jrnlDiaryExportService;

    /**
     * 저널 일기 txt 다룬로드
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param response HttpServletResponse
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see TagProcEventListener
     */
    @GetMapping(value = Url.JRNL_DIARIES_EXPORT, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<byte[]> jrnlDiaryExportTxt(
            final JrnlDiarySearchParam searchParam,
            final HttpServletResponse response
    ) throws Exception {

        final List<JrnlDiaryDto> jrnlDiaryList = jrnlDiaryService.getListDtoWithCache(searchParam);
        final String text = jrnlDiaryExportService.buildTxt(jrnlDiaryList, searchParam);
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        final String filename = "diaries_search_@" + DateUtils.getCurrDateStr(DatePtn.PDATE) + ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}