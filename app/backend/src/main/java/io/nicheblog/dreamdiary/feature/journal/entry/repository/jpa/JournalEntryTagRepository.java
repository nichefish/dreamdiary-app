package io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentCntDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryTagEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryTagContentParam;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * 저널 엔트리 태그의 목록·집계 전용 JPA 저장소.
 * <p>
 * 카테고리맵 조회는 태그 엔티티 그래프 대신 필요한 컬럼만 {@link TagDto}로 projection하고,
 * 사용 건수 조회는 {@link TagContentCntDto} 집계 결과를 반환한다.
 * </p>
 */
@Repository
public interface JournalEntryTagRepository
        extends BaseStreamRepository<JournalEntryTagEntity, Integer> {

    /**
     * 사용자와 콘텐츠 타입 축에 속한 태그의 카테고리 맵 구성용 행만 조회한다.
     * 엔티티 그래프를 적재하지 않고 태그 ID·이름·카테고리만 projection 하며,
     * 엔트리·챕터·일자 존재 조건은 일반 태그 목록 조회와 동일하게 적용한다.
     *
     * @param username 사용자 아이디
     * @param contentTypes 조회할 콘텐츠 타입 키 목록
     * @return 카테고리 맵 구성용 태그 목록
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT DISTINCT new io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto(tag.id, tag.name, COALESCE(category.name, '')) " +
            "FROM JournalEntryTagEntity tag " +
            "INNER JOIN tag.journalEntryTagList tagContent " +
            "INNER JOIN tagContent.journalEntry entry " +
            "INNER JOIN entry.journalChapter chapter " +
            "INNER JOIN chapter.journalDay day " +
            "LEFT JOIN tag.tagCategory category " +
            "WHERE tagContent.createdBy = :username " +
            " AND tagContent.refContentType IN :contentTypes " +
            " AND entry.contentType IN :contentTypes")
    List<TagDto> findCategoryRowsByUserAndContentTypes(
            final @Param("username") String username,
            final @Param("contentTypes") List<String> contentTypes
    );

    /**
     * 기간/사용자/콘텐츠 타입 조건으로 태그별 사용 건수를 집계한다.
     *
     * @param param 집계 파라미터
     * @return 태그 건수 목록
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT new io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentCntDto(ct.tagId, COUNT(ct.id)) " +
            "FROM JournalEntryTagContentEntity ct " +
            "INNER JOIN JournalEntryEntity entry ON ct.refId = entry.id AND entry.contentType IN :#{#param.contentTypes} " +
            "INNER JOIN JournalChapterEntity chapter ON entry.journalChapterId = chapter.id " +
            "INNER JOIN JournalDayEntity day ON chapter.journalDayId = day.id " +
            "WHERE ct.createdBy = :#{#param.createdBy} " +
            " AND ct.refContentType IN :#{#param.contentTypes} " +
            " AND (:#{#param.yy} IS NULL OR day.yy = :#{#param.yy} OR :#{#param.yy} = 9999) " +
            " AND (:#{#param.mnth} IS NULL OR day.mnth = :#{#param.mnth} OR :#{#param.mnth} = 99) " +
            " AND (:#{#param.weekStartDt} IS NULL OR day.weekStartDt = :#{T(io.nicheblog.dreamdiary.global.util.date.DateUtils).asLocalDate(#param.weekStartDt)}) " +
            "GROUP BY ct.tagId")
    List<TagContentCntDto> countSizeMap(final @Param("param") JournalEntryTagContentParam param);
}
