package io.nicheblog.dreamdiary.feature.admin.code.service;

import io.nicheblog.dreamdiary.feature.admin.code.mapstruct.CodeItemMapstruct;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeItemDto;
import io.nicheblog.dreamdiary.feature.admin.code.spec.CodeItemSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseSortableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    /** 기준 로케일. 이 로케일의 코드명은 code_item.code_name 이 단일 원천이라 code_item_i18n 에 저장하지 않는다. */
    private static final String BASE_LOCALE = "ko";

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

    /**
     * 등록 (dto level) override.
     * <p>
     * i18n(번역명)은 폼 전용 필드라 엔티티에 매핑되지 않는다. 그런데 상위 {@code regist} 는
     * 후처리에 <b>엔티티에서 만든</b> {@code updatedDto} 를 넘기므로 {@code postRegist} 에서는
     * 폼의 {@code i18nNames} 를 볼 수 없다(항상 비어 있음). 반대로 폼 DTO({@code registDto}) 에는
     * 등록 전 id 가 없다. 두 값(폼 i18n + 새 id)을 모두 확보하려면 이 레벨에서 처리해야 한다.
     * <p>
     * 그래서 상위 등록을 먼저 수행해 새 id 를 받은 뒤, 폼 DTO 에 그 id 를 채워 {@link #saveI18n} 한다.
     *
     * @param registDto 등록할 폼 DTO (i18nNames 보유)
     * @return {@link ServiceResponse} 등록 결과
     */
    @Override
    @Transactional
    public ServiceResponse regist(final CodeItemDto registDto) throws Exception {
        final ServiceResponse response = BaseDtoWritableService.super.regist(registDto);
        if (response.getRsltObj() instanceof CodeItemDto saved && saved.getId() != null) {
            registDto.setId(saved.getId());
            this.saveI18n(registDto);
        }
        return response;
    }

    @Override
    public void postRegist(final CodeItemDto updatedDto) throws Exception {
        this.normalizeSortOrderByGroupCode(updatedDto.getGroupCode());
        this.evictCacheByGroupCode(updatedDto.getGroupCode());
    }

    @Override
    public void postModify(final CodeItemDto postDto, final CodeItemDto updatedDto) throws Exception {
        // 폼 값(i18nNames)을 가진 postDto 로 저장한다. updatedDto 는 엔티티 기반이라 i18n 이 비어 있다.
        this.saveI18n(postDto);
        this.evictCache(updatedDto);
    }

    /**
     * 상세 코드 다국어 저장.
     * 기존 번역을 전부 삭제하고 전달된 locale → 번역명 전체로 교체한다.
     * <p>
     * 변경 전: {@code dto.codeNameEn} 만 읽어 {@code locale='en'} 한 건만 저장했다. 따라서 en 이외의
     * 로케일은 저장할 수단이 없었고, {@code deleteByCodeItemId} 로 지운 뒤 다시 쓰지 않아
     * 저장할 때마다 조용히 사라졌다.
     * 변경 후: {@code i18nNames} 맵 전체를 저장해 어떤 locale 도 보존된다.
     * ko 는 {@code code_item.code_name} 이 단일 기준이므로 저장 대상에서 제외한다.
     *
     * @param dto 저장된 상세 코드 DTO
     */
    private void saveI18n(final CodeItemDto dto) {
        if (dto.getId() == null) return;
        codeItemI18nRepository.deleteByCodeItemId(dto.getId());

        final Map<String, String> i18nNames = dto.getI18nNames();
        if (i18nNames == null || i18nNames.isEmpty()) return;

        for (final Map.Entry<String, String> entry : i18nNames.entrySet()) {
            final String locale = StringUtils.trimToNull(entry.getKey());
            final String codeName = StringUtils.trimToNull(entry.getValue());
            if (locale == null || codeName == null) continue;
            if (BASE_LOCALE.equals(locale)) {
                log.warn("[saveI18n] ko 는 code_item.code_name 이 기준이라 i18n 저장에서 제외. codeItemId={}", dto.getId());
                continue;
            }
            codeItemI18nRepository.save(
                    CodeItemI18nEntity.builder()
                            .codeItemId(dto.getId())
                            .locale(locale)
                            .codeName(codeName)
                            .build()
            );
        }
    }

    /**
     * 상세 코드 id 기준 다국어 번역명 전체 조회.
     * ko 는 {@code code_item.code_name} 이 기준이므로 결과에 포함되지 않는다.
     *
     * @param id 상세 코드 ID
     * @return locale → 번역명 (없으면 빈 맵)
     */
    public Map<String, String> getI18nNames(final Integer id) {
        final Map<String, String> i18nNames = new LinkedHashMap<>();
        if (id == null) return i18nNames;
        for (final CodeItemI18nEntity entity : codeItemI18nRepository.findByCodeItemId(id)) {
            i18nNames.put(entity.getLocale(), entity.getCodeName());
        }
        return i18nNames;
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
