package io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * CommentCmpstnModule
 * <pre>
 *   댓글 합성composition 모듈 인터페이스.
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface CommentCmpstnModule {

    /** Getter */
    CommentCmpstn getComment();

    /** Setter */
    void setComment(CommentCmpstn cmpstn);
}

