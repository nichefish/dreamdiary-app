package io.nicheblog.dreamdiary.feature.attachable.related.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * RelatedContentFlowDto
 * <pre>
 *  앵커 엔트리에서 탐색한 FLOW 연결 컴포넌트 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Builder
public class RelatedContentFlowDto {

    private Integer anchorId;
    private String anchorContentType;

    @Builder.Default
    private List<RelatedContentFlowEntryDto> entryList = List.of();

    @Builder.Default
    private List<RelatedContentDto> relationList = List.of();
}
