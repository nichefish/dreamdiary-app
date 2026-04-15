package io.nicheblog.dreamdiary.feature.journal.sumry.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.sumry.entity.JournalSumryReviewEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * JournalSumryReviewRepository
 * <pre>
 *  저널 결산 리뷰 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("journalSumryReviewRepository")
public interface JournalSumryReviewRepository
        extends BaseStreamRepository<JournalSumryReviewEntity, Integer> {
    //
}

