package io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * JournalChapterRepository
 * <pre>
 *  저널 챕터 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("journalChapterRepository")
public interface JournalChapterRepository
        extends BaseStreamRepository<JournalChapterEntity, Integer> {

    /**
     * 해당 일자에서 항목 마지막 인덱스 조회
     *
     * @param journalDayId 조회할 일자 번호
     * @return {@link Optional} -- 해당 일자에서 항목의 마지막 인덱스
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT MAX(entry.idx) " +
            "FROM JournalChapterEntity entry " +
            "INNER JOIN FETCH JournalDayEntity day ON entry.journalDayId = day.id " +
            "WHERE entry.journalDayId = :journalDayId")
    Optional<Integer> findLastIndexByJournalDay(final @Param("journalDayId") Integer journalDayId);
}

