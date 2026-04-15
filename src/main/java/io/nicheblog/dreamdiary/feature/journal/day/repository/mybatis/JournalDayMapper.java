package io.nicheblog.dreamdiary.feature.journal.day.repository.mybatis;

import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * JournalDayMapper
 * <pre>
 *  저널 일자 MyBatis 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface JournalDayMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param id 조회할 게시글 번호 (삭제된 데이터)
     * @return {@link JournalDayDto} -- 삭제된 저널 일자 데이터
     */
    JournalDayDto getDeletedById(final @Param("id") Integer id);
}
