package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.helper.JournaaChapterViewHelper;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.dream.service.helper.JournalDreamViewHelper;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.interceptor.SimpleKey;

import java.util.*;

/**
 * JournalDayViewHelper
 *
 * @author nichefish
 */
@UtilityClass
public final class JournalDayViewHelper {

    /**
     * 상태state merge
     *
     * @param listDto 저널 일자 목록
     * @param searchParam 검색 파라미터
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final String username, final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return;

        final Object cacheKey = new SimpleKey(username, searchParam.getYy(), searchParam.getMnth());

        final Map<Integer, JournalState> chapterMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalChapterStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JournalState> diaryMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalDiaryStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JournalState> dreamMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalDreamStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JournalState> intrptMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalIntrptStateMapByUser", cacheKey)).orElse(Collections.emptyMap());

        JournalDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, intrptMap, searchParam);
    }

    /**
     * 주간 조회용 state merge
     *
     * @param listDto 일자 목록
     * @param searchParam 검색 파라미터
     */
    @SuppressWarnings("unchecked")
    public static void mergeWeeklyStates(final String username, final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return;
        if (StringUtils.isBlank(searchParam.getWeekStartDt())) return;

        final Object cacheKey = new SimpleKey(
                username,
                searchParam.getWeekStartDt()
        );

        final Map<Integer, JournalState> chapterMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalChapterWeeklyStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JournalState> diaryMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalDiaryWeeklyStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JournalState> dreamMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalDreamWeeklyStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JournalState> intrptMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalIntrptWeeklyStateMapByUser", cacheKey)).orElse(Collections.emptyMap());

        JournalDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, intrptMap, searchParam);
    }

    /**
     * 상태state merge
     *
     * @param journalDay 저널 일자
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final String username, final JournalDayDto journalDay) {
        if (journalDay == null) return;

        final Object cacheKey = new SimpleKey(username, journalDay.getYy(), journalDay.getMnth());

        final Map<Integer, JournalState> chapterMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalChapterStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JournalState> diaryMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalDiaryStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JournalState> dreamMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalDreamStateMapByUser",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JournalState> intrptMap = Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache("journalIntrptStateMapByUser", cacheKey)).orElse(Collections.emptyMap());

        final List<JournalDayDto> listDto = List.of(journalDay);
        JournalDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, intrptMap);
    }

    /**
     * 캐시에 저장된 상태 맵(chapter/diary/dream/intrpt)을 기준으로 조회된 {@link JournalDayDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param chapterMap chapter id → {@link JournalState} 맵
     * @param diaryMap diary id → {@link JournalState} 맵
     * @param dreamMap dream id → {@link JournalState} 맵
     * @param intrptMap intrpt id → {@link JournalState} 맵
     */
    public static void applyStates(
        final List<JournalDayDto> listDto,
        final Map<Integer, JournalState> chapterMap,
        final Map<Integer, JournalState> diaryMap,
        final Map<Integer, JournalState> dreamMap,
        final Map<Integer, JournalState> intrptMap
    ) {
        for (JournalDayDto day : listDto) {
            JournaaChapterViewHelper.applyStates(day.getJournalChapterList(), chapterMap, diaryMap);
            JournalDreamViewHelper.applyStates(day.getJournalDreamList(), dreamMap, intrptMap);
            JournalDreamViewHelper.applyStates(day.getJournalElseDreamList(), dreamMap, intrptMap);
        }
    }

    /**
     * 캐시에 저장된 상태 맵(chapter/diary/dream/intrpt)을 기준으로 조회된 {@link JournalDayDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param chapterMap chapter id → {@link JournalState} 맵
     * @param diaryMap diary id → {@link JournalState} 맵
     * @param dreamMap dream id → {@link JournalState} 맵
     * @param intrptMap intrpt id → {@link JournalState} 맵
     * @param searchParam JournalDaySearchParam
     */
    public static void applyStates(
        final List<JournalDayDto> listDto,
        final Map<Integer, JournalState> chapterMap,
        final Map<Integer, JournalState> diaryMap,
        final Map<Integer, JournalState> dreamMap,
        final Map<Integer, JournalState> intrptMap,
        final JournalDaySearchParam searchParam
    ) {
        for (JournalDayDto day : listDto) {

            if (searchParam.isShowDiaries()) {
                JournaaChapterViewHelper.applyStates(day.getJournalChapterList(), chapterMap, diaryMap);
            }

            if (searchParam.isShowDreams()) {
                JournalDreamViewHelper.applyStates(day.getJournalDreamList(), dreamMap, intrptMap);
                JournalDreamViewHelper.applyStates(day.getJournalElseDreamList(), dreamMap, intrptMap);
            }
        }
    }

    /**
     * Entry가 collapsed 상태일 경우, 하위 {@link JournalDiaryDto} 들에 포함된 태그를 수집하여 중복 제거된 "요약 태그 목록"을 Entry에 주입한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     */
    public static void applyChapterTagSummary(final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto)) return;
        if (!searchParam.isShowDiaries()) return;

        for (final JournalDayDto day : listDto) {
            if (CollectionUtils.isEmpty(day.getJournalChapterList())) continue;

            for (final JournalChapterDto chapter : day.getJournalChapterList()) {
                if (CollectionUtils.isEmpty(chapter.getJournalDiaryList())) continue;

                final Map<Integer, TagContentDto> tagMap = new LinkedHashMap<>();

                for (final JournalDiaryDto diary : chapter.getJournalDiaryList()) {
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

