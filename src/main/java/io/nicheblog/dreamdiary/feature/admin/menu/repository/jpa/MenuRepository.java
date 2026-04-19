package io.nicheblog.dreamdiary.feature.admin.menu.repository.jpa;

import io.nicheblog.dreamdiary.feature.admin.menu.entity.MenuEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * MenuRepository
 * <pre>
 *  메뉴 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface MenuRepository
        extends BaseStreamRepository<MenuEntity, Integer> {
    //
}

