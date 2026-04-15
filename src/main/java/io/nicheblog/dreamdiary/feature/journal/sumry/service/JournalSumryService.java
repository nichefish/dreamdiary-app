package io.nicheblog.dreamdiary.feature.journal.sumry.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.sumry.entity.JournalSumryEntity;
import io.nicheblog.dreamdiary.feature.journal.sumry.mapstruct.JournalSumryMapstruct;
import io.nicheblog.dreamdiary.feature.journal.sumry.model.JournalSumryDto;
import io.nicheblog.dreamdiary.feature.journal.sumry.repository.jpa.JournalSumryRepository;
import io.nicheblog.dreamdiary.feature.journal.sumry.spec.JournalSumrySpec;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JournalSumryService
 * <pre>
 *  저널 결산 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalSumryService
        implements BaseClsfService<JournalSumryDto, JournalSumryDto, Integer, JournalSumryEntity> {

    @Getter
    private final JournalSumryRepository repository;
    @Getter
    private final JournalSumrySpec spec;
    @Getter
    private final JournalSumryMapstruct mapstruct;

    public JournalSumryMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalSumryMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JournalCacheEvictWorker journalCacheEvictWorker;

    private final ApplicationContext context;
    private JournalSumryService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 저널 결산 정뵤 목록 조회 :: 캐시 사용 위해 구현체로 pullUp
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link List< JournalSumryDto >} -- 검색 조건에 맞는 결산 목록 Dto 리스트
     */
    @Cacheable(value="journalSumryListByUser", key="#username")
    public List<JournalSumryDto> getListDtoByUser(final String username, final BaseSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value="journalSumryTotalListByUser", key="#username"),
            @CacheEvict(value="journalSumryListByUser", key="#username"),
            @CacheEvict(value="journalSumryYyDtlDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #yy)")
    })
    public Boolean makeYySumryByUser(final String username, final Integer yy) throws Exception {
        // 해당 년도 저널 결산 정보 조회
        final JournalSumryEntity sumry = repository.findByYyAndCreatedBy(yy, AuthUtils.requireUsername(username)).orElse(new JournalSumryEntity(yy));

        // 해당 년도 꿈 일자 조회해서 갱신
        final Integer dreamDayCntByYy = repository.getDreamDayCntByYy(yy, AuthUtils.requireUsername(username));
        sumry.setDreamDayCnt(dreamDayCntByYy);
        // 해당 년도 꿈 조회해서 갱신
        final Integer dreamCntByYy = repository.getDreamCntByYy(yy, AuthUtils.requireUsername(username));
        sumry.setDreamCnt(dreamCntByYy);

        repository.save(sumry);

        return true;
    }

    /**
     * 2011년부터 현재 년도까지의 저널 결산 정보 생성
     *
     * @return {@link Boolean} -- 결산 생성 성공 여부 (항상 true 반환)
     */
    @Transactional
    @CacheEvict(value={"journalSumryTotalListByUser", "journalSumryListByUser"}, key="#username")
    public Boolean makeTotalYySumryByUser(final String username) throws Exception {
        final int currYy = DateUtils.getCurrYy();
        final int startYy = 2011;
        for (int yy = startYy; yy <= currYy; yy++) {
            try {
                this.makeYySumryByUser(AuthUtils.requireUsername(username), yy);
            } catch (final Exception e) {
                log.warn("Error creating annual summary for {}", yy);
            }
        }

        return true;
    }

    /**
     * 관련 정보를 취합하여 총 저널 결산 정보를 생성합니다. (캐시 처리)
     *
     * @return {@link JournalSumryDto} -- 총 결산 정보가 담긴 Dto 객체
     */
    @Cacheable(value="journalSumryTotalListByUser", key="#username")
    public JournalSumryDto getTotalSumryByUser(final String username) {
        final JournalSumryDto totalSumry = new JournalSumryDto();
        // 해당 년도 꿈 일자 조회해서 갱신
        final Integer dreamDayCntByYy = repository.getTotalDreamDayCnt(AuthUtils.requireUsername(username));
        totalSumry.setDreamDayCnt(dreamDayCntByYy);
        // 해당 년도 꿈 조회해서 갱신
        final Integer dreamCntByYy = repository.getTotalDreamCnt(AuthUtils.requireUsername(username));
        totalSumry.setDreamCnt(dreamCntByYy);

        return totalSumry;
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JournalSumryDto postDto, final JournalSumryDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_SUMRY);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 수정할 객체 (dto)
     * @param modifyEntity 수정할 객체 (entity)
     */
    @Override
    public void preModify(final JournalSumryDto modifyDto, final JournalSumryEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 저널 결산 상세 정보 조회 (캐시 처리)
     *
     * @param key 식별자
     * @return {@link JournalSumryDto} -- 조회된 결산 정보가 담긴 Dto 객체
     */
    @Cacheable(value="journalSumryDtlDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #key)")
    public JournalSumryDto getSumryDtlByUser(final String username, final Integer key) throws Exception {
        final JournalSumryDto retrieved = this.getSelf().getDtlDto(key);
        if (retrieved != null && !retrieved.getIsCreatedBy(username)) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return retrieved;
    }

    /**
     * 년도별 저널 결산 정보 조회 (캐시 처리)
     *
     * @param yy 조회할 년도
     * @return {@link JournalSumryDto} -- 조회된 결산 정보가 담긴 Dto 객체, 없을 경우 null 반환
     */
    @Cacheable(value="journalSumryYyDtlDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #yy)")
    public JournalSumryDto getDtlDtoByYyByUser(final String username, final Integer yy) throws Exception {
        final Optional<JournalSumryEntity> retrievedWrapper = repository.findByYyAndCreatedBy(yy, AuthUtils.requireUsername(username));
        if (retrievedWrapper.isEmpty()) return null;

        return mapstruct.toDto(retrievedWrapper.get());
    }

    /**
     * 저널 결산 꿈 기록 완료 처리
     *
     * @param key 식별자
     * @return {@link boolean} -- 처리 성공 여부
     */
    @Transactional
    public boolean dreamCompt(final Integer key) throws Exception {
        final JournalSumryEntity retrievedEntity = this.getDtlEntity(key);
        if (!AuthUtils.isCreatedBy(retrievedEntity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        retrievedEntity.setDreamComptYn("Y");
        repository.save(retrievedEntity);

        // 관련 캐시 제거
        journalCacheEvictWorker.evictAfterCommit(
                JournalCacheEvictParam.builder()
                        .id(key)
                        .yy(retrievedEntity.getYy())
                        .build(),
                ContentType.JOURNAL_SUMRY
        );

        return true;
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JournalSumryDto deletedDto) throws Exception {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }
}


