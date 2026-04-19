package io.nicheblog.dreamdiary.feature.journal.annual.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.annual.entity.JournalAnnualReviewEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * JournalAnnualReviewRepository
 * <pre>
 *  저널 결산 리뷰 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalAnnualReviewRepository
        extends BaseStreamRepository<JournalAnnualReviewEntity, Integer> {
    //
}


