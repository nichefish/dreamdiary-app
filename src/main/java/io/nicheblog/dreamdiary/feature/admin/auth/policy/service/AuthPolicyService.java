package io.nicheblog.dreamdiary.feature.admin.auth.policy.service;

import io.nicheblog.dreamdiary.auth.policy.entity.AuthPolicyEntity;
import io.nicheblog.dreamdiary.auth.policy.repository.jpa.AuthPolicyRepository;
import io.nicheblog.dreamdiary.feature.admin.auth.policy.mapstruct.AuthPolicyMapstruct;
import io.nicheblog.dreamdiary.feature.admin.auth.policy.model.AuthPolicyDto;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * AuthPolicyService
 * <pre>
 *  인증 정책 정보 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("authPolicyService")
@RequiredArgsConstructor
public class AuthPolicyService {

    private final AuthPolicyRepository repository;
    private final AuthPolicyMapstruct mapstruct = AuthPolicyMapstruct.INSTANCE;

    /**
     * 인증 정책 조회 (Dto 레벨)
     *
     * @return {@link AuthPolicyDto} -- 로그인 설정 정보
     */
    @Transactional(readOnly = true)
    public AuthPolicyDto getDtlDto() throws Exception {
        // entity level
        final AuthPolicyEntity retrievedEntity = this.getDtlEntity();

        return mapstruct.toDto(retrievedEntity);
    }

    /**
     * 인증 정책 조회 (Entity 레벨)
     *
     * @return {@link AuthPolicyEntity} -- 로그인 설정 엔티티
     */
    @Transactional
    public AuthPolicyEntity getDtlEntity() throws Exception {
        final Optional<AuthPolicyEntity> retrievedWrapper = repository.findById(1);
        if (retrievedWrapper.isEmpty()) {
            final List<AuthPolicyEntity> scrapAll = repository.findAll();
            if (CollectionUtils.isEmpty(scrapAll)) return null;
            return scrapAll.get(0);
        }

        return retrievedWrapper.orElse(null);
    }

    /**
     * 인증 정책 등록/수정
     *
     * @param registDto 인증 정책 정보 Dto
     * @return {@link Boolean} -- 성공 여부를 나타내는 Boolean 값
     */
    @Transactional
    @CacheEvict(value = {"authPolicyEntity", "authPolicy"}, allEntries = true)
    public ServiceResponse regist(final AuthPolicyDto registDto) throws Exception {
        // Dto -> Entity 변환
        final AuthPolicyEntity retrievedEntity = mapstruct.toEntity(registDto);
        // insert/update
        final AuthPolicyEntity updated = repository.save(retrievedEntity);

        return ServiceResponse.builder()
                .rslt(updated.getAuthPolicyNo() != null)
                .rsltObj(mapstruct.toDto(updated))
                .build();
    }
}
