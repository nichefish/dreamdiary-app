package io.nicheblog.dreamdiary.feature.jrnl.day.service.helper;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlState;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.helper.JrnlDreamViewHelper;
import io.nicheblog.dreamdiary.feature.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.feature.jrnl.entry.service.helper.JrnlEntryViewHelper;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.interceptor.SimpleKey;

import java.util.*;

/**
 * JrnlDayViewHelper
 *
 * @author nichefish
 */
@UtilityClass
public final class JrnlDayViewHelper {

    /**
     * ?곹깭state merge
     *
     * @param listDto ????쇱옄 紐⑸줉
     * @param searchParam 寃???뚮씪誘명꽣
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return;

        final Object cacheKey = new SimpleKey(AuthUtils.requireUserId(AuthUtils.getLgnUserId()), searchParam.getYy(), searchParam.getMnth());

        final Map<Integer, JrnlState> entryMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlEntryStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> diaryMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlDiaryStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> dreamMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlDreamStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> intrptMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlIntrptStateMapByUser", cacheKey)).orElse(Collections.emptyMap());

        JrnlDayViewHelper.applyStates(listDto, entryMap, diaryMap, dreamMap, intrptMap, searchParam);
    }

    /**
     * ?곹깭state merge
     *
     * @param jrnlDay ????쇱옄
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final JrnlDayDto jrnlDay) {
        if (jrnlDay == null) return;

        final Object cacheKey = new org.springframework.cache.interceptor.SimpleKey(AuthUtils.requireUserId(AuthUtils.getLgnUserId()), jrnlDay.getYy(), jrnlDay.getMnth());

        final Map<Integer, JrnlState> entryMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlEntryStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> diaryMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlDiaryStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> dreamMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlDreamStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> intrptMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlIntrptStateMapByUser", cacheKey)).orElse(Collections.emptyMap());

        final List<JrnlDayDto> listDto = List.of(jrnlDay);
        JrnlDayViewHelper.applyStates(listDto, entryMap, diaryMap, dreamMap, intrptMap);
    }

    /**
     * 罹먯떆????λ맂 ?곹깭 留?entry/diary/dream/intrpt)??湲곗??쇰줈 議고쉶??{@link JrnlDayDto} ?몃━ 援ъ“???곹깭瑜?諛섏쁺?쒕떎.
     *
     * @param listDto 議고쉶??????쇱옄 紐⑸줉 DTO
     * @param entryMap entry postNo ??{@link JrnlState} 留?
     * @param diaryMap diary postNo ??{@link JrnlState} 留?
     * @param dreamMap dream postNo ??{@link JrnlState} 留?
     * @param intrptMap intrpt postNo ??{@link JrnlState} 留?
     */
    public static void applyStates(
        final List<JrnlDayDto> listDto,
        final Map<Integer, JrnlState> entryMap,
        final Map<Integer, JrnlState> diaryMap,
        final Map<Integer, JrnlState> dreamMap,
        final Map<Integer, JrnlState> intrptMap
    ) {
        for (JrnlDayDto day : listDto) {
            JrnlEntryViewHelper.applyStates(day.getJrnlEntryList(), entryMap, diaryMap);
            JrnlDreamViewHelper.applyStates(day.getJrnlDreamList(), dreamMap, intrptMap);
        }
    }

    /**
     * 罹먯떆????λ맂 ?곹깭 留?entry/diary/dream/intrpt)??湲곗??쇰줈 議고쉶??{@link JrnlDayDto} ?몃━ 援ъ“???곹깭瑜?諛섏쁺?쒕떎.
     *
     * @param listDto 議고쉶??????쇱옄 紐⑸줉 DTO
     * @param entryMap entry postNo ??{@link JrnlState} 留?
     * @param diaryMap diary postNo ??{@link JrnlState} 留?
     * @param dreamMap dream postNo ??{@link JrnlState} 留?
     * @param intrptMap intrpt postNo ??{@link JrnlState} 留?
     * @param searchParam JrnlDaySearchParam
     */
    public static void applyStates(
        final List<JrnlDayDto> listDto,
        final Map<Integer, JrnlState> entryMap,
        final Map<Integer, JrnlState> diaryMap,
        final Map<Integer, JrnlState> dreamMap,
        final Map<Integer, JrnlState> intrptMap,
        final JrnlDaySearchParam searchParam
    ) {
        for (JrnlDayDto day : listDto) {

            if (searchParam.isShowDiaries()) {
                JrnlEntryViewHelper.applyStates(day.getJrnlEntryList(), entryMap, diaryMap);
            }

            if (searchParam.isShowDreams()) {
                JrnlDreamViewHelper.applyStates(day.getJrnlDreamList(), dreamMap, intrptMap);
            }
        }
    }

    /**
     * Entry媛 collapsed ?곹깭??寃쎌슦, ?섏쐞 {@link JrnlDiaryDto} ?ㅼ뿉 ?ы븿???쒓렇瑜??섏쭛?섏뿬 以묐났 ?쒓굅??"?붿빟 ?쒓렇 紐⑸줉"??Entry??二쇱엥?쒕떎.
     *
     * @param listDto 議고쉶??????쇱옄 紐⑸줉 DTO
     */
    public static void applyEntryTagSummary(final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto)) return;
        if (!searchParam.isShowDiaries()) return;

        for (final JrnlDayDto day : listDto) {
            if (CollectionUtils.isEmpty(day.getJrnlEntryList())) continue;

            for (final JrnlEntryDto entry : day.getJrnlEntryList()) {
                if (CollectionUtils.isEmpty(entry.getJrnlDiaryList())) continue;

                final Map<Integer, TagContentDto> tagMap = new LinkedHashMap<>();

                for (final JrnlDiaryDto diary : entry.getJrnlDiaryList()) {
                    final List<TagContentDto> tagList = diary.getTag().getList();
                    if (CollectionUtils.isEmpty(tagList)) continue;

                    for (final TagContentDto tag : tagList) {
                        tagMap.putIfAbsent(tag.getRefTagNo(), tag);
                    }
                }

                entry.getTag().setList(new ArrayList<>(tagMap.values()));
            }
        }
    }
}
