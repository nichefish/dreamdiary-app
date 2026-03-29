package io.nicheblog.dreamdiary.feature.jrnl.sumry.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.entity.JrnlSumryEntity;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.mapstruct.JrnlSumryMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.model.JrnlSumryDto;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.repository.jpa.JrnlSumryRepository;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.spec.JrnlSumrySpec;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
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
 * JrnlSumryService
 * <pre>
 *  저널 결산 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlSumryService")
@RequiredArgsConstructor
@Log4j2
public class JrnlSumryService
        implements BaseClsfService<JrnlSumryDto, JrnlSumryDto, Integer, JrnlSumryEntity> {

    @Getter
    private final JrnlSumryRepository repository;
    @Getter
    private final JrnlSumrySpec spec;
    @Getter
    private final JrnlSumryMapstruct mapstruct;

    public JrnlSumryMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JrnlSumryMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JrnlCacheEvictWorker jrnlCacheEvictWorker;

    private final ApplicationContext context;
    private JrnlSumryService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 저널 결산 정뵤 목록 조회 :: 캐시 사용 위해 구현체로 pullUp
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link List<JrnlSumryDto>} -- 검색 조건에 맞는 결산 목록 Dto 리스트
     */
    public List<JrnlSumryDto> getMyListDto(final BaseSearchParam searchParam) throws Exception {
        final String userId = AuthUtils.getLgnUserId();
        return this.getSelf().getListDtoByUser(userId, searchParam);
    }

    /**
     * 저널 결산 정뵤 목록 조회 :: 캐시 사용 위해 구현체로 pullUp
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link List<JrnlSumryDto>} -- 검색 조건에 맞는 결산 목록 Dto 리스트
     */
    @Cacheable(value="jrnlSumryListByUser", key="#userId")
    public List<JrnlSumryDto> getListDtoByUser(final String userId, final BaseSearchParam searchParam) throws Exception {
        searchParam.setRegstrId(userId);
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 년도를 받아서 해당 년도 저널 결산 정보 생성
     *
     * @return {@link Boolean} -- 결산 생성 성공 여부 (항상 true 반환)
     */
    public Boolean makeMyYySumry(final Integer yy) throws Exception {
        final String userId = AuthUtils.getLgnUserId();
        return this.getSelf().makeYySumryByUser(userId, yy);
    }

    public Boolean makeMyTotalYySumry() throws Exception {
        final String userId = AuthUtils.getLgnUserId();
        final Boolean result = this.getSelf().makeTotalYySumryByUser(userId);
        EhCacheUtils.clearMyCache("jrnlSumryDtlDtoByUser");
        EhCacheUtils.clearMyCache("jrnlSumryYyDtlDtoByUser");
        return result;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value="jrnlSumryTotalListByUser", key="#userId"),
            @CacheEvict(value="jrnlSumryListByUser", key="#userId"),
            @CacheEvict(value="jrnlSumryYyDtlDtoByUser", key="#userId + \"_\" + #yy")
    })
    public Boolean makeYySumryByUser(final String userId, final Integer yy) throws Exception {
        // 해당 년도 저널 결산 정보 조회
        final JrnlSumryEntity sumry = repository.findByYyAndRegstrId(yy, userId).orElse(new JrnlSumryEntity(yy));

        // 해당 년도 꿈 일자 조회해서 갱신
        final Integer dreamDayCntByYy = repository.getDreamDayCntByYy(yy, userId);
        sumry.setDreamDayCnt(dreamDayCntByYy);
        // 해당 년도 꿈 조회해서 갱신
        final Integer dreamCntByYy = repository.getDreamCntByYy(yy, userId);
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
    @CacheEvict(value={"jrnlSumryTotalListByUser", "jrnlSumryListByUser"}, key="#userId")
    public Boolean makeTotalYySumryByUser(final String userId) throws Exception {
        final int currYy = DateUtils.getCurrYy();
        final int startYy = 2011;
        for (int yy = startYy; yy <= currYy; yy++) {
            try {
                this.makeYySumryByUser(userId, yy);
            } catch (final Exception e) {
                log.warn("Error creating annual summary for {}", yy);
            }
        }

        return true;
    }

    /**
     * 관련 정보를 취합하여 총 저널 결산 정보를 생성합니다. (캐시 처리)
     *
     * @return {@link JrnlSumryDto} -- 총 결산 정보가 담긴 Dto 객체
     */
    public JrnlSumryDto getMyTotalSumry() {
        final String userId = AuthUtils.getLgnUserId();
        return this.getSelf().getTotalSumryByUser(userId);
    }

    /**
     * 관련 정보를 취합하여 총 저널 결산 정보를 생성합니다. (캐시 처리)
     *
     * @return {@link JrnlSumryDto} -- 총 결산 정보가 담긴 Dto 객체
     */
    @Cacheable(value="jrnlSumryTotalListByUser", key="#userId")
    public JrnlSumryDto getTotalSumryByUser(final String userId) {
        final JrnlSumryDto totalSumry = new JrnlSumryDto();
        // 해당 년도 꿈 일자 조회해서 갱신
        final Integer dreamDayCntByYy = repository.getTotalDreamDayCnt(userId);
        totalSumry.setDreamDayCnt(dreamDayCntByYy);
        // 해당 년도 꿈 조회해서 갱신
        final Integer dreamCntByYy = repository.getTotalDreamCnt(userId);
        totalSumry.setDreamCnt(dreamCntByYy);

        return totalSumry;
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlSumryDto postDto, final JrnlSumryDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_SUMRY);
    }

    /**
     * 저널 결산 상세 정보 조회 (캐시 처리)
     *
     * @param key 식별자
     * @return {@link JrnlSumryDto} -- 조회된 결산 정보가 담긴 Dto 객체
     */
    public JrnlSumryDto getMySumryDtl(final Integer key) throws Exception {
        final String userId = AuthUtils.getLgnUserId();
        return this.getSelf().getSumryDtlByUser(userId, key);
    }

    @Cacheable(value="jrnlSumryDtlDtoByUser", key="#userId + \"_\" + #key")
    public JrnlSumryDto getSumryDtlByUser(final String userId, final Integer key) throws Exception {
        return this.getSelf().getDtlDto(key);
    }

    /**
     * 년도별 저널 결산 정보 조회 (캐시 처리)
     *
     * @param yy 조회할 년도
     * @return {@link JrnlSumryDto} -- 조회된 결산 정보가 담긴 Dto 객체, 없을 경우 null 반환
     */
    public JrnlSumryDto getMyDtlDtoByYy(final Integer yy) throws Exception {
        final String userId = AuthUtils.getLgnUserId();
        return this.getSelf().getDtlDtoByYyByUser(userId, yy);
    }

    @Cacheable(value="jrnlSumryYyDtlDtoByUser", key="#userId + \"_\" + #yy")
    public JrnlSumryDto getDtlDtoByYyByUser(final String userId, final Integer yy) throws Exception {
        final Optional<JrnlSumryEntity> retrievedWrapper = repository.findByYyAndRegstrId(yy, userId);
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
        final JrnlSumryEntity retrievedEntity = this.getDtlEntity(key);
        retrievedEntity.setDreamComptYn("Y");
        repository.save(retrievedEntity);

        // 관련 캐시 제거
        jrnlCacheEvictWorker.evictAfterCommit(
                JrnlCacheEvictParam.builder()
                        .postNo(key)
                        .yy(retrievedEntity.getYy())
                        .build(),
                ContentType.JRNL_SUMRY
        );

        return true;
    }
}
