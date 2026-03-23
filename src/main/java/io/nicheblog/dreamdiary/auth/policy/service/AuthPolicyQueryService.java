package io.nicheblog.dreamdiary.auth.policy.service;

import io.nicheblog.dreamdiary.auth.policy.entity.AuthPolicyEntity;
import io.nicheblog.dreamdiary.auth.policy.mapstruct.AuthPolicyQueryMapstruct;
import io.nicheblog.dreamdiary.auth.policy.model.AuthPolicyQueryDto;
import io.nicheblog.dreamdiary.auth.policy.repository.jpa.AuthPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * AuthPolicyQueryService
 * <pre>
 *  인증 정책 조회 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service("authPolicyQueryService")
@RequiredArgsConstructor
public class AuthPolicyQueryService {

    private final AuthPolicyRepository repository;
    private final AuthPolicyQueryMapstruct mapstruct = AuthPolicyQueryMapstruct.INSTANCE;

    /**
     * 인증 정책 조회 (Dto 레벨)
     *
     * @return {@link AuthPolicyQueryDto} -- 로그인 설정 정보
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "authPolicy", unless = "#result == null")
    public AuthPolicyQueryDto getDtlDto() throws Exception {
        final AuthPolicyEntity retrievedEntity = this.getDtlEntity();
        return mapstruct.toDto(retrievedEntity);
    }

    /**
     * 인증 정책 조회 (Entity 레벨)
     *
     * @return {@link AuthPolicyEntity} -- 로그인 설정 엔티티
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "authPolicyEntity", unless = "#result == null")
    public AuthPolicyEntity getDtlEntity() throws Exception {
        final Optional<AuthPolicyEntity> retrievedWrapper = repository.findById(1);
        if (retrievedWrapper.isEmpty()) {
            final List<AuthPolicyEntity> scrapAll = repository.findAll();
            if (CollectionUtils.isEmpty(scrapAll)) return null;
            return scrapAll.get(0);
        }

        return retrievedWrapper.orElse(null);
    }
}
