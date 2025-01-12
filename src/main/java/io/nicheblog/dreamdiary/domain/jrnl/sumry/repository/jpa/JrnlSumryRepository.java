package io.nicheblog.dreamdiary.domain.jrnl.sumry.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.sumry.entity.JrnlSumryEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * JrnlSumryRepository
 * <pre>
 *  저널 결산 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlSumryRepository")
public interface JrnlSumryRepository
        extends BaseStreamRepository<JrnlSumryEntity, Integer> {

    /**
     * 년도별 저널 결산 정보 조회
     *
     * @param yy 결산 정보를 조회할 년도
     * @return {@link Optional} -- 해당 년도의 결산 정보를 담고 있는 Optional 객체
     */
    Optional<JrnlSumryEntity> findByYyAndRegstrId(final Integer yy, final String regstrId);

    /**
     * 년도별 꿈기록 개수 조회
     *
     * @param yy 기록 정보를 조회할 년도
     * @return {@link Integer} -- 년도별 꿈기록 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(dream.postNo) " +
            "FROM JrnlDreamEntity dream " +
            "INNER JOIN FETCH JrnlDayEntity day ON dream.jrnlDayNo = day.postNo " +
            "WHERE day.yy = :yy " +
            "   AND day.regstrId = :regstrId")
    Integer getDreamCntByYy(final @Param("yy") Integer yy, final @Param("regstrId") String regstrId);

    /**
     * 전체 꿈기록 개수 조회
     *
     * @return {@link Integer} -- 전체 꿈기록 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COALESCE(SUM(sumry.dreamCnt), 0) " +
            "FROM JrnlSumryEntity sumry " +
            "WHERE sumry.regstrId = :regstrId")
    Integer getTotalDreamCnt(final @Param("regstrId") String regstrId);
    
    /**
     * 년도별 저널 꿈기록 일자 개수 조회
     *
     * @param yy 기록 정보를 조회할 년도
     * @return {@link Integer} -- 년도별 꿈기록 일자 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(distinct day.postNo) " +
            "FROM JrnlDayEntity day " +
            "INNER JOIN FETCH JrnlDreamEntity dream ON day.postNo = dream.jrnlDayNo " +
            "WHERE day.yy = :yy " +
            "   AND day.regstrId = :regstrId")
    Integer getDreamDayCntByYy(final @Param("yy") Integer yy, final @Param("regstrId") String regstrId);

    /**
     * 전체 꿈기록 일자 개수 조회
     *
     * @return {@link Integer} -- 전체 꿈기록 일자 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COALESCE(SUM(sumry.dreamDayCnt), 0) " +
            "FROM JrnlSumryEntity sumry " +
            "WHERE sumry.regstrId = :regstrId")
    Integer getTotalDreamDayCnt(final @Param("regstrId") String regstrId);

    /**
     * 년도별 일기기록 일자 개수 조회
     *
     * @param yy 기록 정보를 조회할 년도
     * @return {@link Integer} -- 년도별 일기기록 일자 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(distinct day.postNo) " +
            "FROM JrnlDayEntity day " +
            "INNER JOIN FETCH JrnlDiaryEntity diary ON day.postNo = diary.jrnlDayNo " +
            "WHERE day.yy = :yy" +
            "   AND day.regstrId = :regstrId")
    Integer getDiaryDayCntByYy(final @Param("yy") Integer yy, final @Param("regstrId") String regstrId);


    /**
     * 전체 일기기록 일자 개수 조회
     * 
     * @return {@link Integer} -- 전체 일기기록 일자 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COALESCE(SUM(sumry.postNo), 0) " +
            "FROM JrnlSumryEntity sumry " +
            "WHERE sumry.regstrId = :regstrId")
    Integer getTotalDiaryDayCnt(final @Param("regstrId") String regstrId);
}