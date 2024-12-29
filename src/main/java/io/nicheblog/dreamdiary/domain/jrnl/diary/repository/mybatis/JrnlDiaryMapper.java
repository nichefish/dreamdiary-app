package io.nicheblog.dreamdiary.domain.jrnl.diary.repository.mybatis;

import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * JrnlDiaryMapper
 * <pre>
 *  저널 일기 MyBatis 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface JrnlDiaryMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param postNo 조회할 게시글 번호 (삭제된 데이터)
     * @return {@link JrnlDiaryDto} -- 삭제된 저널 일기 데이터
     */
    JrnlDiaryDto getDeletedByPostNo(final @Param("postNo") Integer postNo);
}
