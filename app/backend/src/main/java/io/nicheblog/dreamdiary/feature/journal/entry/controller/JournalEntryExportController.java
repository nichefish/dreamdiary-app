package io.nicheblog.dreamdiary.feature.journal.entry.controller;

import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntrySearchParam;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryExportService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.my.JournalEntryMyViewService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypeResolver;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 저널 엔트리 텍스트 내보내기 API 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
public class JournalEntryExportController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DAY_MONTHLY;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;

    private final JournalEntryMyViewService journalEntryMyViewService;
    private final JournalEntryExportService journalEntryExportService;
    private final JournalEntryTypeResolver typeResolver;

    /**
     * 엔트리 검색 결과를 타입 조건으로 텍스트 파일 내보내기한다.
     *
     * @param searchParam 검색 조건
     * @param type 엔트리 타입(DIARY/DREAM 또는 JOURNAL_DIARY/JOURNAL_DREAM)
     * @param includeReflection 해석 포함 여부 (기본 true). 포함 시 각 엔트리의 target 리플렉션 본문을 함께 내보낸다.
     * @return 텍스트 첨부 응답
     * @throws Exception 내보내기 처리 중 예외
     */
    @GetMapping(value = Url.JOURNAL_ENTRIES_EXPORT, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<byte[]> journalEntryExportTxt(
            final JournalEntrySearchParam searchParam,
            final @RequestParam(value = "type", required = false) String type,
            final @RequestParam(value = "includeReflection", defaultValue = "true") boolean includeReflection
    ) throws Exception {
        final ContentType contentType = typeResolver.resolveByRawTypeOrFallback(type, searchParam.getContentType());
        final List<JournalEntryDto> journalEntryList = journalEntryMyViewService.getMyList(searchParam, contentType);
        return buildTextAttachment(journalEntryList, searchParam, contentType == ContentType.JOURNAL_DIARY ? "diaries" : "dreams", includeReflection);
    }

    /**
     * 텍스트 본문을 파일 첨부 응답으로 구성한다.
     *
     * @param journalEntryList 내보낼 엔트리 목록
     * @param searchParam 검색 조건
     * @param filenamePrefix 파일명 접두사
     * @param includeReflection 해석 포함 여부 (기본 true)
     * @return 텍스트 첨부 응답
     * @throws Exception 텍스트 생성 중 예외
     */
    private ResponseEntity<byte[]> buildTextAttachment(
            final List<JournalEntryDto> journalEntryList,
            final JournalEntrySearchParam searchParam,
            final String filenamePrefix,
            final boolean includeReflection
    ) throws Exception {
        final String text = journalEntryExportService.buildTxt(journalEntryList, searchParam, includeReflection);
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        final String filename = filenamePrefix + "_search_@" + DateUtils.getCurrDateStr(DatePtn.PDATE) + ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }

}
