package io.nicheblog.dreamdiary.extension.comment.repository.mybatis;

import io.nicheblog.dreamdiary.extension.comment.model.CommentDto;
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
     * @param postNo - 조회할 게시글 번호 (삭제된 데이터)
     * @return {@link CommentDto} -- 삭제된 댓글 데이터
     */
    CommentDto getDeletedByPostNo(final @Param("postNo") Integer postNo);
}
