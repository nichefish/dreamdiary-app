package io.nicheblog.dreamdiary.feature.board.post.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.param.BaseAttachableSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * BoardPostSearchParam
 * <pre>
 *  게시판 게시물 검색 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BoardPostSearchParam extends BaseAttachableSearchParam {

    /** 말머리 ID (목록 필터) */
    private Integer prefixId;
}
