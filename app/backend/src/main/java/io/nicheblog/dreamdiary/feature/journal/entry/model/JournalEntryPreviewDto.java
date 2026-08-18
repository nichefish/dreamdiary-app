package io.nicheblog.dreamdiary.feature.journal.entry.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JournalEntryPreviewDto
 * <pre>
 *  작성 중 본문 미리보기 응답. 목록과 동일한 markdownContent HTML을 담는다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryPreviewDto {

    /** 목록 렌더와 같은 MarkdownUtils 결과 HTML. */
    private String markdownContent;
}
