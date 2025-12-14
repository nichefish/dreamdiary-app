package io.nicheblog.dreamdiary.domain.jrnl.entry.repository.mybatis;

import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.model.JrnlIntrptDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * JrnlEntryMapper
 * <pre>
 *  저널 항목 MyBatis 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface JrnlEntryMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param postNo 조회할 게시글 번호 (삭제된 데이터)
     * @return {@link JrnlIntrptDto} -- 삭제된 저널 일기 데이터
     */
    JrnlEntryDto getDeletedByPostNo(final @Param("postNo") Integer postNo);

    /**
     * 인덱스 갱신
     * @param updatedDto 수정된 dto
     * @return Integer -- 업데이트된 행 개수
     */
    Integer batchUpdateIdx(final List<JrnlEntryDto> updatedDto);

    /**
     * 인덱스 갱신용 전체 목록 조회
     *
     * @param jrnlDayNo 상위 키값
     * @return Integer -- 업데이트된 행 개수
     */
    List<JrnlEntryDto> findAllForReorder(final @Param("jrnlDayNo") Integer jrnlDayNo);
}
