package io.nicheblog.dreamdiary.feature.journal.annual.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.annual.entity.JournalAnnualEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * JournalAnnualRepository
 * <pre>
 *  저널 결산 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalAnnualRepository
        extends BaseStreamRepository<JournalAnnualEntity, Integer> {

    /**
     * 년도별 저널 결산 정보 조회
     *
     * @param yy 결산 정보를 조회할 년도
     * @return {@link Optional} -- 해당 년도의 결산 정보를 담고 있는 Optional 객체
     */
    Optional<JournalAnnualEntity> findByYyAndCreatedBy(final Integer yy, final String createdBy);

    /**
     * 년도별 꿈기록 개수 조회
     *
     * @param yy 기록 정보를 조회할 년도
     * @param ownerId 저널 일자 소유 사용자 영속 ID
     * @return {@link Integer} -- 년도별 꿈기록 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(entry.id) " +
            "FROM JournalEntryEntity entry " +
            "INNER JOIN JournalChapterEntity chapter ON entry.journalChapterId = chapter.id " +
            "INNER JOIN JournalDayEntity day ON chapter.journalDayId = day.id " +
            "WHERE entry.contentType = 'JOURNAL_DREAM' " +
            "   AND day.yy = :yy " +
            "   AND day.ownerId = :ownerId")
    Integer getDreamCntByYy(final @Param("yy") Integer yy, final @Param("ownerId") Integer ownerId);

    /**
     * 전체 꿈기록 개수 조회
     *
     * @return {@link Integer} -- 전체 꿈기록 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COALESCE(SUM(annual.dreamCnt), 0) " +
            "FROM JournalAnnualEntity annual " +
            "WHERE annual.createdBy = :createdBy")
    Integer getTotalDreamCnt(final @Param("createdBy") String createdBy);
    
    /**
     * 년도별 저널 꿈기록 일자 개수 조회
     *
     * @param yy 기록 정보를 조회할 년도
     * @param ownerId 저널 일자 소유 사용자 영속 ID
     * @return {@link Integer} -- 년도별 꿈기록 일자 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(distinct day.id) " +
            "FROM JournalDayEntity day " +
            "INNER JOIN JournalChapterEntity chapter ON chapter.journalDayId = day.id AND chapter.chapterType = io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType.DREAM " +
            "INNER JOIN JournalEntryEntity entry ON entry.journalChapterId = chapter.id AND entry.contentType = 'JOURNAL_DREAM' " +
            "WHERE day.yy = :yy " +
            "   AND day.ownerId = :ownerId")
    Integer getDreamDayCntByYy(final @Param("yy") Integer yy, final @Param("ownerId") Integer ownerId);

    /**
     * 전체 꿈기록 일자 개수 조회
     *
     * @return {@link Integer} -- 전체 꿈기록 일자 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COALESCE(SUM(annual.dreamDayCnt), 0) " +
            "FROM JournalAnnualEntity annual " +
            "WHERE annual.createdBy = :createdBy")
    Integer getTotalDreamDayCnt(final @Param("createdBy") String createdBy);
}
