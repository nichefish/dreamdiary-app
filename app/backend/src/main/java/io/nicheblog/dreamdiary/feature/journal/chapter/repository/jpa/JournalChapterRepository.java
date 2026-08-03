package io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * JournalChapterRepository
 * <pre>
 *  저널 챕터 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalChapterRepository
        extends BaseStreamRepository<JournalChapterEntity, Integer> {

    /** 현재 사용자가 소유한 챕터 단건 */
    Optional<JournalChapterEntity> findByIdAndCreatedBy(Integer id, String createdBy);

    /**
     * 해당 일자에서 항목 마지막 인덱스 조회
     *
     * @param journalDayId 조회할 일자 번호
     * @return {@link Optional} -- 해당 일자에서 항목의 마지막 인덱스
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT MAX(entry.sortOrder) " +
            "FROM JournalChapterEntity entry " +
            "INNER JOIN FETCH JournalDayEntity day ON entry.journalDayId = day.id " +
            "WHERE entry.journalDayId = :journalDayId")
    Optional<Integer> findLastIndexByJournalDay(final @Param("journalDayId") Integer journalDayId);

    /** 동일 일자의 꿈(DREAM) 챕터 1건 (없으면 empty) */
    Optional<JournalChapterEntity> findFirstByJournalDayIdAndChapterType(Integer journalDayId, ChapterType chapterType);

    /**
     * 동일 일자에 지정 타입이 아닌 챕터가 하나라도 존재하는지 여부.
     * 시스템 요약 역할 자동 부여 판정에서 DREAM 챕터를 제외하기 위해 사용한다.
     * (DREAM 은 항상 마지막에 배치되는 개념 챕터라 "첫 일반 챕터" 판정 대상이 아니다.)
     */
    boolean existsByJournalDayIdAndChapterTypeNot(Integer journalDayId, ChapterType chapterType);

    /** 동일 일자의 전체 챕터 (삭제 제외, @Where 적용) */
    List<JournalChapterEntity> findAllByJournalDayId(Integer journalDayId);
}
