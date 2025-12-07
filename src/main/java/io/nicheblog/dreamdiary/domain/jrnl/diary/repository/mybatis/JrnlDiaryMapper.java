package io.nicheblog.dreamdiary.domain.jrnl.diary.repository.mybatis;

import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /**
     * 인덱스 갱신
     * @param updatedDto 수정된 dto
     * @return Integer -- 업데이트된 행 개수
     */
    Integer batchUpdateIdx(final List<JrnlDiaryDto> updatedDto);

    /**
     * 인덱스 갱신용 전체 목록 조회
     *
     * @param jrnlEntryNo 상위 키값
     * @return Integer -- 업데이트된 행 개수
     */
    List<JrnlDiaryDto> findAllForReorder(final @Param("jrnlEntryNo") Integer jrnlEntryNo);
}
