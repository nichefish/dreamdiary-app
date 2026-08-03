package io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadCandidateProjection;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JournalThreadRepository
 * <pre>
 *  저널 스레드 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalThreadRepository
        extends BaseStreamRepository<JournalThreadEntity, Integer> {

    Optional<JournalThreadEntity> findByIdAndCreatedBy(Integer id, String createdBy);

    /**
     * 엔트리 소속 메뉴에 노출할 스레드 후보를 조회한다.
     * <p>
     * 현재 엔트리가 속한 스레드를 먼저 두고, 최근 소속 추가 시각, 활성 소속 수,
     * 스레드 수정·생성 시각 순으로 정렬한다. 스레드와 소속 모두 현재 사용자 범위로 제한한다.
     * 소프트 삭제된 소속은 사용 빈도와 최근 사용 집계에서 제외한다.
     * 기본({@code includeResolved=N})은 완료({@code RESOLVED}) 스레드를 제외하고,
     * 라이프사이클 행이 없으면 {@code OPEN} 으로 본다.
     * </p>
     *
     * @param createdBy 현재 사용자 계정명
     * @param entryId 후보를 요청한 엔트리 ID
     * @param keyword 제목 검색어 (빈 문자열이면 전체)
     * @param prefixId 말머리 ID. {@code null}이면 필터하지 않는다.
     * @param includeResolved {@code Y}이면 완료 스레드 포함, 그 외는 제외
     * @param pageable 최대 후보 수
     * @return 우선순위가 적용된 후보 집계 목록
     */
    @Query(value = "SELECT jt.id AS id, " +
            "       jt.title AS title, " +
            "       p.id AS prefixId, p.name AS prefixName, p.color AS prefixColor, p.active_yn AS prefixActiveYn, " +
            "       COALESCE(( " +
            "           SELECT lc.lifecycle_key " +
            "           FROM lifecycle lc " +
            "           WHERE lc.ref_id = jt.id " +
            "             AND lc.ref_content_type = 'JOURNAL_THREAD' " +
            "             AND lc.deleted_at IS NULL " +
            "           LIMIT 1 " +
            "       ), 'OPEN') AS lifecycleKey, " +
            "       COUNT(jte.id) AS membershipCount, " +
            "       MAX(jte.created_at) AS lastMembershipAt, " +
            "       MAX(CASE WHEN jte.entry_id = :entryId THEN 1 ELSE 0 END) AS currentEntryMembershipCount " +
            "FROM journal_thread jt " +
            "LEFT JOIN prefix_content pc ON pc.ref_id = jt.id AND pc.ref_content_type = 'JOURNAL_THREAD' AND pc.deleted_at IS NULL " +
            "LEFT JOIN prefix p ON p.id = pc.prefix_id AND p.deleted_at IS NULL " +
            "LEFT JOIN journal_thread_entry jte " +
            "  ON jte.thread_id = jt.id " +
            " AND jte.created_by = :createdBy " +
            " AND jte.deleted_at IS NULL " +
            "WHERE jt.created_by = :createdBy " +
            "  AND jt.deleted_at IS NULL " +
            "  AND (:keyword = '' OR LOWER(COALESCE(jt.title, '')) LIKE CONCAT('%', LOWER(:keyword), '%')) " +
            "  AND (:prefixId IS NULL OR pc.prefix_id = :prefixId) " +
            "  AND (:includeResolved = 'Y' OR COALESCE(( " +
            "           SELECT lc.lifecycle_key " +
            "           FROM lifecycle lc " +
            "           WHERE lc.ref_id = jt.id " +
            "             AND lc.ref_content_type = 'JOURNAL_THREAD' " +
            "             AND lc.deleted_at IS NULL " +
            "           LIMIT 1 " +
            "       ), 'OPEN') <> 'RESOLVED') " +
            "GROUP BY jt.id, jt.title, jt.created_at, jt.updated_at, " +
            "         p.id, p.name, p.color, p.active_yn " +
            "ORDER BY MAX(CASE WHEN jte.entry_id = :entryId THEN 1 ELSE 0 END) DESC, " +
            "         CASE WHEN MAX(jte.created_at) IS NULL THEN 1 ELSE 0 END ASC, " +
            "         MAX(jte.created_at) DESC, " +
            "         COUNT(jte.id) DESC, " +
            "         COALESCE(jt.updated_at, jt.created_at) DESC, " +
            "         jt.id DESC",
            nativeQuery = true)
    List<JournalThreadCandidateProjection> findCandidates(
            final @Param("createdBy") String createdBy,
            final @Param("entryId") Integer entryId,
            final @Param("keyword") String keyword,
            final @Param("prefixId") Integer prefixId,
            final @Param("includeResolved") String includeResolved,
            final Pageable pageable
    );
}
