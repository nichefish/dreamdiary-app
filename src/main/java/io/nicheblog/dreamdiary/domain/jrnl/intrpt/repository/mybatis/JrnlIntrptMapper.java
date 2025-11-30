package io.nicheblog.dreamdiary.domain.jrnl.intrpt.repository.mybatis;

import io.nicheblog.dreamdiary.domain.jrnl.intrpt.model.JrnlIntrptDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * JrnlIntrptMapper
 * <pre>
 *  저널 해석 MyBatis 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface JrnlIntrptMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param postNo 조회할 게시글 번호 (삭제된 데이터)
     * @return {@link JrnlIntrptDto} -- 삭제된 저널 일기 데이터
     */
    JrnlIntrptDto getDeletedByPostNo(final @Param("postNo") Integer postNo);
}
