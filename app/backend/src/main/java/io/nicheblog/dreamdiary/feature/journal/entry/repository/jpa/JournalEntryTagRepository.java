package io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentCntDto;
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

@Repository
public interface JournalEntryTagRepository
        extends BaseStreamRepository<JournalEntryTagEntity, Integer> {

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
            "INNER JOIN JournalEntryEntity entry ON ct.refId = entry.id AND entry.contentType = :#{#param.contentType} " +
            "INNER JOIN JournalChapterEntity chapter ON entry.journalChapterId = chapter.id " +
            "INNER JOIN JournalDayEntity day ON chapter.journalDayId = day.id " +
            "WHERE ct.createdBy = :#{#param.createdBy} " +
            " AND ct.refContentType = :#{#param.contentType} " +
            " AND (:#{#param.yy} IS NULL OR day.yy = :#{#param.yy} OR :#{#param.yy} = 9999) " +
            " AND (:#{#param.mnth} IS NULL OR day.mnth = :#{#param.mnth} OR :#{#param.mnth} = 99) " +
            " AND (:#{#param.weekStartDt} IS NULL OR day.weekStartDt = :#{T(io.nicheblog.dreamdiary.global.util.date.DateUtils).asLocalDate(#param.weekStartDt)}) " +
            "GROUP BY ct.tagId")
    List<TagContentCntDto> countSizeMap(final @Param("param") JournalEntryTagContentParam param);
}
