package io.nicheblog.dreamdiary.infrastructure.cd.service;

import io.nicheblog.dreamdiary.infrastructure.cache.config.CacheableConfig;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.RedisUtils;
import io.nicheblog.dreamdiary.infrastructure.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.infrastructure.cd.entity.DtlCdKey;
import io.nicheblog.dreamdiary.infrastructure.cd.mapstruct.DtlCdMapstruct;
import io.nicheblog.dreamdiary.infrastructure.cd.model.DtlCdDto;
import io.nicheblog.dreamdiary.infrastructure.cd.repository.jpa.DtlCdRepository;
import io.nicheblog.dreamdiary.infrastructure.cd.spec.DtlCdSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;

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
public class DtlCdService
        implements BaseDtoWritableService<DtlCdDto, DtlCdDto, DtlCdKey, DtlCdEntity> {

    @Getter
    private final DtlCdRepository repository;
    @Getter
    private final DtlCdSpec spec;
    @Getter
    private final DtlCdMapstruct mapstruct = DtlCdMapstruct.INSTANCE;

    public DtlCdMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public DtlCdMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationContext context;
    private DtlCdService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 공통 - 코드 정보를 Model에 추가합니다.
     *
     * @param clCd 분류 코드
     * @param model ModelMap 객체
     */
    public void setCdListToModel(final String clCd, final ModelMap model) throws Exception {
        model.addAttribute(clCd, this.getSelf().getCdDtoListByClCd(clCd));
    }

    /**
     * 분류 코드로 상세 코드 목록 조회 (entity level)
     *
     * @param clCd 분류 코드
     * @return {@link List} -- 상세 코드 목록 (entity level)
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "cdEntityListByClCd", key = "'clCd:' + #clCd", condition = "#clCd!=null", cacheResolver = "cacheResolver")
    @CacheableConfig(cacheTarget = CacheableConfig.CacheTarget.SHARED)
    public List<DtlCdEntity> getCdEntityListByClCd(final String clCd) throws Exception {
        if (StringUtils.isEmpty(clCd)) return null;
        return null;
        // return repository.findByClCdAndStateUseYn(clCd, "Y", Sort.by(Sort.Direction.ASC, "idx"));
    }

    /**
     * 분류 코드로 상세 코드 목록 조회 (dto level)
     *
     * @param clCd 분류 코드
     * @return {@link List} -- 상세 코드 목록 (dto level)
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "cdDtoListByClCd", key = "'clCd:' + #clCd", condition = "#clCd!=null", cacheResolver = "cacheResolver")
    @CacheableConfig(cacheTarget = CacheableConfig.CacheTarget.SHARED)
    public List<DtlCdDto> getCdDtoListByClCd(final String clCd) throws Exception {
        if (StringUtils.isEmpty(clCd)) return null;

        // 코드 목록 조회 (entity level)
        final List<DtlCdEntity> rsDtlCdList = this.getCdEntityListByClCd(clCd);
        if (CollectionUtils.isEmpty(rsDtlCdList)) return null;
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
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "dtlCdNm", key = "'clCd:' + #clCd + ',dtlCd:' + #dtlCd", condition = "#clCd!=null and #dtlCd!=null")
    public String getDtlCdNm(final String clCd, final String dtlCd) {
        if (StringUtils.isEmpty(clCd) || StringUtils.isEmpty(dtlCd)) return null;
        final DtlCdEntity rsDtlCd = repository.findByClCdAndDtlCd(clCd, dtlCd);
        if (rsDtlCd == null) return null;
        return rsDtlCd.getDtlCdNm();
    }

    /**
     * 등록 후처리 (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final DtlCdDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        this.evictCache(updatedDto);
    }

    /**
     * 수정 후처리 (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final DtlCdDto postDto, final DtlCdDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        this.evictCache(updatedDto);
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final DtlCdDto deletedDto) throws Exception {
        // 관련 캐시 삭제
        this.evictCache(deletedDto);
    }

    /**
     * 관련 캐시 삭제.
     *
     * @param rslt 캐시 처리할 엔티티
     */
    public void evictCache(final DtlCdDto rslt) {
        RedisUtils.deleteData("cdEntityListByClCd::clCd:" + rslt.getClCd());
        RedisUtils.deleteData("cdDtoListByClCd::clCd:" + rslt.getClCd());
        EhCacheUtils.evictCache("dtlCdNm", "clCd:"+ rslt.getClCd() +",dtlCd:"+ rslt.getDtlCd());
    }
}
