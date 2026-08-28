package io.nicheblog.dreamdiary.feature.attachable.tag.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentParam;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * TagContentRepository
 * <pre>
 *  태그-컨텐츠 정보 repository 인터페이스.
 *  (cascade하지 않고 수동 관리)
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface TagContentRepository
        extends BaseStreamRepository<TagContentEntity, Integer> {

    /**
     * 특정 게시물에 대해 태그 정보와 연결되지 않는 태그-컨텐츠 삭제.
     *
     * @param param - 삭제할 대상의 파라미터 (게시글 번호, 컨텐츠 타입, 태그 이름, 카테고리 포함)
     */
    @Modifying
    @Query("UPDATE TagContentEntity ct SET ct.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE ct.refId = :#{#param.refId} " +
            "  AND ct.refContentType = :#{#param.refContentType} " +
            "  AND ct.createdBy = :#{#param.createdBy} " +
            "  AND ct.deletedAt IS NULL " +
            "  AND EXISTS (SELECT 1 FROM TagEntity t " +
            "               LEFT JOIN t.tagCategory tc " +
            "               WHERE t.id = ct.tagId " +
            "                 AND t.name = :#{#param.name} " +
            "                 AND ( " +
            "                      ((:#{#param.ctgr} IS NULL OR :#{#param.ctgr} = '') AND t.tagCategoryId IS NULL) " +
            "                      OR tc.name = :#{#param.ctgr} " +
            "                 ))")
    void deleteObsoleteTagContents(final @Param("param") TagContentParam param);

    /**
     * (tag_id, ref_id, ref_content_type, created_by) 쌍의 소프트 삭제 포함 기존 행을 조회한다.
     * 유니크 키(uk_tag_content_pair)가 deleted_at 을 제외하므로, 재등록 시 이 조회로 복원 대상을 찾는다.
     *
     * @param tagId 태그 ID
     * @param refId 참조 글 번호
     * @param refContentType 참조 콘텐츠 타입
     * @param createdBy 등록자 계정명
     * @return 소프트 삭제 포함 기존 연결 (없으면 empty)
     */
    @Query(value = "SELECT * FROM tag_content " +
            "WHERE tag_id = :tagId AND ref_id = :refId " +
            "  AND ref_content_type = :refContentType AND created_by = :createdBy " +
            "ORDER BY id LIMIT 1", nativeQuery = true)
    Optional<TagContentEntity> findAnyByPair(
            final @Param("tagId") Integer tagId,
            final @Param("refId") Integer refId,
            final @Param("refContentType") String refContentType,
            final @Param("createdBy") String createdBy);

    /**
     * 소프트 삭제된 태그-컨텐츠 연결을 복원한다(deleted_at = NULL).
     *
     * @param id 복원할 연결 ID
     * @return 갱신된 행 수
     */
    @Modifying
    @Query(value = "UPDATE tag_content SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    int reviveById(final @Param("id") Integer id);

    /**
     * 주어진 tag ID 목록 중 tag_content 참조가 0인 고아 태그 ID를 반환한다.
     *
     * @param tagIds 확인할 태그 ID 목록
     * @return 참조 카운트가 0인 태그 ID 목록
     */
    @Query("SELECT t.id FROM TagEntity t " +
            "WHERE t.id IN :tagIds " +
            "  AND NOT EXISTS (SELECT 1 FROM TagContentEntity tc WHERE tc.tagId = t.id)")
    List<Integer> findOrphanTagIds(final @Param("tagIds") Collection<Integer> tagIds);
}

