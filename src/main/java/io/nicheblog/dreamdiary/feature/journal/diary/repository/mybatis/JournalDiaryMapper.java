package io.nicheblog.dreamdiary.feature.journal.diary.repository.mybatis;

import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * JournalDiaryMapper
 * <pre>
 *  저널 일기 MyBatis 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface JournalDiaryMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param id 조회할 게시글 번호 (삭제된 데이터)
     * @return {@link JournalDiaryDto} -- 삭제된 저널 일기 데이터
     */
    JournalDiaryDto getDeletedById(final @Param("id") Integer id);

    /**
     * 인덱스 갱신
     * @param updatedDto 수정된 dto
     * @return Integer -- 업데이트된 행 개수
     */
    Integer batchUpdateIdx(final List<JournalDiaryDto> updatedDto);

    /**
     * 인덱스 갱신용 전체 목록 조회
     *
     * @param journalChapterId 상위 키값
     * @return Integer -- 업데이트된 행 개수
     */
    List<JournalDiaryDto> findAllForReorder(final @Param("journalChapterId") Integer journalChapterId);
}

