package io.nicheblog.dreamdiary.feature.board.post.repository.jpa;

import io.nicheblog.dreamdiary.feature.board.post.entity.BoardPostEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * BoardPostRepository
 * <pre>
 *  게시판 게시물 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("boardPostRepository")
public interface BoardPostRepository
        extends BaseStreamRepository<BoardPostEntity, Integer> {
    //
}
