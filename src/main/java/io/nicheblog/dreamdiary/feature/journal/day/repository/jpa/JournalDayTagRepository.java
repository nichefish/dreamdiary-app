package io.nicheblog.dreamdiary.feature.journal.day.repository.jpa;

import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentCntDto;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayTagEntity;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayTagContentParam;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * JournalDayTagRepository
 * <pre>
 *  저널 일자 태그 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */

@Repository
public interface JournalDayTagRepository
        extends BaseStreamRepository<JournalDayTagEntity, Integer> {

    /**
     * 년도/월별 저널 일자 태그 개수 맵 조회
     *
     * @param param - 삭제할 대상의 파라미터 (게시글 번호, 컨텐츠 타입, 태그 이름, 카테고리 포함)
     * @return Integer - 태그 ID와 컨텐츠 타입에 해당하는 태그 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT new io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentCntDto(ct.tagId, COUNT(ct.id)) " +
            "FROM JournalDayTagContentEntity ct " +
            "INNER JOIN FETCH JournalDayEntity day ON ct.refId = day.id " +
            "WHERE ct.createdBy = :#{#param.createdBy} " +
            " AND (:#{#param.yy} IS NULL OR day.yy = :#{#param.yy} OR :#{#param.yy} = 9999) " +
            " AND (:#{#param.mnth} IS NULL OR day.mnth = :#{#param.mnth} OR :#{#param.mnth} = 99) " +
            " AND (:#{#param.weekStartDt} IS NULL OR day.weekStartDt = :#{T(io.nicheblog.dreamdiary.global.util.date.DateUtils).asDate(#param.weekStartDt)}) " +
            "GROUP BY ct.tagId")
    List<TagContentCntDto> countDaySizeMap(final @Param("param") JournalDayTagContentParam param);

    /**
     * 태그가 기록된 연도 목록을 최신순으로 조회합니다.
     *
     * @param tagId 태그 ID
     * @param createdBy 사용자 ID
     * @return 연도 목록
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT DISTINCT day.yy " +
            "FROM JournalDayTagContentEntity ct " +
            "INNER JOIN FETCH JournalDayEntity day ON ct.refId = day.id " +
            "WHERE ct.tagId = :tagId " +
            "  AND ct.createdBy = :createdBy " +
            "ORDER BY day.yy DESC")
    List<Integer> findDistinctYysByTagIdAndCreatedBy(final @Param("tagId") Integer tagId, final @Param("createdBy") String createdBy);
}


