package io.nicheblog.dreamdiary.extension.clsf.comment.entity.embed;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;

/**
 * CommentEmbedModule
 * <pre>
 *   댓글 Embed 모듈 인터페이스.
 * </pre>
 *
 * @author nichefish
 * @see MapstructHelper
 */
public interface CommentEmbedModule {

    /** Getter */
    CommentEmbed getComment();

    /** Setter */
    void setComment(CommentEmbed embed);
}
