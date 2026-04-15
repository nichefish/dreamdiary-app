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
 *  DB 조회 대신 인메모리 캐시를 우선 사용하고, miss 시 해당 clCd만 재로딩한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CodeLookupService {

    private static final String USE_YN = "Y";

    private final CodeItemRepository dtlCdRepository;
    private final CodeLookupMapstruct codeLookupMapstruct;

    /** clCd -> 상세코드 목록 캐시 */
    private final Map<String, List<CodeLookupItem>> codeItemListCacheByClCd = new ConcurrentHashMap<>();
    /** clCd + dtlCd -> 상세코드명 캐시 */
    private final Map<String, String> dtlCdNmCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void preloadAll() {
        this.reloadAll();
    }

    /**
     * 코드 정보를 Model에 추가.
     */
    public void setCdListToModel(final String clCd, final ModelMap model) {
        model.addAttribute(clCd, this.getCdItemListByClCd(clCd));
    }

    /**
     * clCd 기준 상세코드 목록 조회 (인메모리 우선).
     */
    public List<CodeLookupItem> getCdItemListByClCd(final String clCd) {
        if (StringUtils.isEmpty(clCd)) return null;

        List<CodeLookupItem> cached = codeItemListCacheByClCd.get(clCd);
        if (cached == null) {
            cached = loadCdItemList(clCd);
            codeItemListCacheByClCd.put(clCd, cached);
        }
        return cached.isEmpty() ? null : cached;
    }

    /**
     * clCd + dtlCd 기준 코드명 조회 (인메모리 우선).
     */
    public String getDtlCdNm(final String clCd, final String dtlCd) {
        if (StringUtils.isEmpty(clCd) || StringUtils.isEmpty(dtlCd)) return null;

        final String cacheKey = getDtlCdNmCacheKey(clCd, dtlCd);
        final String cachedNm = dtlCdNmCache.get(cacheKey);
        if (cachedNm != null) return cachedNm;

        final List<CodeLookupItem> dtlCdList = getCdItemListByClCd(clCd);
        if (CollectionUtils.isEmpty(dtlCdList)) return null;

        for (final CodeLookupItem item : dtlCdList) {
            if (!dtlCd.equals(item.getDtlCd())) continue;
            dtlCdNmCache.put(cacheKey, item.getDtlCdNm());
            return item.getDtlCdNm();
        }
        return null;
    }

    /**
     * 전체 코드 캐시를 재구성.
     */
    public synchronized void reloadAll() {
        final List<CodeItemEntity> allCdList = dtlCdRepository.findAllByUseYnOrderByClCdAscSortOrderAsc(USE_YN);

        codeItemListCacheByClCd.clear();
        dtlCdNmCache.clear();
        if (CollectionUtils.isEmpty(allCdList)) return;

        final Map<String, List<CodeLookupItem>> groupedMap = new ConcurrentHashMap<>();
        for (final CodeItemEntity entity : allCdList) {
            final CodeLookupItem item = codeLookupMapstruct.toLookupItem(entity);
            groupedMap.computeIfAbsent(item.getClCd(), key -> new ArrayList<>()).add(item);
            dtlCdNmCache.put(getDtlCdNmCacheKey(item.getClCd(), item.getDtlCd()), item.getDtlCdNm());
        }

        groupedMap.forEach((clCd, itemList) ->
                codeItemListCacheByClCd.put(clCd, Collections.unmodifiableList(itemList))
        );
        log.info("cd cache preloaded. clCd count: {}, dtlCd count: {}",
                codeItemListCacheByClCd.size(), dtlCdNmCache.size());
    }

    /**
     * clCd 단위 캐시 무효화.
     */
    public void evictClCdCache(final String clCd) {
        if (StringUtils.isEmpty(clCd)) return;

        codeItemListCacheByClCd.remove(clCd);
        dtlCdNmCache.keySet().removeIf(key -> key.startsWith(clCd + "::"));
    }

    /**
     * 단일 상세코드 캐시 무효화.
     */
    public void evictDtlCdCache(final String clCd, final String dtlCd) {
        if (StringUtils.isEmpty(clCd)) return;

        // 목록 캐시를 비워야 dtlCdNm 변경/사용여부 변경이 안전하게 반영된다.
        codeItemListCacheByClCd.remove(clCd);
        if (StringUtils.isNotEmpty(dtlCd)) {
            dtlCdNmCache.remove(getDtlCdNmCacheKey(clCd, dtlCd));
        } else {
            dtlCdNmCache.keySet().removeIf(key -> key.startsWith(clCd + "::"));
        }
    }

    private List<CodeLookupItem> loadCdItemList(final String clCd) {
        final List<CodeItemEntity> dtlCdEntityList = dtlCdRepository.findByClCdAndUseYnOrderBySortOrderAsc(clCd, USE_YN);
        if (CollectionUtils.isEmpty(dtlCdEntityList)) {
            return Collections.emptyList();
        }

        final List<CodeLookupItem> itemList = new ArrayList<>(dtlCdEntityList.size());
        for (final CodeItemEntity entity : dtlCdEntityList) {
            final CodeLookupItem item = codeLookupMapstruct.toLookupItem(entity);
            itemList.add(item);
            dtlCdNmCache.put(getDtlCdNmCacheKey(item.getClCd(), item.getDtlCd()), item.getDtlCdNm());
        }

        return Collections.unmodifiableList(itemList);
    }

    private String getDtlCdNmCacheKey(final String clCd, final String dtlCd) {
        return clCd + "::" + dtlCd;
    }
}
