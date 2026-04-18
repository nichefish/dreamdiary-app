package io.nicheblog.dreamdiary.infrastructure.code.repository.jpa;

import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CodeItemRepository
 * <pre>
 *  code_item repository 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface CodeItemRepository
        extends BaseStreamRepository<CodeItemEntity, Integer> {

    List<CodeItemEntity> findByGroupCode(final String groupCode);

    List<CodeItemEntity> findByGroupCodeOrderBySortOrderAscCodeAsc(final String groupCode);

    List<CodeItemEntity> findByGroupCodeAndUseYnOrderBySortOrderAsc(final String groupCode, final String useYn);

    List<CodeItemEntity> findAllByUseYnOrderByGroupCodeAscSortOrderAsc(final String useYn);

    CodeItemEntity findByGroupCodeAndCode(final String groupCode, final String code);
}
