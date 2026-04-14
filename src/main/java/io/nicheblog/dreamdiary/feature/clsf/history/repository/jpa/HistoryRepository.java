package io.nicheblog.dreamdiary.feature.clsf.history.repository.jpa;

import io.nicheblog.dreamdiary.feature.clsf.history.entity.HistoryEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * HistoryRepository
 * <pre>
 *  이력 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("historyRepository")
public interface HistoryRepository
        extends BaseStreamRepository<HistoryEntity, Integer> {

    List<HistoryEntity> findAllByRefIdAndRefContentTypeOrderByRegDtDesc(Integer refId, String refContentType);

    Optional<HistoryEntity> findByIdAndRefIdAndRefContentType(Integer id, Integer refId, String refContentType);

    void deleteAllByRefIdAndRefContentType(Integer refId, String refContentType);
}
