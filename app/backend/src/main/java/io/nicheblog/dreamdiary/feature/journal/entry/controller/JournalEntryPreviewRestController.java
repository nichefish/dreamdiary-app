package io.nicheblog.dreamdiary.feature.journal.entry.controller;

import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPreviewDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPreviewRequest;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * JournalEntryPreviewRestController
 * <pre>
 *  작성 중 저널 엔트리·리플렉션 본문 미리보기 REST.
 *  저장하지 않고 MarkdownUtils만 적용한다.
 * </pre>
 *
 * @author nichefish
 */
@Log4j2
@RestController
public class JournalEntryPreviewRestController {

    /**
     * 미저장 본문 HTML을 목록과 같은 markdownContent로 렌더한다.
     *
     * @param request 작성 중 본문
     * @return Ajax 응답
     */
    @PostMapping(Url.JOURNAL_ENTRY_PREVIEW)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> preview(final @RequestBody JournalEntryPreviewRequest request) {
        final String content = request == null || request.getContent() == null ? "" : request.getContent();
        log.info("journal entry preview render requested. contentLength={}", content.length());
        final JournalEntryPreviewDto dto = new JournalEntryPreviewDto(MarkdownUtils.markdown(content));
        return ResponseEntity.ok(
                AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(dto)
        );
    }
}
