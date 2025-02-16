package io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn;

import io.nicheblog.dreamdiary.extension.clsf.comment.model.CommentDto;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * CommentCmpstn
 * <pre>
 *  위임:: 댓글 관련 정보. (dto level)
 * </pre>
 *
 * @author nichefish
 * @see CommentCmpstnModule
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCmpstn
        implements Serializable {

    /** 댓글 목록 */
    private List<CommentDto> list;

    /** 댓글 갯수 */
    @Builder.Default
    private Integer cnt = 0;

    /** 댓글 존재 여부 */
    @Builder.Default
    private Boolean hasComment = false;
}