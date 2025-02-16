package io.nicheblog.dreamdiary.domain.admin.lgnPolicy.service.impl;

import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.entity.LgnPolicyEntity;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.mapstruct.LgnPolicyMapstruct;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.model.LgnPolicyDto;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.repository.jpa.LgnPolicyRepository;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.service.LgnPolicyService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * LgnPolicyService
 * <pre>
 *  로그인 정책 정보 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("lgnPolicyService")
@RequiredArgsConstructor
public class LgnPolicyServiceImpl
        implements LgnPolicyService {

    private final LgnPolicyRepository repository;
    private final LgnPolicyMapstruct mapstruct = LgnPolicyMapstruct.INSTANCE;

    /**
     * 사용자 관리 > 로그인 설정 조회 (Dto 레벨)
     *
     * @return {@link LgnPolicyDto} -- 로그인 설정 정보
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lgnPolicy", unless = "#result == null")   // 항목이 한 개 고정일 경우 Cache의 key는 불필요하다.
    public LgnPolicyDto getDtlDto() throws Exception {
        // entity level
        final LgnPolicyEntity retrievedEntity = this.getDtlEntity();

        return mapstruct.toDto(retrievedEntity);
    }

    /**
     * 사용자 관리 > 로그인 설정 조회 (Entity 레벨)
     *
     * @return {@link LgnPolicyEntity} -- 로그인 설정 엔티티
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    @Cacheable(value = "lgnPolicyEntity", unless = "#result == null")   // 항목이 한 개 고정일 경우 Cache의 key는 불필요하다.
    public LgnPolicyEntity getDtlEntity() throws Exception {
        final Optional<LgnPolicyEntity> retrievedWrapper = repository.findById(1);
        if (retrievedWrapper.isEmpty()) {
            final List<LgnPolicyEntity> scrapAll = repository.findAll();
            if (CollectionUtils.isEmpty(scrapAll)) return null;
            return scrapAll.get(0);
        }

        return retrievedWrapper.orElse(null);
    }

    /**
     * 사용자 관리 > 로그인 설정 정보 등록/수정
     *
     * @param registDto 로그인 정책 정보 Dto
     * @return {@link Boolean} -- 성공 여부를 나타내는 Boolean 값
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    @CacheEvict(value = {"lgnPolicyEntity", "lgnPolicy"}, allEntries = true)
    public ServiceResponse regist(final LgnPolicyDto registDto) throws Exception {
        // Dto -> Entity 변환
        final LgnPolicyEntity retrievedEntity = mapstruct.toEntity(registDto);
        // insert/update
        final LgnPolicyEntity updated = repository.save(retrievedEntity);

        return ServiceResponse.builder()
                .rslt(updated.getLgnPolicyNo() != null)
                .rsltObj(mapstruct.toDto(updated))
                .build();
    }
}
