package io.nicheblog.dreamdiary.extension.clsf.meta.repository.jpa;

import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaContentEntity;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaContentParam;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * MetaContentRepository
 * <pre>
 *  메타-컨텐츠 정보 repository 인터페이스.
 *  (cascade하지 않고 수동 관리)
 * </pre>
 *
 * @author nichefish
 */
@Repository("metaContentRepository")
public interface MetaContentRepository
        extends BaseStreamRepository<MetaContentEntity, Integer> {

    /**
     * 특정 게시물에 대해 메타 정보와 연결되지 않는 메타-컨텐츠 삭제.
     *
     * @param param - 삭제할 대상의 파라미터 (게시글 번호, 컨텐츠 타입, 메타 이름, 카테고리 포함)
     */
    @Modifying
    @Query("DELETE FROM MetaContentEntity ct " +
            "WHERE ct.refPostNo = :#{#param.refPostNo} " +
            "  AND ct.refContentType = :#{#param.refContentType} " +
            "  AND ct.regstrId = :#{#param.regstrId} " +
            "  AND EXISTS (SELECT 1 FROM MetaEntity t WHERE t.metaNo = ct.refMetaNo AND t.metaNm = :#{#param.metaNm} AND (t.ctgr = :#{#param.ctgr} OR (t.ctgr IS NULL AND :#{#param.ctgr} IS NULL)))")
    void deleteObsoleteMetaContents(final @Param("param") MetaContentParam param);
}
