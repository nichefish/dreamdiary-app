package io.nicheblog.dreamdiary.feature.attachable.comment.repository.mybatis;

import io.nicheblog.dreamdiary.feature.attachable.comment.model.CommentDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * CommentMapper
 * <pre>
 *  댓글 MyBatis 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface CommentMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param id - 조회할 게시글 번호 (삭제된 데이터)
     * @return {@link CommentDto} -- 삭제된 댓글 데이터
     */
    CommentDto getDeletedById(final @Param("id") Integer id);
}
