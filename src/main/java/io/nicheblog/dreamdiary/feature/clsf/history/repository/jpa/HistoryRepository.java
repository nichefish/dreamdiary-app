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

    List<HistoryEntity> findAllByRefPostNoAndRefContentTypeOrderByRegDtDesc(Integer refPostNo, String refContentType);

    Optional<HistoryEntity> findByHistoryNoAndRefPostNoAndRefContentType(Integer historyNo, Integer refPostNo, String refContentType);
}
