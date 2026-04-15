package io.nicheblog.dreamdiary.feature.journal.intrpt.repository.jpa;

import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentCntDto;
import io.nicheblog.dreamdiary.feature.journal.intrpt.entity.JournalIntrptTagEntity;
import io.nicheblog.dreamdiary.feature.journal.intrpt.model.JournalIntrptTagContentParam;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * JournalIntrptTagRepository
 * <pre>
 *  저널 해석 태그 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalIntrptTagRepository
        extends BaseStreamRepository<JournalIntrptTagEntity, Integer> {

    /**
     * 년도/월별 저널 해석 태그 개수 맵 조회
     *
     * @param param - 삭제할 대상의 파라미터 (게시글 번호, 컨텐츠 타입, 태그 이름, 카테고리 포함)
     * @return Integer - 태그 ID와 컨텐츠 타입에 해당하는 태그 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT new io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentCntDto(ct.tagId, COUNT(ct.id)) " +
            "FROM JournalIntrptTagContentEntity ct " +
            "INNER JOIN FETCH JournalIntrptEntity intrpt ON ct.refId = intrpt.id " +
            "INNER JOIN FETCH JournalDayEntity day ON intrpt.journalDream.journalDayId = day.id " +
            "WHERE ct.createdBy = :#{#param.createdBy} " +
            " AND (:#{#param.yy} IS NULL OR day.yy = :#{#param.yy} OR :#{#param.yy} = 9999) " +
            " AND (:#{#param.mnth} IS NULL OR day.mnth = :#{#param.mnth} OR :#{#param.mnth} = 99)" +
            "GROUP BY ct.tagId")
    List<TagContentCntDto> countIntrptSizeMap(final @Param("param") JournalIntrptTagContentParam param);
}


