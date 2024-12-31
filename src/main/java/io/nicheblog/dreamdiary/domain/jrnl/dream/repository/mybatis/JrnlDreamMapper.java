package io.nicheblog.dreamdiary.domain.jrnl.dream.repository.mybatis;

import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * JrnlDreamMapper
 * <pre>
 *  저널 꿈 MyBatis 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface JrnlDreamMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param postNo 조회할 게시글 번호
     * @return {@link JrnlDreamDto} -- 삭제된 저널 꿈 데이터
     */
    JrnlDreamDto getDeletedByPostNo(final @Param("postNo") Integer postNo);
}
