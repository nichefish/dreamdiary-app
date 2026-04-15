package io.nicheblog.dreamdiary.feature.admin.cd.service;

import io.nicheblog.dreamdiary.feature.admin.cd.mapstruct.DtlCdMapstruct;
import io.nicheblog.dreamdiary.feature.admin.cd.model.DtlCdDto;
import io.nicheblog.dreamdiary.feature.admin.cd.spec.DtlCdSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseSortableService;
import io.nicheblog.dreamdiary.infrastructure.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.infrastructure.cd.entity.DtlCdKey;
import io.nicheblog.dreamdiary.infrastructure.cd.model.CdLookupItem;
import io.nicheblog.dreamdiary.infrastructure.cd.repository.jpa.DtlCdRepository;
import io.nicheblog.dreamdiary.infrastructure.cd.service.CdLookupService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DtlCdService
 * <pre>
 *  상세 코드 CRUD 서비스.
 *  조회/코드명 lookup 은 CdLookupService(infrastructure)에서 인메모리 캐시로 처리한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class DtlCdService
        implements BaseDtoWritableService<DtlCdDto, DtlCdDto, DtlCdKey, DtlCdEntity>,
                   BaseSortableService<DtlCdDto, DtlCdKey, DtlCdEntity> {

    @Getter
    private final DtlCdRepository repository;
    @Getter
    private final DtlCdSpec spec;
    @Getter
    private final DtlCdMapstruct mapstruct = DtlCdMapstruct.INSTANCE;

    private final CdLookupService cdLookupService;

    public DtlCdMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public DtlCdMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /**
     * 코드 정보를 Model 에 추가.
     */
    public void setCdListToModel(final String clCd, final ModelMap model) {
        cdLookupService.setCdListToModel(clCd, model);
    }

    /**
     * 분류코드 기준 상세코드 목록 조회 (entity level).
     */
    @Transactional(readOnly = true)
    public List<DtlCdEntity> getCdEntityListByClCd(final String clCd) {
        if (StringUtils.isEmpty(clCd)) return null;
        return repository.findByClCdAndUseYnOrderBySortOrderAsc(clCd, "Y");
    }

    /**
     * 분류코드 기준 상세코드 목록 조회 (dto level).
     */
    @Transactional(readOnly = true)
    public List<DtlCdDto> getCdDtoListByClCd(final String clCd) {
        final List<CdLookupItem> itemList = cdLookupService.getCdItemListByClCd(clCd);
        if (itemList == null) return null;

        final List<DtlCdDto> dtoList = new ArrayList<>(itemList.size());
        for (final CdLookupItem item : itemList) {
            dtoList.add(
                    DtlCdDto.builder()
                            .clCd(item.getClCd())
                            .dtlCd(item.getDtlCd())
                            .dtlCdNm(item.getDtlCdNm())
                            .dc(item.getDc())
                            .sortOrder(item.getSortOrder())
                            .useYn(item.getUseYn())
                            .protectedYn(item.getProtectedYn())
                            .build()
            );
        }
        return dtoList;
    }

    /**
     * 분류코드 + 상세코드 기준 상세코드명 조회.
     */
    @Transactional(readOnly = true)
    public String getDtlCdNm(final String clCd, final String dtlCd) {
        return cdLookupService.getDtlCdNm(clCd, dtlCd);
    }

    @Override
    public void preRegist(final DtlCdDto registDto) {
        if (StringUtils.isEmpty(registDto.getClCd())) return;
        if (registDto.getSortOrder() != null && registDto.getSortOrder() > 0) return;

        final List<DtlCdEntity> existingList = repository.findByClCd(registDto.getClCd());
        int maxIdx = 0;
        if (existingList != null) {
            for (final DtlCdEntity entity : existingList) {
                if (entity == null || entity.getSortOrder() == null) continue;
                maxIdx = Math.max(maxIdx, entity.getSortOrder());
            }
        }
        registDto.setSortOrder(maxIdx + 1);
    }

    @Override
    public void postRegist(final DtlCdDto updatedDto) throws Exception {
        this.normalizeSortOrderByClCd(updatedDto.getClCd());
        this.evictCacheByClCd(updatedDto.getClCd());
    }

    @Override
    public void postModify(final DtlCdDto postDto, final DtlCdDto updatedDto) {
        this.evictCache(updatedDto);
    }

    @Override
    public void postDelete(final DtlCdDto deletedDto) throws Exception {
        this.normalizeSortOrderByClCd(deletedDto.getClCd());
        this.evictCacheByClCd(deletedDto.getClCd());
    }

    @Override
    public void postSortOrder(final List<DtlCdDto> sortOrders) throws Exception {
        if (sortOrders == null || sortOrders.isEmpty()) return;
        final DtlCdDto first = sortOrders.get(0);
        if (first == null || StringUtils.isEmpty(first.getClCd())) return;

        this.normalizeSortOrderByClCd(first.getClCd());
        this.evictCacheByClCd(first.getClCd());
    }

    /**
     * 분류코드 단위로 sortOrder를 1부터 순차 재정렬한다.
     */
    @Transactional
    public void normalizeSortOrderByClCd(final String clCd) {
        if (StringUtils.isEmpty(clCd)) return;

        final List<DtlCdEntity> entityList = repository.findByClCdOrderBySortOrderAscDtlCdAsc(clCd);
        if (entityList == null || entityList.isEmpty()) return;

        boolean hasChange = false;
        int nextIdx = 1;
        for (final DtlCdEntity entity : entityList) {
            if (entity == null) continue;
            if (!Objects.equals(entity.getSortOrder(), nextIdx)) {
                entity.setSortOrder(nextIdx);
                hasChange = true;
            }
            nextIdx++;
        }
        if (hasChange) repository.saveAllAndFlush(entityList);
    }

    /**
     * 코드 캐시 무효화.
     */
    public void evictCache(final DtlCdDto rslt) {
        cdLookupService.evictDtlCdCache(rslt.getClCd(), rslt.getDtlCd());
    }

    /**
     * 분류코드 단위 코드 캐시 무효화.
     */
    public void evictCacheByClCd(final String clCd) {
        cdLookupService.evictClCdCache(clCd);
    }
}

