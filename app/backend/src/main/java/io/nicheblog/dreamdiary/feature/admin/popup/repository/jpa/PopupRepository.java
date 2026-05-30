package io.nicheblog.dreamdiary.feature.admin.popup.repository.jpa;

import io.nicheblog.dreamdiary.feature.admin.popup.entity.PopupEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * PopupRepository
 * <pre>
 *  팝업 관리 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface PopupRepository
        extends BaseStreamRepository<PopupEntity, Integer> {
    //
}

