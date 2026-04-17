package io.nicheblog.dreamdiary.feature.journal.interpretation.repository.mybatis;

import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * JournalInterpretationMapper
 * <pre>
 *  저널 해석 MyBatis 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface JournalInterpretationMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param id 조회할 게시글 번호 (삭제된 데이터)
     * @return {@link JournalInterpretationDto} -- 삭제된 저널 일기 데이터
     */
    JournalInterpretationDto getDeletedById(final @Param("id") Integer id);
    
    /**
     * 인덱스 갱신
     * @param updatedDto 수정된 dto
     * @return Integer -- 업데이트된 행 개수
     */
    Integer batchUpdateIdx(final List<JournalInterpretationDto> updatedDto);

    /**
     * 인덱스 갱신용 전체 목록 조회
     *
     * @param journalDreamId 상위 키값
     * @return Integer -- 업데이트된 행 개수
     */
    List<JournalInterpretationDto> findAllForReorder(final @Param("journalDreamId") Integer journalDreamId);
}

