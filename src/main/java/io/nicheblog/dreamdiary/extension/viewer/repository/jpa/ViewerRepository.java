package io.nicheblog.dreamdiary.extension.viewer.repository.jpa;

import io.nicheblog.dreamdiary.extension.viewer.entity.ViewerEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * ViewerRepository
 * <pre>
 *  컨텐츠 열람자 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("viewerRepository")
public interface ViewerRepository
        extends BaseStreamRepository<ViewerEntity, Integer> {
    //
}
