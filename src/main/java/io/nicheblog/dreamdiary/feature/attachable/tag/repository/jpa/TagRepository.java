package io.nicheblog.dreamdiary.feature.attachable.tag.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * TagRepository
 * <pre>
 *  태그 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface TagRepository
        extends BaseStreamRepository<TagEntity, Integer> {

    /**
     * 태그명 + 카테고리명으로 조회
     *
     * @param tagNm 조회할 태그명
     * @param ctgr 조회할 카테고리명
     * @return 태그명과 카테고리명에 해당하는 TagEntity를 포함하는 Optional 객체
     */
    @Query(value = "SELECT t.* " +
        "FROM tag t " +
        "LEFT JOIN tag_category tc ON tc.id = t.tag_category_id AND tc.deleted_at IS NULL " +
        "WHERE t.tag_nm = :tagNm " +
        "  AND t.deleted_at IS NULL " +
        "  AND ( " +
        "       ((:ctgr IS NULL OR :ctgr = '') AND t.tag_category_id IS NULL) " +
        "       OR tc.ctgr_nm = :ctgr " +
        "  )", nativeQuery = true)
    Optional<TagEntity> findByTagNmAndCtgr(final String tagNm, final String ctgr);

    /**
     * 태그 ID로 조회
     *
     * @param ids 태그 ID 목록
     * @return 태그명과 카테고리명에 해당하는 TagEntity를 포함하는 Optional 객체
     */
    List<TagEntity> findAllByIdIn(List<Integer> ids);

    /**
     * 컨텐츠 타입별 태그 개수 조회
     *
     * @param tagId - 조회할 태그 ID
     * @param refContentType - 조회할 컨텐츠 타입 (필터링 조건, null 또는 빈 문자열일 경우 조건 무시)
     * @return Integer - 태그 ID와 컨텐츠 타입에 해당하는 태그 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(ct.id) " +
            "FROM TagContentEntity ct " +
            "INNER JOIN fetch TagEntity tag ON tag.id = ct.tagId " +
            "WHERE ct.tagId = :tagId " +
            " AND (:refContentType IS NULL OR :refContentType = '' OR ct.refContentType = :refContentType)" +
            " AND (ct.createdBy = :createdBy)")
    Integer countTagSize(final @Param("tagId") Integer tagId, final @Param("refContentType") String refContentType, final String createdBy);
}

