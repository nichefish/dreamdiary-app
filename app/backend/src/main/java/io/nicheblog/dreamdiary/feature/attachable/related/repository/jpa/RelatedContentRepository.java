package io.nicheblog.dreamdiary.feature.attachable.related.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.related.entity.RelatedContentEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * RelatedContentRepository
 * <pre>
 *  관련글 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface RelatedContentRepository
        extends BaseStreamRepository<RelatedContentEntity, Integer> {

    @Query("SELECT rc " +
            "FROM RelatedContentEntity rc " +
            "WHERE rc.createdBy = :createdBy " +
            "  AND ((rc.leftId = :id AND rc.leftContentType = :contentType) " +
            "    OR (rc.rightId = :id AND rc.rightContentType = :contentType)) " +
            "ORDER BY rc.createdAt DESC")
    List<RelatedContentEntity> findAllByRef(
            final @Param("id") Integer id,
            final @Param("contentType") String contentType,
            final @Param("createdBy") String createdBy
    );

    @Query("SELECT rc " +
            "FROM RelatedContentEntity rc " +
            "WHERE rc.createdBy = :createdBy " +
            "  AND (rc.leftId IN :idSet OR rc.rightId IN :idSet) " +
            "ORDER BY rc.createdAt DESC")
    List<RelatedContentEntity> findAllByAnyRefIdIn(
            final @Param("idSet") Set<Integer> idSet,
            final @Param("createdBy") String createdBy
    );

    @Query(value = "SELECT * " +
            "FROM related_content rc " +
            "WHERE rc.left_id = :leftId " +
            "  AND rc.left_content_type = :leftContentType " +
            "  AND rc.right_id = :rightId " +
            "  AND rc.right_content_type = :rightContentType " +
            "  AND rc.created_by = :createdBy " +
            "LIMIT 1", nativeQuery = true)
    Optional<RelatedContentEntity> findAnyByPair(
            final @Param("leftId") Integer leftId,
            final @Param("leftContentType") String leftContentType,
            final @Param("rightId") Integer rightId,
            final @Param("rightContentType") String rightContentType,
            final @Param("createdBy") String createdBy
    );

    @Modifying
    // 변경 전: SET rc.deletedAt = 'Y' — 날짜 필드에 문자열 리터럴을 대입하던 기존 결함 (LocalDateTime 전환 시 부트 검증 실패 유발).
    // 변경 후: soft delete 의미에 맞게 현재 시각을 기록한다.
    @Query("UPDATE RelatedContentEntity rc " +
            "SET rc.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE rc.createdBy = :createdBy " +
            "  AND ((rc.leftId = :id AND rc.leftContentType = :contentType) " +
            "    OR (rc.rightId = :id AND rc.rightContentType = :contentType))")
    int softDeleteAllByRef(
            final @Param("id") Integer id,
            final @Param("contentType") String contentType,
            final @Param("createdBy") String createdBy
    );
}

