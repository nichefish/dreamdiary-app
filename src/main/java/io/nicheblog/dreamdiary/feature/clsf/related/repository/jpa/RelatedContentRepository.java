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
            "  AND ((rc.leftPostNo = :postNo AND rc.leftContentType = :contentType) " +
            "    OR (rc.rightPostNo = :postNo AND rc.rightContentType = :contentType)) " +
            "ORDER BY rc.regDt DESC")
    List<RelatedContentEntity> findAllByRef(
            final @Param("postNo") Integer postNo,
            final @Param("contentType") String contentType,
            final @Param("regstrId") String regstrId
    );

    @Query("SELECT rc " +
            "FROM RelatedContentEntity rc " +
            "WHERE rc.regstrId = :regstrId " +
            "  AND (rc.leftPostNo IN :postNoSet OR rc.rightPostNo IN :postNoSet) " +
            "ORDER BY rc.regDt DESC")
    List<RelatedContentEntity> findAllByAnyRefPostNoIn(
            final @Param("postNoSet") Set<Integer> postNoSet,
            final @Param("regstrId") String regstrId
    );

    @Query(value = "SELECT * " +
            "FROM related_content rc " +
            "WHERE rc.left_post_no = :leftPostNo " +
            "  AND rc.left_content_type = :leftContentType " +
            "  AND rc.right_post_no = :rightPostNo " +
            "  AND rc.right_content_type = :rightContentType " +
            "  AND rc.regstr_id = :regstrId " +
            "LIMIT 1", nativeQuery = true)
    Optional<RelatedContentEntity> findAnyByPair(
            final @Param("leftPostNo") Integer leftPostNo,
            final @Param("leftContentType") String leftContentType,
            final @Param("rightPostNo") Integer rightPostNo,
            final @Param("rightContentType") String rightContentType,
            final @Param("regstrId") String regstrId
    );

    @Modifying
    @Query("UPDATE RelatedContentEntity rc " +
            "SET rc.delYn = 'Y' " +
            "WHERE rc.regstrId = :regstrId " +
            "  AND ((rc.leftPostNo = :postNo AND rc.leftContentType = :contentType) " +
            "    OR (rc.rightPostNo = :postNo AND rc.rightContentType = :contentType))")
    int softDeleteAllByRef(
            final @Param("postNo") Integer postNo,
            final @Param("contentType") String contentType,
            final @Param("regstrId") String regstrId
    );
}
