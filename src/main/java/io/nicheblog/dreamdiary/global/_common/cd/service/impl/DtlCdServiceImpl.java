package io.nicheblog.dreamdiary.global._common.cd.service.impl;

import io.nicheblog.dreamdiary.global._common._clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global._common.cache.config.CacheableConfig;
import io.nicheblog.dreamdiary.global._common.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.global._common.cache.util.RedisUtils;
import io.nicheblog.dreamdiary.global._common.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.global._common.cd.entity.DtlCdKey;
import io.nicheblog.dreamdiary.global._common.cd.mapstruct.DtlCdMapstruct;
import io.nicheblog.dreamdiary.global._common.cd.model.DtlCdDto;
import io.nicheblog.dreamdiary.global._common.cd.repository.jpa.DtlCdRepository;
import io.nicheblog.dreamdiary.global._common.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.global._common.cd.spec.DtlCdSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.List;

/**
 * DtlCdService
 * <pre>
 *  상세 코드 관리 서비스 모듈
 *  ※상세 코드(dtl_cd) = 분류 코드 하위의 상세 코드. 분류 코드(cl_cd)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Service("dtlCdService")
@RequiredArgsConstructor
@Log4j2
public class DtlCdServiceImpl
        implements DtlCdService {

    @Getter
    private final DtlCdRepository repository;
    @Getter
    private final DtlCdSpec spec;
    @Getter
    private final DtlCdMapstruct mapstruct = DtlCdMapstruct.INSTANCE;

    private final ApplicationContext context;
    private DtlCdServiceImpl getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 공통 - 코드 정보를 Model에 추가합니다.
     *
     * @param clCd 분류 코드
     * @param model ModelMap 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void setCdListToModel(final String clCd, final ModelMap model) throws Exception {
        model.addAttribute(clCd, this.getSelf().getCdDtoListByClCd(clCd));
    }

    /**
     * 분류 코드로 상세 코드 목록 조회 (entity level)
     *
     * @param clCd 분류 코드
     * @return {@link List} -- 상세 코드 목록 (entity level)
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "cdEntityListByClCd", key = "'clCd:' + #clCd", condition = "#clCd!=null", cacheResolver = "cacheResolver")
    @CacheableConfig(cacheTarget = CacheableConfig.CacheTarget.SHARED)
    public List<DtlCdEntity> getCdEntityListByClCd(final String clCd) throws Exception {
        if (StringUtils.isEmpty(clCd)) return null;
        return repository.findByClCdAndStateUseYn(clCd, "Y", Sort.by(Sort.Direction.ASC, "state.sortOrdr"));
    }

    /**
     * 분류 코드로 상세 코드 목록 조회 (dto level)
     *
     * @param clCd 분류 코드
     * @return {@link List} -- 상세 코드 목록 (dto level)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "cdDtoListByClCd", key = "'clCd:' + #clCd", condition = "#clCd!=null", cacheResolver = "cacheResolver")
    @CacheableConfig(cacheTarget = CacheableConfig.CacheTarget.SHARED)
    public List<DtlCdDto> getCdDtoListByClCd(final String clCd) throws Exception {
        if (StringUtils.isEmpty(clCd)) return null;

        // 코드 목록 조회 (entity level)
        final List<DtlCdEntity> rsDtlCdList = this.getCdEntityListByClCd(clCd);
        // Entity -> Dto 변환
        final List<DtlCdDto> rsDtlCdDtoList = new ArrayList<>();
        for (final DtlCdEntity dtlCdEntity : rsDtlCdList) {
            rsDtlCdDtoList.add(mapstruct.toDto(dtlCdEntity));
        }
        return rsDtlCdDtoList;
    }

    /**
     * 분류 코드, 상세 코드로 상세 코드명 조회
     *
     * @param clCd 분류 코드 (String)
     * @param dtlCd 상세 코드 (String)
     * @return {@link String} -- 상세 코드명
     */
    @Override
    @Transactional(readOnly = true)
    public String getDtlCdNm(final String clCd, final String dtlCd) {
        if (StringUtils.isEmpty(clCd) || StringUtils.isEmpty(dtlCd)) return null;
        final DtlCdEntity rsDtlCd = repository.findByClCdAndDtlCd(clCd, dtlCd);
        if (rsDtlCd == null) return null;
        return rsDtlCd.getDtlCdNm();
    }

    /**
     * 등록 전처리. (override)
     *
     * @param dto 등록할 객체
     */
    @Override
    public void preRegist(final DtlCdDto dto) {
        if (dto.getState() == null) dto.setState(new StateCmpstn());
    }

    /**
     * 등록 후처리. (override)
     *
     * @param rslt - 등록된 엔티티
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void postRegist(final DtlCdEntity rslt) throws Exception {
        // 관련 캐시 삭제
        this.evictRelatedCache(rslt);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param rslt - 수정된 엔티티
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final DtlCdEntity rslt) throws Exception {
        // 관련 캐시 삭제
        this.evictRelatedCache(rslt);
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param rslt - 삭제된 엔티티
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void postDelete(final DtlCdEntity rslt) throws Exception {
        // 관련 캐시 삭제
        this.evictRelatedCache(rslt);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param key - 등록된 엔티티의 키
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void postSetState(final DtlCdKey key) throws Exception {
        // 관련 캐시 삭제
        this.evictRelatedCache(key.getClCd());
    }

    /**
     * 등록 후처리. (override)
     *
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public void postSortOrdr(final List<DtlCdDto> sortOrdr) throws Exception {
        // 관련 캐시 삭제
        this.evictRelatedCache(sortOrdr.get(0).getClCd());
    }

    /**
     * 관련 캐시 삭제.
     *
     * @param rslt 캐시 처리할 엔티티
     */
    @Override
    public void evictRelatedCache(final DtlCdEntity rslt) {
        this.evictRelatedCache("cdEntityListByClCd::clCd:" + rslt.getClCd());
        this.evictRelatedCache("cdDtoListByClCd::clCd:" + rslt.getClCd());
        // 연관된 모든 엔티티의 캐시 클리어
        EhCacheUtils.clearL2Cache();
    }

    public void evictRelatedCache(final String cackeKey) {
        RedisUtils.deleteData(cackeKey);
        RedisUtils.deleteData(cackeKey);
        // 연관된 모든 엔티티의 캐시 클리어
        EhCacheUtils.clearL2Cache();
    }
}