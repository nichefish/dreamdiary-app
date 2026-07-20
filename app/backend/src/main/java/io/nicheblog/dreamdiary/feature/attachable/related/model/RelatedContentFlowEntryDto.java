package io.nicheblog.dreamdiary.feature.attachable.related.model;

import lombok.Builder;
import lombok.Getter;

/**
 * RelatedContentFlowEntryDto
 * <pre>
 *  FLOW 종단 보기에 표시할 저널 엔트리 요약 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Builder
public class RelatedContentFlowEntryDto {

    private Integer id;
    private String contentType;
    private String title;
    private String content;
    private String markdownContent;
    private String stdrdDt;
    private Integer journalDayId;
    private Integer journalChapterId;
    private Integer chapterSortOrder;
    private Integer sortOrder;
    private Boolean anchor;
}
