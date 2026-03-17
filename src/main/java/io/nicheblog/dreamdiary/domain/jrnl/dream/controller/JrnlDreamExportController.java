package io.nicheblog.dreamdiary.domain.jrnl.dream.controller;

import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamSearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamExportService;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamService;
import io.nicheblog.dreamdiary.domain.clsf.tag.handler.TagProcEventListener;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
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

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JrnlDreamExportController
 * <pre>
 *  저널 꿈 내보내기 Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JrnlDreamExportController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    private final JrnlDreamService jrnlDreamService;
    private final JrnlDreamExportService jrnlDreamExportService;

    /**
     * 저널 꿈 txt 다룬로드
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see TagProcEventListener
     */
    @GetMapping(value = Url.JRNL_DREAMS_EXPORT, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<byte[]> jrnlDreamExportTxt(
            final JrnlDreamSearchParam searchParam
    ) throws Exception {

        final List<JrnlDreamDto> jrnlDreamList = jrnlDreamService.getListDtoWithCache(searchParam);
        final String text = jrnlDreamExportService.buildTxt(jrnlDreamList, searchParam);
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        final String filename = "dreams_search_@" + DateUtils.getCurrDateStr(DatePtn.PDATE) + ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}
