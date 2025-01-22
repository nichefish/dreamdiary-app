package io.nicheblog.dreamdiary.domain.jrnl.sumry.service.impl;

import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.entity.JrnlSumryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.mapstruct.JrnlSumryMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.model.JrnlSumryDto;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.repository.jpa.JrnlSumryRepository;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.service.JrnlSumryService;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.spec.JrnlSumrySpec;
import io.nicheblog.dreamdiary.global._common._clsf.ContentType;
import io.nicheblog.dreamdiary.global._common.cache.event.EhCacheEvictEvent;
import io.nicheblog.dreamdiary.global._common.cache.handler.EhCacheEvictEventListner;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
public class JrnlSumryServiceImpl
        implements JrnlSumryService {

    @Getter
    private final JrnlSumryRepository repository;
    @Getter
    private final JrnlSumrySpec spec;
    @Getter
    private final JrnlSumryMapstruct mapstruct = JrnlSumryMapstruct.INSTANCE;

    private final ApplicationEventPublisher publisher;

    private final String JRNL_SUMRY = ContentType.JRNL_SUMRY.key;

    private final ApplicationContext context;
    private JrnlSumryServiceImpl getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 저널 결산 정뵤 목록 조회 :: 캐시 사용 위해 구현체로 pullUp
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link List<JrnlSumryDto.LIST>} -- 검색 조건에 맞는 결산 목록 DTO 리스트
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myJrnlSumryList", key="T(io.nicheblog.dreamdiary.auth.util.AuthUtils).getLgnUserId()")
    public List<JrnlSumryDto.LIST> getMyListDto(final BaseSearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.getLgnUserId());
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);

        return this.getSelf().getListDto(searchParamMap);
    }

    /**
     * 년도를 받아서 해당 년도 저널 결산 정보 생성
     *
     * @return {@link Boolean} -- 결산 생성 성공 여부 (항상 true 반환)
     * @throws Exception 결산 생성 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value={"myJrnlTotalSumry", "myJrnlSumryList"}, allEntries = true),
            @CacheEvict(value="myJrnlSumryDtlByYy", key="T(io.nicheblog.dreamdiary.auth.util.AuthUtils).getLgnUserId() + \"_\" + #rslt.getYy()")
    })
    public Boolean makeYySumry(final Integer yy) throws Exception {
        final String regstrId = AuthUtils.getLgnUserId();
        // 해당 년도 저널 결산 정보 조회
        final JrnlSumryEntity sumry = repository.findByYyAndRegstrId(yy, regstrId).orElse(new JrnlSumryEntity(yy));

        // 해당 년도 꿈 일자 조회해서 갱신
        final Integer dreamDayCntByYy = repository.getDreamDayCntByYy(yy, regstrId);
        sumry.setDreamDayCnt(dreamDayCntByYy);
        // 해당 년도 꿈 조회해서 갱신
        final Integer dreamCntByYy = repository.getDreamCntByYy(yy, regstrId);
        sumry.setDreamCnt(dreamCntByYy);
        // 해당 년도 일기 일자 조회해서 갱신
        final Integer diaryCntByYy = repository.getDiaryDayCntByYy(yy, regstrId);
        sumry.setDiaryDayCnt(diaryCntByYy);

        repository.save(sumry);

        return true;
    }

    /**
     * 2011년부터 현재 년도까지의 저널 결산 정보 생성
     *
     * @return {@link Boolean} -- 결산 생성 성공 여부 (항상 true 반환)
     * @throws Exception 결산 생성 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    @CacheEvict(value={"myJrnlTotalSumry", "myJrnlSumryList", "myJrnlSumryDtl"}, allEntries = true)
    public Boolean makeTotalYySumry() throws Exception {
        final int currYy = DateUtils.getCurrYy();
        final int startYy = 2011;
        for (int yy = startYy; yy <= currYy; yy++) {
            try {
                this.makeYySumry(yy);
            } catch (final Exception e) {
                log.warn("Error creating annual summary for {}", yy);
            }
        }

        return true;
    }

    /**
     * 관련 정보를 취합하여 총 저널 결산 정보를 생성합니다. (캐시 처리)
     * 
     * @return {@link JrnlSumryDto} -- 총 결산 정보가 담긴 DTO 객체
     */
    @Override
    @Cacheable(value="myJrnlTotalSumry", key="T(io.nicheblog.dreamdiary.auth.util.AuthUtils).getLgnUserId()")
    public JrnlSumryDto getTotalSumry() {
        final String regstrId = AuthUtils.getLgnUserId();
        final JrnlSumryDto totalSumry = new JrnlSumryDto();
        // 해당 년도 꿈 일자 조회해서 갱신
        final Integer dreamDayCntByYy = repository.getTotalDreamDayCnt(regstrId);
        totalSumry.setDreamDayCnt(dreamDayCntByYy);
        // 해당 년도 꿈 조회해서 갱신
        final Integer dreamCntByYy = repository.getTotalDreamCnt(regstrId);
        totalSumry.setDreamCnt(dreamCntByYy);
        // 해당 년도 일기 일자 조회해서 갱신
        final Integer diaryCntByYy = repository.getTotalDiaryDayCnt(regstrId);
        totalSumry.setDiaryDayCnt(diaryCntByYy);

        return totalSumry;
    }

    /**
     * 저널 결산 상세 정보 조회 (캐시 처리)
     *
     * @param key 식별자
     * @return {@link JrnlSumryDto.DTL} -- 조회된 결산 정보가 담긴 DTO 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myJrnlSumryDtl", key="T(io.nicheblog.dreamdiary.auth.util.AuthUtils).getLgnUserId() + \"_\" + #key")
    public JrnlSumryDto.DTL getSumryDtl(final Integer key) throws Exception {
        return this.getSelf().getDtlDto(key);
    }

    /**
     * 년도별 저널 결산 정보 조회 (캐시 처리)
     *
     * @param yy 조회할 년도
     * @return {@link JrnlSumryDto} -- 조회된 결산 정보가 담긴 DTO 객체, 없을 경우 null 반환
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myJrnlSumryDtlByYy", key="T(io.nicheblog.dreamdiary.auth.util.AuthUtils).getLgnUserId() + \"_\" + #yy")
    public JrnlSumryDto getDtlDtoByYy(final Integer yy) throws Exception {
        final Optional<JrnlSumryEntity> retrievedWrapper = repository.findByYyAndRegstrId(yy, AuthUtils.getLgnUserId());
        if (retrievedWrapper.isEmpty()) return null;

        return mapstruct.toDto(retrievedWrapper.get());
    }

    /**
     * 저널 결산 꿈 기록 완료 처리
     *
     * @param key 식별자
     * @return {@link boolean} -- 처리 성공 여부
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see EhCacheEvictEventListner
     */
    @Override
    @Transactional
    public boolean dreamCompt(final Integer key) throws Exception {
        final JrnlSumryEntity retrievedEntity = this.getDtlEntity(key);
        retrievedEntity.setDreamComptYn("Y");
        repository.save(retrievedEntity);
        
        // 캐시 초기화
        publisher.publishEvent(new EhCacheEvictEvent(this, key, JRNL_SUMRY));

        return true;
    }
}