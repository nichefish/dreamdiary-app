package io.nicheblog.dreamdiary.feature.clsf.meta.repository.jpa;

import io.nicheblog.dreamdiary.feature.clsf.meta.entity.MetaEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * MetaRepository
 * <pre>
 *  메타 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("metaRepository")
public interface MetaRepository
        extends BaseStreamRepository<MetaEntity, Integer> {

    /**
     * 메타명 + 카테고리명으로 테이블 조회
     *
     * @param tagNm 조회할 태그명
     * @param ctgr 조회할 카테고리명
     * @return 태그명과 카테고리명에 해당하는 TagEntity를 포함하는 Optional 객체
     */
    @Query(value = "SELECT * FROM meta m WHERE m.meta_nm = :tagNm AND m.ctgr = :ctgr AND m.label = :label AND m.deleted_at IS NULL", nativeQuery = true)
    Optional<MetaEntity> findByMetaNmAndCtgrAndLabel(final String tagNm, final String ctgr, final String label);

    /**
     * 컨텐츠 타입별 메타 개수 조회
     *
     * @param metaId 조회할 메타 ID
     * @param refContentType 조회할 컨텐츠 타입 (필터링 조건, null 또는 빈 문자열일 경우 조건 무시)
     * @return 메타 번호와 컨텐츠 타입에 해당하는 메타 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(ct.id) " +
            "FROM MetaContentEntity ct " +
            "INNER JOIN fetch MetaEntity meta ON meta.id = ct.metaId " +
            "WHERE ct.metaId = :metaId " +
            " AND (:refContentType IS NULL OR :refContentType = '' OR ct.refContentType = :refContentType)" +
            " AND (ct.createdBy = :createdBy)")
    Integer countMetaSize(final @Param("metaId") Integer metaId, final @Param("refContentType") String refContentType, final String createdBy);
}
