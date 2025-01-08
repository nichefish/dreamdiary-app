package io.nicheblog.dreamdiary.domain.board.post.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BasePostSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

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
public class BoardPostSearchParam
        extends BasePostSearchParam {

    /** 게시판 정의 */
    @Size(max = 50)
    private String boardDef;
}
