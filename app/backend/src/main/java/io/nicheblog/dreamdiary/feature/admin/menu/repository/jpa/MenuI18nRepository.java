package io.nicheblog.dreamdiary.feature.admin.menu.repository.jpa;

import io.nicheblog.dreamdiary.feature.admin.menu.entity.MenuI18nEntity;
import io.nicheblog.dreamdiary.feature.admin.menu.entity.MenuI18nId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MenuI18nRepository
 * <pre>
 *  메뉴 다국어 JPA Repository.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface MenuI18nRepository extends JpaRepository<MenuI18nEntity, MenuI18nId> {

    /** menuId 로 번역 목록 조회. */
    List<MenuI18nEntity> findByMenuId(final Integer menuId);

    /** menuId 목록으로 번역 일괄 조회 (사이드바 트리 지역화 시 N+1 방지용). */
    List<MenuI18nEntity> findByMenuIdIn(final List<Integer> menuIds);

    /** locale 로 번역 일괄 조회 (특정 언어 전체 트리 지역화용). */
    List<MenuI18nEntity> findByLocale(final String locale);

    /** menuId 에 속한 번역 전체 삭제. */
    @Transactional
    void deleteByMenuId(final Integer menuId);
}
