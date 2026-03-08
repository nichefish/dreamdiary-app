package io.nicheblog.dreamdiary.domain.jrnl.sumry.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.sumry.entity.JrnlSumryReviewEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * JrnlSumryReviewRepository
 * <pre>
 *  저널 결산 리뷰 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlSumryReviewRepository")
public interface JrnlSumryReviewRepository
        extends BaseStreamRepository<JrnlSumryReviewEntity, Integer> {
    //
}
