package io.nicheblog.dreamdiary.feature.jrnl.day.service.helper;

import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlState;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.helper.JrnlDreamViewHelper;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.model.JrnlChapterDto;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.service.helper.JrnlChapterViewHelper;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
     * 상태state merge
     *
     * @param listDto 저널 일자 목록
     * @param searchParam 검색 파라미터
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final String username, final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return;

        final Object cacheKey = new SimpleKey(username, searchParam.getYy(), searchParam.getMnth());

        final Map<Integer, JrnlState> chapterMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlChapterStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> diaryMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlDiaryStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> dreamMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlDreamStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> intrptMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlIntrptStateMapByUser", cacheKey)).orElse(Collections.emptyMap());

        JrnlDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, intrptMap, searchParam);
    }

    /**
     * 주간 조회용 state merge
     *
     * @param listDto 일자 목록
     * @param searchParam 검색 파라미터
     */
    @SuppressWarnings("unchecked")
    public static void mergeWeeklyStates(final String username, final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return;
        if (StringUtils.isBlank(searchParam.getWeekStartDt())) return;

        final Object cacheKey = new SimpleKey(
                username,
                searchParam.getWeekStartDt()
        );

        final Map<Integer, JrnlState> chapterMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlChapterWeeklyStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> diaryMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlDiaryWeeklyStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> dreamMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlDreamWeeklyStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> intrptMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlIntrptWeeklyStateMapByUser", cacheKey)).orElse(Collections.emptyMap());

        JrnlDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, intrptMap, searchParam);
    }

    /**
     * 상태state merge
     *
     * @param jrnlDay 저널 일자
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final String username, final JrnlDayDto jrnlDay) {
        if (jrnlDay == null) return;

        final Object cacheKey = new SimpleKey(username, jrnlDay.getYy(), jrnlDay.getMnth());

        final Map<Integer, JrnlState> chapterMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlChapterStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> diaryMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlDiaryStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> dreamMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlDreamStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> intrptMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("jrnlIntrptStateMapByUser", cacheKey)).orElse(Collections.emptyMap());

        final List<JrnlDayDto> listDto = List.of(jrnlDay);
        JrnlDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, intrptMap);
    }

    /**
     * 캐시에 저장된 상태 맵(chapter/diary/dream/intrpt)을 기준으로 조회된 {@link JrnlDayDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param chapterMap chapter id → {@link JrnlState} 맵
     * @param diaryMap diary id → {@link JrnlState} 맵
     * @param dreamMap dream id → {@link JrnlState} 맵
     * @param intrptMap intrpt id → {@link JrnlState} 맵
     */
    public static void applyStates(
        final List<JrnlDayDto> listDto,
        final Map<Integer, JrnlState> chapterMap,
        final Map<Integer, JrnlState> diaryMap,
        final Map<Integer, JrnlState> dreamMap,
        final Map<Integer, JrnlState> intrptMap
    ) {
        for (JrnlDayDto day : listDto) {
            JrnlChapterViewHelper.applyStates(day.getJrnlChapterList(), chapterMap, diaryMap);
            JrnlDreamViewHelper.applyStates(day.getJrnlDreamList(), dreamMap, intrptMap);
            JrnlDreamViewHelper.applyStates(day.getJrnlElseDreamList(), dreamMap, intrptMap);
        }
    }

    /**
     * 캐시에 저장된 상태 맵(chapter/diary/dream/intrpt)을 기준으로 조회된 {@link JrnlDayDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param chapterMap chapter id → {@link JrnlState} 맵
     * @param diaryMap diary id → {@link JrnlState} 맵
     * @param dreamMap dream id → {@link JrnlState} 맵
     * @param intrptMap intrpt id → {@link JrnlState} 맵
     * @param searchParam JrnlDaySearchParam
     */
    public static void applyStates(
        final List<JrnlDayDto> listDto,
        final Map<Integer, JrnlState> chapterMap,
        final Map<Integer, JrnlState> diaryMap,
        final Map<Integer, JrnlState> dreamMap,
        final Map<Integer, JrnlState> intrptMap,
        final JrnlDaySearchParam searchParam
    ) {
        for (JrnlDayDto day : listDto) {

            if (searchParam.isShowDiaries()) {
                JrnlChapterViewHelper.applyStates(day.getJrnlChapterList(), chapterMap, diaryMap);
            }

            if (searchParam.isShowDreams()) {
                JrnlDreamViewHelper.applyStates(day.getJrnlDreamList(), dreamMap, intrptMap);
                JrnlDreamViewHelper.applyStates(day.getJrnlElseDreamList(), dreamMap, intrptMap);
            }
        }
    }

    /**
     * Entry가 collapsed 상태일 경우, 하위 {@link JrnlDiaryDto} 들에 포함된 태그를 수집하여 중복 제거된 "요약 태그 목록"을 Entry에 주입한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     */
    public static void applyChapterTagSummary(final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto)) return;
        if (!searchParam.isShowDiaries()) return;

        for (final JrnlDayDto day : listDto) {
            if (CollectionUtils.isEmpty(day.getJrnlChapterList())) continue;

            for (final JrnlChapterDto chapter : day.getJrnlChapterList()) {
                if (CollectionUtils.isEmpty(chapter.getJrnlDiaryList())) continue;

                final Map<Integer, TagContentDto> tagMap = new LinkedHashMap<>();

                for (final JrnlDiaryDto diary : chapter.getJrnlDiaryList()) {
                    final List<TagContentDto> tagList = diary.getTag().getList();
                    if (CollectionUtils.isEmpty(tagList)) continue;

                    for (final TagContentDto tag : tagList) {
                        tagMap.putIfAbsent(tag.getTagId(), tag);
                    }
                }

                chapter.getTag().setList(new ArrayList<>(tagMap.values()));
            }
        }
    }
}
