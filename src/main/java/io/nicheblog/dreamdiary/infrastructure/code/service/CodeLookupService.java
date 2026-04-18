package io.nicheblog.dreamdiary.infrastructure.code.service;

import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntity;
import io.nicheblog.dreamdiary.infrastructure.code.mapstruct.CodeLookupMapstruct;
import io.nicheblog.dreamdiary.infrastructure.code.model.CodeLookupItem;
import io.nicheblog.dreamdiary.infrastructure.code.repository.jpa.CodeItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CodeLookupService.
 * <pre>
 *  코드 조회 전용 서비스.
 *  DB 조회 대신 인메모리 캐시를 우선 사용하고, miss 시 해당 groupCode만 재로딩한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CodeLookupService {

    private static final String USE_YN = "Y";

    private final CodeItemRepository codeItemRepository;
    private final CodeLookupMapstruct codeLookupMapstruct;

    /** groupCode -> 상세코드 목록 캐시 */
    private final Map<String, List<CodeLookupItem>> codeItemListCacheByGroupCode = new ConcurrentHashMap<>();
    /** groupCode + code -> 상세코드명 캐시 */
    private final Map<String, String> codeNameCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void preloadAll() {
        this.reloadAll();
    }

    /**
     * 코드 정보를 Model에 추가.
     */
    public void setCdListToModel(final String groupCode, final ModelMap model) {
        model.addAttribute(groupCode, this.getCdItemListByGroupCode(groupCode));
    }

    /**
     * groupCode 기준 상세코드 목록 조회 (인메모리 우선).
     */
    public List<CodeLookupItem> getCdItemListByGroupCode(final String groupCode) {
        if (StringUtils.isEmpty(groupCode)) return null;

        List<CodeLookupItem> cached = codeItemListCacheByGroupCode.get(groupCode);
        if (cached == null) {
            cached = loadCdItemList(groupCode);
            codeItemListCacheByGroupCode.put(groupCode, cached);
        }
        return cached.isEmpty() ? null : cached;
    }

    /**
     * groupCode + code 기준 코드명 조회 (인메모리 우선).
     */
    public String getCodeName(final String groupCode, final String code) {
        if (StringUtils.isEmpty(groupCode) || StringUtils.isEmpty(code)) return null;

        final String cacheKey = getCodeNameCacheKey(groupCode, code);
        final String cachedNm = codeNameCache.get(cacheKey);
        if (cachedNm != null) return cachedNm;

        final List<CodeLookupItem> itemList = getCdItemListByGroupCode(groupCode);
        if (CollectionUtils.isEmpty(itemList)) return null;

        for (final CodeLookupItem item : itemList) {
            if (!code.equals(item.getCode())) continue;
            codeNameCache.put(cacheKey, item.getCodeName());
            return item.getCodeName();
        }
        return null;
    }

    /**
     * 전체 코드 캐시를 재구성.
     */
    public synchronized void reloadAll() {
        final List<CodeItemEntity> allCdList = codeItemRepository.findAllByUseYnOrderByGroupCodeAscSortOrderAsc(USE_YN);

        codeItemListCacheByGroupCode.clear();
        codeNameCache.clear();
        if (CollectionUtils.isEmpty(allCdList)) return;

        final Map<String, List<CodeLookupItem>> groupedMap = new ConcurrentHashMap<>();
        for (final CodeItemEntity entity : allCdList) {
            final CodeLookupItem item = codeLookupMapstruct.toLookupItem(entity);
            groupedMap.computeIfAbsent(item.getGroupCode(), key -> new ArrayList<>()).add(item);
            codeNameCache.put(getCodeNameCacheKey(item.getGroupCode(), item.getCode()), item.getCodeName());
        }

        groupedMap.forEach((groupCode, itemList) ->
                codeItemListCacheByGroupCode.put(groupCode, Collections.unmodifiableList(itemList))
        );
        log.info("cd cache preloaded. groupCode count: {}, codeItem count: {}",
                codeItemListCacheByGroupCode.size(), codeNameCache.size());
    }

    /**
     * groupCode 단위 캐시 무효화.
     */
    public void evictGroupCodeCache(final String groupCode) {
        if (StringUtils.isEmpty(groupCode)) return;

        codeItemListCacheByGroupCode.remove(groupCode);
        codeNameCache.keySet().removeIf(key -> key.startsWith(groupCode + "::"));
    }

    /**
     * 단일 상세코드 캐시 무효화.
     */
    public void evictCodeItemCache(final String groupCode, final String code) {
        if (StringUtils.isEmpty(groupCode)) return;

        // 목록 캐시를 비워야 codeName 변경/사용여부 변경이 안전하게 반영된다.
        codeItemListCacheByGroupCode.remove(groupCode);
        if (StringUtils.isNotEmpty(code)) {
            codeNameCache.remove(getCodeNameCacheKey(groupCode, code));
        } else {
            codeNameCache.keySet().removeIf(key -> key.startsWith(groupCode + "::"));
        }
    }

    private List<CodeLookupItem> loadCdItemList(final String groupCode) {
        final List<CodeItemEntity> entityList = codeItemRepository.findByGroupCodeAndUseYnOrderBySortOrderAsc(groupCode, USE_YN);
        if (CollectionUtils.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        final List<CodeLookupItem> itemList = new ArrayList<>(entityList.size());
        for (final CodeItemEntity entity : entityList) {
            final CodeLookupItem item = codeLookupMapstruct.toLookupItem(entity);
            itemList.add(item);
            codeNameCache.put(getCodeNameCacheKey(item.getGroupCode(), item.getCode()), item.getCodeName());
        }

        return Collections.unmodifiableList(itemList);
    }

    private String getCodeNameCacheKey(final String groupCode, final String code) {
        return groupCode + "::" + code;
    }
}
