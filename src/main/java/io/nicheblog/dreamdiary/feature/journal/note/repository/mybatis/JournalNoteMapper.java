package io.nicheblog.dreamdiary.feature.journal.note.repository.mybatis;

import io.nicheblog.dreamdiary.feature.journal.note.model.JournalNoteDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * JournalNoteMapper
 * <pre>
 *  저널 일기 MyBatis 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface JournalNoteMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param id 조회할 게시글 번호 (삭제된 데이터)
     * @return {@link JournalNoteDto} -- 삭제된 저널 노트 데이터
     */
    JournalNoteDto getDeletedById(final @Param("id") Integer id);

    /**
     * 인덱스 갱신
     * @param updatedDto 수정된 dto
     * @return Integer -- 업데이트된 행 개수
     */
    Integer batchUpdateIdx(final List<JournalNoteDto> updatedDto);

    /**
     * 인덱스 갱신용 전체 목록 조회
     *
     * @param journalChapterId 상위 키값
     * @return Integer -- 업데이트된 행 개수
     */
    List<JournalNoteDto> findAllForReorder(final @Param("journalChapterId") Integer journalChapterId);
}

