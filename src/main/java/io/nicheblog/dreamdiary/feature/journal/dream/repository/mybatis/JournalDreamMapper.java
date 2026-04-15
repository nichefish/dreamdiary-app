package io.nicheblog.dreamdiary.feature.journal.dream.repository.mybatis;

import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * JournalDreamMapper
 * <pre>
 *  저널 꿈 MyBatis 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface JournalDreamMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param id 조회할 게시글 번호
     * @return {@link JournalDreamDto} -- 삭제된 저널 꿈 데이터
     */
    JournalDreamDto getDeletedById(final @Param("id") Integer id);
    
    /**
     * 인덱스 갱신
     * @param updatedDto 수정된 dto
     * @return Integer -- 업데이트된 행 개수
     */
    Integer batchUpdateIdx(final List<JournalDreamDto> updatedDto);

    /**
     * 인덱스 갱신용 전체 목록 조회
     *
     * @param journalDayId 상위 키값
     * @return Integer -- 업데이트된 행 개수
     */
    List<JournalDreamDto> findAllForReorder(final @Param("journalDayId") Integer journalDayId);
}

