package io.nicheblog.dreamdiary.infrastructure.code.repository.jpa;

import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemI18nEntity;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemI18nId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CodeItemI18nRepository
 * <pre>
 *  상세 코드 다국어 JPA Repository.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface CodeItemI18nRepository extends JpaRepository<CodeItemI18nEntity, CodeItemI18nId> {

    /** codeItemId 로 번역 목록 조회. */
    List<CodeItemI18nEntity> findByCodeItemId(final Integer codeItemId);

    /** codeItemId 목록으로 번역 일괄 조회 (캐시 preload 용). */
    List<CodeItemI18nEntity> findByCodeItemIdIn(final List<Integer> codeItemIds);

    /** codeItemId + locale 로 단건 조회. */
    Optional<CodeItemI18nEntity> findByCodeItemIdAndLocale(final Integer codeItemId, final String locale);

    /** codeItemId 에 속한 번역 전체 삭제. */
    @Transactional
    void deleteByCodeItemId(final Integer codeItemId);
}
