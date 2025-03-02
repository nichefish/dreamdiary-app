package io.nicheblog.dreamdiary.domain.jrnl.day.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayContentTagParam;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.ContentTagCntDto;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * JrnlDayTagRepository
 * <pre>
 *  저널 일자 태그 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */

@Repository("jrnlDayTagRepository")
public interface JrnlDayTagRepository
        extends BaseStreamRepository<JrnlDayTagEntity, Integer> {

    /**
     * 년도/월별 저널 일자 태그 개수 맵 조회
     *
     * @param param - 삭제할 대상의 파라미터 (게시글 번호, 컨텐츠 타입, 태그 이름, 카테고리 포함)
     * @return Integer - 태그 번호와 컨텐츠 타입에 해당하는 태그 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT new io.nicheblog.dreamdiary.extension.clsf.tag.model.ContentTagCntDto(ct.refTagNo, COUNT(ct.contentTagNo)) " +
            "FROM JrnlDayContentTagEntity ct " +
            "INNER JOIN FETCH JrnlDayEntity day ON ct.refPostNo = day.postNo " +
            "WHERE ct.regstrId = :#{#param.regstrId} " +
            " AND (:#{#param.yy} IS NULL OR day.yy = :#{#param.yy} OR :#{#param.yy} = 9999) " +
            " AND (:#{#param.mnth} IS NULL OR day.mnth = :#{#param.mnth} OR :#{#param.mnth} = 99)" +
            "GROUP BY ct.refTagNo")
    List<ContentTagCntDto> countDaySizeMap(final @Param("param") JrnlDayContentTagParam param);
}
