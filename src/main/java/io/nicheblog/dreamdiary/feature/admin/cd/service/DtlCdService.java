package io.nicheblog.dreamdiary.feature.admin.cd.service;

import io.nicheblog.dreamdiary.feature.admin.cd.mapstruct.DtlCdMapstruct;
import io.nicheblog.dreamdiary.feature.admin.cd.model.DtlCdDto;
import io.nicheblog.dreamdiary.feature.admin.cd.spec.DtlCdSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
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

/**
 * DtlCdService
 * <pre>
 *  상세 코드 CRUD 서비스.
 *  조회/코드명 lookup 은 CdLookupService(infrastructure)에서 인메모리 캐시로 처리한다.
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
        return repository.findByClCdAndUseYnOrderByIdxAsc(clCd, "Y");
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
    public void postRegist(final DtlCdDto updatedDto) {
        this.evictCache(updatedDto);
    }

    @Override
    public void postModify(final DtlCdDto postDto, final DtlCdDto updatedDto) {
        this.evictCache(updatedDto);
    }

    @Override
    public void postDelete(final DtlCdDto deletedDto) {
        this.evictCache(deletedDto);
    }

    /**
     * 코드 캐시 무효화.
     */
    public void evictCache(final DtlCdDto rslt) {
        cdLookupService.evictDtlCdCache(rslt.getClCd(), rslt.getDtlCd());
    }
}
