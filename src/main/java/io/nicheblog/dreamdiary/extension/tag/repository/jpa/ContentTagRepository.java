package io.nicheblog.dreamdiary.extension.tag.repository.jpa;

import io.nicheblog.dreamdiary.extension.tag.entity.ContentTagEntity;
import io.nicheblog.dreamdiary.extension.tag.model.ContentTagParam;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * ContentTagRepository
 * <pre>
 *  컨텐츠 태그 정보 repository 인터페이스.
 *  (cascade하지 않고 수동 관리)
 * </pre>
 *
 * @author nichefish
 */
@Repository("contentTagRepository")
public interface ContentTagRepository
        extends BaseStreamRepository<ContentTagEntity, Integer> {

    /**
     * 특정 게시물에 대해 태그 정보와 연결되지 않는 컨텐츠 태그 삭제.
     *
     * @param param - 삭제할 대상의 파라미터 (게시글 번호, 컨텐츠 타입, 태그 이름, 카테고리 포함)
     */
    @Modifying
    @Query("DELETE FROM ContentTagEntity ct " +
            "WHERE ct.refPostNo = :#{#param.refPostNo} " +
            "  AND ct.refContentType = :#{#param.refContentType} " +
            "  AND ct.regstrId = :#{#param.regstrId} " +
            "  AND EXISTS (SELECT 1 FROM TagEntity t WHERE t.tagNo = ct.refTagNo AND t.tagNm = :#{#param.tagNm} AND (t.ctgr = :#{#param.ctgr} OR (t.ctgr IS NULL AND :#{#param.ctgr} IS NULL)))")
    void deleteObsoleteContentTags(final @Param("param") ContentTagParam param);
}
