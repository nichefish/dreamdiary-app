package io.nicheblog.dreamdiary.domain.jrnl.diary.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryContentTagParam;
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
 * JrnlDiaryTagRepository
 * <pre>
 *  저널 일기 태그 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlDiaryTagRepository")
public interface JrnlDiaryTagRepository
        extends BaseStreamRepository<JrnlDiaryTagEntity, Integer> {

    /**
     * 년도/월별 저널 일기 태그 개수 조회
     *
     * @param param - 삭제할 대상의 파라미터 (게시글 번호, 컨텐츠 타입, 태그 이름, 카테고리 포함)
     * @return {@link Integer} -- 태그 번호와 년도, 월에 해당하는 태그 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(ct.contentTagNo) " +
            "FROM JrnlDiaryContentTagEntity ct " +
            "INNER JOIN FETCH JrnlDiaryEntity diary ON ct.refPostNo = diary.postNo " +
            "INNER JOIN FETCH JrnlDayEntity day ON diary.jrnlDayNo = day.postNo " +
            "WHERE ct.refTagNo = :#{#param.tagNo} " +
            " AND (:#{#param.yy} IS NULL OR day.yy = :#{#param.yy} OR :#{#param.yy} = 9999) " +
            " AND (:#{#param.mnth} IS NULL OR day.mnth = :#{#param.mnth} OR :#{#param.mnth} = 99)" +
            " AND (ct.regstrId = :#{#param.regstrId})")
    Integer countDiarySize(final @Param("param") JrnlDiaryContentTagParam param);

    /**
     * 년도/월별 저널 일기 태그 개수 맵 조회
     *
     * @param param - 삭제할 대상의 파라미터 (게시글 번호, 컨텐츠 타입, 태그 이름, 카테고리 포함)
     * @return Integer - 태그 번호와 컨텐츠 타입에 해당하는 태그 개수
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT new io.nicheblog.dreamdiary.extension.clsf.tag.model.ContentTagCntDto(ct.refTagNo, COUNT(ct.contentTagNo)) " +
            "FROM JrnlDiaryContentTagEntity ct " +
            "INNER JOIN FETCH JrnlDiaryEntity diary ON ct.refPostNo = diary.postNo " +
            "INNER JOIN FETCH JrnlDayEntity day ON diary.jrnlDayNo = day.postNo " +
            "WHERE ct.regstrId = :#{#param.regstrId} " +
            " AND (:#{#param.yy} IS NULL OR day.yy = :#{#param.yy} OR :#{#param.yy} = 9999) " +
            " AND (:#{#param.mnth} IS NULL OR day.mnth = :#{#param.mnth} OR :#{#param.mnth} = 99)" +
            "GROUP BY ct.refTagNo")
    List<ContentTagCntDto> countDiarySizeMap(final @Param("param") JrnlDiaryContentTagParam param);
}
