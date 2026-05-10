package io.nicheblog.dreamdiary.feature.board.post.repository.jpa;

import io.nicheblog.dreamdiary.feature.board.post.entity.BoardPostEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * BoardPostRepository
 * <pre>
 *  게시판 게시물 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface BoardPostRepository
        extends BaseStreamRepository<BoardPostEntity, Integer> {

    /**
     * 게시판 관리 목록용: {@code content_type} 별 게시글 건수 (삭제 제외는 엔티티 {@code @Where} 로 반영).
     *
     * @param keys {@code board.board_key} 목록
     * @return [0]=contentType 문자열, [1]=건수
     */
    @Query("SELECT bp.contentType, COUNT(bp) FROM BoardPostEntity bp WHERE bp.contentType IN :keys GROUP BY bp.contentType")
    List<Object[]> countGroupedByContentTypeIn(@Param("keys") List<String> keys);
}

