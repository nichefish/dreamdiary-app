package io.nicheblog.dreamdiary.feature.journal.diary.repository.jpa;

import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentCntDto;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryTagEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryTagContentParam;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * JournalDiaryTagRepository
 * <pre>
 *  저널 일기 태그 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalDiaryTagRepository
        extends BaseStreamRepository<JournalDiaryTagEntity, Integer> {

    /**
     * 년도/월별 저널 일기 태그 개수 맵 조회
     *
     * @param param - 삭제할 대상의 파라미터 (게시글 번호, 컨텐츠 타입, 태그 이름, 카테고리 포함)
     * @return Integer - 태그 ID와 컨텐츠 타입에 해당하는 태그 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT new io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentCntDto(ct.tagId, COUNT(ct.id)) " +
            "FROM JournalDiaryTagContentEntity ct " +
            "INNER JOIN FETCH JournalDiaryEntity diary ON ct.refId = diary.id " +
            "INNER JOIN FETCH JournalDayEntity day ON diary.journalChapter.journalDayId = day.id " +
            "WHERE ct.createdBy = :#{#param.createdBy} " +
            " AND (:#{#param.yy} IS NULL OR day.yy = :#{#param.yy} OR :#{#param.yy} = 9999) " +
            " AND (:#{#param.mnth} IS NULL OR day.mnth = :#{#param.mnth} OR :#{#param.mnth} = 99) " +
            " AND (:#{#param.weekStartDt} IS NULL OR day.weekStartDt = :#{T(io.nicheblog.dreamdiary.global.util.date.DateUtils).asDate(#param.weekStartDt)}) " +
            "GROUP BY ct.tagId")
    List<TagContentCntDto> countDiarySizeMap(final @Param("param") JournalDiaryTagContentParam param);
}


