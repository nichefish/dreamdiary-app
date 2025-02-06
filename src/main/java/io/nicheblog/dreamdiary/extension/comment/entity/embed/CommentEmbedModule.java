package io.nicheblog.dreamdiary.extension.comment.entity.embed;

/**
 * CommentEmbedModule
 * <pre>
 *   댓글 Embed 모듈 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface CommentEmbedModule {

    /** Getter */
    CommentEmbed getComment();

    /** Setter */
    void setComment(CommentEmbed embed);
}
