package io.nicheblog.dreamdiary.feature.journal.dream.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * JournalDreamRepository
 * <pre>
 *  저널 꿈 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("journalDreamRepository")
public interface JournalDreamRepository
        extends BaseStreamRepository<JournalDreamEntity, Integer> {

    /**
     * 해당 일자에서 꿈 마지막 인덱스 조회
     *
     * @param journalDayId 조회할 일자 번호
     * @return {@link Optional} -- 해당 일자에서 꿈의 마지막 인덱스
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT MAX(dream.idx) " +
            "FROM JournalDreamEntity dream " +
            "INNER JOIN FETCH JournalDayEntity day ON dream.journalDayId = day.id " +
            "WHERE dream.journalDayId = :journalDayId AND (dream.elseDreamYn IS NULL OR dream.elseDreamYn = 'N')")
    Optional<Integer> findLastIndexByJournalDay(final @Param("journalDayId") Integer journalDayId);
}

