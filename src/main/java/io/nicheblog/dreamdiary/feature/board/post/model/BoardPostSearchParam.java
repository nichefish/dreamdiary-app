package io.nicheblog.dreamdiary.feature.board.post.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.param.BaseAttachableSearchParam;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Size;

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

    /** 글 분류 코드 (목록 필터) */
    @Size(max = 50)
    private String categoryCode;
}
