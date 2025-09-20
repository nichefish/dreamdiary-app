package io.nicheblog.dreamdiary.domain.jrnl.entry.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntryEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * JrnlEntryRepository
 * <pre>
 *  저널 항목 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlEntryRepository")
public interface JrnlEntryRepository
        extends BaseStreamRepository<JrnlEntryEntity, Integer> {

    /**
     * 해당 일자에서 항목 마지막 인덱스 조회
     *
     * @param jrnlDayNo 조회할 일자 번호
     * @return {@link Optional} -- 해당 일자에서 항목의 마지막 인덱스
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT MAX(entry.idx) " +
            "FROM JrnlEntryEntity entry " +
            "INNER JOIN FETCH JrnlDayEntity day ON entry.jrnlDayNo = day.postNo " +
            "WHERE entry.jrnlDayNo = :jrnlDayNo")
    Optional<Integer> findLastIndexByJrnlDay(final @Param("jrnlDayNo") Integer jrnlDayNo);
}