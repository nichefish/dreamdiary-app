package io.nicheblog.dreamdiary.feature.journal.dream.controller;

import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamSearchParam;
import io.nicheblog.dreamdiary.feature.journal.dream.service.JournalDreamExportService;
import io.nicheblog.dreamdiary.feature.journal.dream.service.my.MyJournalDreamService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
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
 * JournalDreamExportController
 * <pre>
 *  저널 꿈 내보내기 Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JournalDreamExportController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    private final MyJournalDreamService myJournalDreamService;
    private final JournalDreamExportService journalDreamExportService;

    /**
     * 저널 꿈 txt 다룬로드
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = Url.JOURNAL_DREAMS_EXPORT, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<byte[]> journalDreamExportTxt(
            final JournalDreamSearchParam searchParam
    ) throws Exception {

        final List<JournalDreamDto> journalDreamList = myJournalDreamService.getMyListDto(searchParam);
        final String text = journalDreamExportService.buildTxt(journalDreamList, searchParam);
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        final String filename = "dreams_search_@" + DateUtils.getCurrDateStr(DatePtn.PDATE) + ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}

