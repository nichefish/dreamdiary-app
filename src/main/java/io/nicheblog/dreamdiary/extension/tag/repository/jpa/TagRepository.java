package io.nicheblog.dreamdiary.extension.tag.repository.jpa;

import io.nicheblog.dreamdiary.extension.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * TagRepository
 * <pre>
 *  태그 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("tagRepository")
public interface TagRepository
        extends BaseStreamRepository<TagEntity, Integer> {

    /**
     * 태그명으로 테이블 조회
     *
     * @param tagNm - 조회할 태그명
     * @return Optional<TagEntity> - 태그명에 해당하는 TagEntity를 포함하는 Optional 객체
     */
    Optional<TagEntity> findByTagNm(final String tagNm);
    
    /**
     * 태그명 + 카테고리명으로 테이블 조회
     *
     * @param tagNm - 조회할 태그명
     * @param ctgr - 조회할 카테고리명
     * @return Optional<TagEntity> - 태그명과 카테고리명에 해당하는 TagEntity를 포함하는 Optional 객체
     */
    Optional<TagEntity> findByTagNmAndCtgr(final String tagNm, final String ctgr);

    /**
     * 컨텐츠 타입별 태그 개수 조회
     *
     * @param tagNo - 조회할 태그 번호
     * @param refContentType - 조회할 컨텐츠 타입 (필터링 조건, null 또는 빈 문자열일 경우 조건 무시)
     * @return Integer - 태그 번호와 컨텐츠 타입에 해당하는 태그 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(ct.contentTagNo) " +
            "FROM ContentTagEntity ct " +
            "INNER JOIN fetch TagEntity tag ON tag.tagNo = ct.refTagNo " +
            "WHERE ct.refTagNo = :tagNo " +
            " AND (:refContentType IS NULL OR :refContentType = '' OR ct.refContentType = :refContentType)" +
            " AND (ct.regstrId = :regstrId)")
    Integer countTagSize(final @Param("tagNo") Integer tagNo, final @Param("refContentType") String refContentType, final String regstrId);
}
