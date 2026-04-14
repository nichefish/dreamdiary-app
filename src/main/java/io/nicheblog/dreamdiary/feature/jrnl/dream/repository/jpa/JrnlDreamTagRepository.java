package io.nicheblog.dreamdiary.feature.jrnl.dream.repository.jpa;

import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentCntDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.entity.JrnlDreamTagEntity;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamTagContentParam;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * JrnlDreamTagRepository
 * <pre>
 *  저널 꿈 태그 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlDreamTagRepository")
public interface JrnlDreamTagRepository
        extends BaseStreamRepository<JrnlDreamTagEntity, Integer> {

    /**
     * 년도/월별 저널 꿈 태그 개수 맵 조회
     *
     * @param param - 삭제할 대상의 파라미터 (게시글 번호, 컨텐츠 타입, 태그 이름, 카테고리 포함)
     * @return {@link Integer} -- 태그 ID와 년도, 월에 해당하는 태그 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT new io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentCntDto(ct.tagId, COUNT(ct.id)) " +
            "FROM JrnlDreamTagContentEntity ct " +
            "INNER JOIN FETCH JrnlDreamEntity diary ON ct.refId = diary.id " +
            "INNER JOIN FETCH JrnlDayEntity day ON diary.jrnlDayId = day.id " +
            "WHERE ct.regstrId = :#{#param.regstrId} " +
            " AND (:#{#param.yy} IS NULL OR day.yy = :#{#param.yy} OR :#{#param.yy} = 9999) " +
            " AND (:#{#param.mnth} IS NULL OR day.mnth = :#{#param.mnth} OR :#{#param.mnth} = 99) " +
            " AND (:#{#param.weekStartDt} IS NULL OR day.weekStartDt = :#{T(io.nicheblog.dreamdiary.global.util.date.DateUtils).asDate(#param.weekStartDt)}) " +
            " GROUP BY ct.tagId")
    List<TagContentCntDto> countDreamSizeMap(final @Param("param") JrnlDreamTagContentParam param);
}
