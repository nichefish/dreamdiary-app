package io.nicheblog.dreamdiary.auth.policy.repository.jpa;

import io.nicheblog.dreamdiary.auth.policy.entity.AuthPolicyEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * AuthPolicyRepository
 * <pre>
 *  인증 정책 정보 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface AuthPolicyRepository
        extends BaseStreamRepository<AuthPolicyEntity, Integer> {
    //
}

