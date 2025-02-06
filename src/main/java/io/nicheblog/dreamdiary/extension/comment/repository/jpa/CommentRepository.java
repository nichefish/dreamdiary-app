package io.nicheblog.dreamdiary.extension.comment.repository.jpa;

import io.nicheblog.dreamdiary.extension.comment.entity.CommentEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    /** entityGraph */
    @Override
    @EntityGraph(value = "CommentEntity.withCtgrCd")
    Optional<CommentEntity> findById(final @NotNull Integer key);
}