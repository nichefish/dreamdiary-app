package io.nicheblog.dreamdiary.infrastructure.release.repository.jpa;

import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.infrastructure.release.entity.ReleaseHistoryEntity;
import io.nicheblog.dreamdiary.infrastructure.release.type.ReleaseEventType;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ReleaseHistoryRepository
 * <pre>
 *  release_info (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ReleaseHistoryRepository
        extends BaseStreamRepository<ReleaseHistoryEntity, Integer> {

    Optional<ReleaseHistoryEntity> findTopByEventTypeOrderByCreatedAtDesc(ReleaseEventType eventType);

    List<ReleaseHistoryEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
