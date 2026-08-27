package io.nicheblog.dreamdiary.feature.journal.entry.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JournalEntryPreviewRequest
 * <pre>
 *  작성 중 본문 미리보기 요청. 미저장 HTML을 받아 마크다운 렌더만 수행한다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
public class JournalEntryPreviewRequest {

    /** TinyMCE가 유지 중인 미저장 본문 HTML. */
    private String content;
}
