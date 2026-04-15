package io.nicheblog.dreamdiary.feature.journal.chapter.repository.mybatis;

import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.intrpt.model.JournalIntrptDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * JournalChapterMapper
 * <pre>
 *  저널 챕터 MyBatis 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface JournalChapterMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param id 조회할 게시글 번호 (삭제된 데이터)
     * @return {@link JournalIntrptDto} -- 삭제된 저널 일기 데이터
     */
    JournalChapterDto getDeletedById(final @Param("id") Integer id);

    /**
     * 인덱스 갱신
     * @param updatedDto 수정된 dto
     * @return Integer -- 업데이트된 행 개수
     */
    Integer batchUpdateIdx(final List<JournalChapterDto> updatedDto);

    /**
     * 인덱스 갱신용 전체 목록 조회
     *
     * @param journalDayId 상위 키값
     * @return Integer -- 업데이트된 행 개수
     */
    List<JournalChapterDto> findAllForReorder(final @Param("journalDayId") Integer journalDayId);
}

