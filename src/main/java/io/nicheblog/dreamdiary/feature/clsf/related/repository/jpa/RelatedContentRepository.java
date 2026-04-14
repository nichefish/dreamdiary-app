package io.nicheblog.dreamdiary.feature.clsf.related.repository.jpa;

import io.nicheblog.dreamdiary.feature.clsf.related.entity.RelatedContentEntity;
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
@Repository("relatedContentRepository")
public interface RelatedContentRepository
        extends BaseStreamRepository<RelatedContentEntity, Integer> {

    @Query("SELECT rc " +
            "FROM RelatedContentEntity rc " +
            "WHERE rc.regstrId = :regstrId " +
            "  AND ((rc.leftId = :id AND rc.leftContentType = :contentType) " +
            "    OR (rc.rightId = :id AND rc.rightContentType = :contentType)) " +
            "ORDER BY rc.regDt DESC")
    List<RelatedContentEntity> findAllByRef(
            final @Param("id") Integer id,
            final @Param("contentType") String contentType,
            final @Param("regstrId") String regstrId
    );

    @Query("SELECT rc " +
            "FROM RelatedContentEntity rc " +
            "WHERE rc.regstrId = :regstrId " +
            "  AND (rc.leftId IN :idSet OR rc.rightId IN :idSet) " +
            "ORDER BY rc.regDt DESC")
    List<RelatedContentEntity> findAllByAnyRefIdIn(
            final @Param("idSet") Set<Integer> idSet,
            final @Param("regstrId") String regstrId
    );

    @Query(value = "SELECT * " +
            "FROM related_content rc " +
            "WHERE rc.left_id = :leftId " +
            "  AND rc.left_content_type = :leftContentType " +
            "  AND rc.right_id = :rightId " +
            "  AND rc.right_content_type = :rightContentType " +
            "  AND rc.regstr_id = :regstrId " +
            "LIMIT 1", nativeQuery = true)
    Optional<RelatedContentEntity> findAnyByPair(
            final @Param("leftId") Integer leftId,
            final @Param("leftContentType") String leftContentType,
            final @Param("rightId") Integer rightId,
            final @Param("rightContentType") String rightContentType,
            final @Param("regstrId") String regstrId
    );

    @Modifying
    @Query("UPDATE RelatedContentEntity rc " +
            "SET rc.delYn = 'Y' " +
            "WHERE rc.regstrId = :regstrId " +
            "  AND ((rc.leftId = :id AND rc.leftContentType = :contentType) " +
            "    OR (rc.rightId = :id AND rc.rightContentType = :contentType))")
    int softDeleteAllByRef(
            final @Param("id") Integer id,
            final @Param("contentType") String contentType,
            final @Param("regstrId") String regstrId
    );
}
