package io.nicheblog.dreamdiary.feature.journal.interpretation.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.interpretation.entity.JournalInterpretationEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * JournalInterpretationRepository
 * <pre>
 *  저널 해석 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalInterpretationRepository
        extends BaseStreamRepository<JournalInterpretationEntity, Integer> {

    List<JournalInterpretationEntity> findAllByCreatedByAndRefIdInAndRefContentTypeInOrderByRefContentTypeAscRefIdAscSortOrderAsc(
            String createdBy,
            Collection<Integer> refIdList,
            Collection<ContentType> refContentTypeList
    );

    /**
     * 해당 참조 엔티티에서 해석 마지막 인덱스 조회
     *
     * @param refId 참조 엔티티 번호
     * @param refContentType 참조 컨텐츠 타입
     * @return {@link Optional} -- 해당 그룹의 마지막 sortOrder
     */
    @Query("SELECT MAX(interpretation.sortOrder) " +
            "FROM JournalInterpretationEntity interpretation " +
            "WHERE interpretation.refId = :refId " +
            "  AND interpretation.refContentType = :refContentType")
    Optional<Integer> findLastIndexByRef(
            final @Param("refId") Integer refId,
            final @Param("refContentType") ContentType refContentType
    );
}
