package io.nicheblog.dreamdiary.feature.journal.interpretation.repository.mybatis;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
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
     * @return {@link JournalInterpretationDto} -- 삭제된 저널 해석 데이터
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
     * @param refId 참조 엔티티 번호
     * @param refContentType 참조 컨텐츠 타입
     * @return {@link List} -- 재정렬 대상 목록
     */
    List<JournalInterpretationDto> findAllForReorder(
            final @Param("refId") Integer refId,
            final @Param("refContentType") ContentType refContentType
    );
}
