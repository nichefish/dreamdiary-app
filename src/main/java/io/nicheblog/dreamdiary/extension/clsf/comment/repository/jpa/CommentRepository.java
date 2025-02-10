package io.nicheblog.dreamdiary.extension.clsf.comment.repository.jpa;

import io.nicheblog.dreamdiary.extension.clsf.comment.entity.CommentEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * CommentRepository
 * <pre>
 *  댓글 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("commentRepository")
public interface CommentRepository
        extends BaseStreamRepository<CommentEntity, Integer> {

    //
}