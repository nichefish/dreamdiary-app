package io.nicheblog.dreamdiary.feature.admin.code.service;

import io.nicheblog.dreamdiary.feature.admin.code.mapstruct.CodeItemMapstruct;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeItemDto;
import io.nicheblog.dreamdiary.feature.admin.code.spec.CodeItemSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseSortableService;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntity;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemI18nEntity;
import io.nicheblog.dreamdiary.infrastructure.code.model.CodeLookupItem;
import io.nicheblog.dreamdiary.infrastructure.code.repository.jpa.CodeItemI18nRepository;
import io.nicheblog.dreamdiary.infrastructure.code.repository.jpa.CodeItemRepository;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
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
 * CodeItemService
 * <pre>
 *  상세 코드 CRUD 서비스.
 *  조회/코드명 lookup 은 CodeLookupService(infrastructure)에서 인메모리 캐시로 처리한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CodeItemService
        implements BaseDtoWritableService<CodeItemDto, CodeItemDto, Integer, CodeItemEntity>,
                   BaseSortableService<CodeItemDto, Integer, CodeItemEntity> {

    @Getter
    private final CodeItemRepository repository;
    @Getter
    private final CodeItemSpec spec;
    @Getter
    private final CodeItemMapstruct mapstruct = CodeItemMapstruct.INSTANCE;

    private final CodeLookupService codeLookupService;
    private final CodeItemI18nRepository codeItemI18nRepository;

    public CodeItemMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public CodeItemMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /**
     * 코드 정보를 Model 에 추가.
     */
    public void setCdListToModel(final String groupCode, final ModelMap model) {
        codeLookupService.setCdListToModel(groupCode, model);
    }

    /**
     * 분류코드 기준 상세코드 목록 조회 (entity level).
     */
    @Transactional(readOnly = true)
    public List<CodeItemEntity> getCdEntityListByGroupCode(final String groupCode) {
        if (StringUtils.isEmpty(groupCode)) return null;
        return repository.findByGroupCodeAndUseYnOrderBySortOrderAsc(groupCode, "Y");
    }

    /**
     * 분류코드 기준 상세코드 목록 조회 (dto level).
     */
    @Transactional(readOnly = true)
    public List<CodeItemDto> getCdDtoListByGroupCode(final String groupCode) {
        final List<CodeLookupItem> itemList = codeLookupService.getCdItemListByGroupCode(groupCode);
        if (itemList == null) return null;

        final List<CodeItemDto> dtoList = new ArrayList<>(itemList.size());
        for (final CodeLookupItem item : itemList) {
            dtoList.add(
                    CodeItemDto.builder()
                            .groupCode(item.getGroupCode())
                            .code(item.getCode())
                            .codeName(item.getCodeName())
                            .description(item.getDescription())
                            .sortOrder(item.getSortOrder())
                            .useYn(item.getUseYn())
                            .protectedYn(item.getProtectedYn())
                            .build()
            );
        }
        return dtoList;
    }

    /**
     * 분류코드 + 상세 code 기준 표시명 조회.
     */
    @Transactional(readOnly = true)
    public String getCodeName(final String groupCode, final String code) {
        return codeLookupService.getCodeName(groupCode, code);
    }

    @Override
    public void preRegist(final CodeItemDto registDto) {
        if (StringUtils.isEmpty(registDto.getGroupCode())) return;
        if (registDto.getSortOrder() != null && registDto.getSortOrder() > 0) return;

        final List<CodeItemEntity> existingList = repository.findByGroupCode(registDto.getGroupCode());
        int maxIdx = 0;
        if (existingList != null) {
            for (final CodeItemEntity entity : existingList) {
                if (entity == null || entity.getSortOrder() == null) continue;
                maxIdx = Math.max(maxIdx, entity.getSortOrder());
            }
        }
        registDto.setSortOrder(maxIdx + 1);
    }

    @Override
    public void postRegist(final CodeItemDto updatedDto) throws Exception {
        this.saveI18n(updatedDto);
        this.normalizeSortOrderByGroupCode(updatedDto.getGroupCode());
        this.evictCacheByGroupCode(updatedDto.getGroupCode());
    }

    @Override
    public void postModify(final CodeItemDto postDto, final CodeItemDto updatedDto) {
        this.saveI18n(updatedDto);
        this.evictCache(updatedDto);
    }

    /**
     * 상세 코드 다국어 저장.
     * 기존 번역을 전부 삭제하고 새 값으로 교체한다.
     */
    private void saveI18n(final CodeItemDto dto) {
        if (dto.getId() == null) return;
        codeItemI18nRepository.deleteByCodeItemId(dto.getId());
        if (StringUtils.isNotEmpty(dto.getCodeNameEn())) {
            codeItemI18nRepository.save(
                    CodeItemI18nEntity.builder()
                            .codeItemId(dto.getId())
                            .locale("en")
                            .codeName(dto.getCodeNameEn().trim())
                            .build()
            );
        }
    }

    /**
     * 상세 코드 id 기준 영문 번역명 조회.
     *
     * @param id 상세 코드 ID
     * @return 영문 번역명 (없으면 null)
     */
    public String getCodeNameEn(final Integer id) {
        if (id == null) return null;
        return codeItemI18nRepository.findByCodeItemIdAndLocale(id, "en")
                .map(CodeItemI18nEntity::getCodeName)
                .orElse(null);
    }

    @Override
    public void postDelete(final CodeItemDto deletedDto) throws Exception {
        this.normalizeSortOrderByGroupCode(deletedDto.getGroupCode());
        this.evictCacheByGroupCode(deletedDto.getGroupCode());
    }

    @Override
    public void postSortOrder(final List<CodeItemDto> sortOrders) throws Exception {
        if (sortOrders == null || sortOrders.isEmpty()) return;
        final CodeItemDto first = sortOrders.get(0);
        if (first == null || StringUtils.isEmpty(first.getGroupCode())) return;

        this.normalizeSortOrderByGroupCode(first.getGroupCode());
        this.evictCacheByGroupCode(first.getGroupCode());
    }

    /**
     * 분류코드 단위로 sortOrder를 1부터 순차 재정렬한다.
     */
    @Transactional
    public void normalizeSortOrderByGroupCode(final String groupCode) {
        if (StringUtils.isEmpty(groupCode)) return;

        final List<CodeItemEntity> entityList = repository.findByGroupCodeOrderBySortOrderAscCodeAsc(groupCode);
        if (entityList == null || entityList.isEmpty()) return;

        boolean hasChange = false;
        int nextIdx = 1;
        for (final CodeItemEntity entity : entityList) {
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
    public void evictCache(final CodeItemDto rslt) {
        codeLookupService.evictCodeItemCache(rslt.getGroupCode(), rslt.getCode());
    }

    /**
     * 분류코드 단위 코드 캐시 무효화.
     */
    public void evictCacheByGroupCode(final String groupCode) {
        codeLookupService.evictGroupCodeCache(groupCode);
    }
}
