package io.nicheblog.dreamdiary.feature.jrnl.day.service.helper;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.helper.JrnlDreamViewHelper;
import io.nicheblog.dreamdiary.feature.jrnl.entry.entity.JrnlEntryEntity;
import io.nicheblog.dreamdiary.feature.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.feature.jrnl.entry.service.helper.JrnlEntryViewHelper;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.entity.JrnlIntrptEntity;
import io.nicheblog.dreamdiary.feature.jrnl.state.JrnlState;
import io.nicheblog.dreamdiary.feature.jrnl.state.JrnlStateMaps;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * JrnlDayViewHelper
 *
 * @author nichefish
 */
public final class JrnlDayViewHelper {

    /**
     * 상태state merge
     *
     * @param listDto 저널 일자 목록
     * @param searchParam 검색 파라미터
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return;

        final String cacheKey = AuthUtils.getLgnUserId() + "_" + searchParam.getYy() + "_" + searchParam.getMnth();

        final Map<Integer, JrnlState> entryMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myEntryStateMap",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> diaryMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myDiaryStateMap",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> dreamMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myDreamStateMap",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> intrptMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myIntrptStateMap", cacheKey)).orElse(Collections.emptyMap());

        JrnlDayViewHelper.applyStates(listDto, entryMap, diaryMap, dreamMap, intrptMap, searchParam);
    }

    /**
     * 상태state merge
     *
     * @param jrnlDay 저널 일자
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final JrnlDayDto jrnlDay) {
        if (jrnlDay == null) return;

        final String cacheKey = AuthUtils.getLgnUserId() + "_" + jrnlDay.getYy() + "_" + jrnlDay.getMnth();

        final Map<Integer, JrnlState> entryMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myEntryStateMap",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> diaryMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myDiaryStateMap",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> dreamMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myDreamStateMap",  cacheKey)).orElse(Collections.emptyMap());
        final Map<Integer, JrnlState> intrptMap = Optional.ofNullable((Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myIntrptStateMap", cacheKey)).orElse(Collections.emptyMap());

        final List<JrnlDayDto> listDto = List.of(jrnlDay);
        JrnlDayViewHelper.applyStates(listDto, entryMap, diaryMap, dreamMap, intrptMap);
    }

    /**
     * 캐시에 저장된 상태 맵(entry/diary/dream/intrpt)을 기준으로 조회된 {@link JrnlDayDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param entryMap entry postNo → {@link JrnlState} 맵
     * @param diaryMap diary postNo → {@link JrnlState} 맵
     * @param dreamMap dream postNo → {@link JrnlState} 맵
     * @param intrptMap intrpt postNo → {@link JrnlState} 맵
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
     * 캐시에 저장된 상태 맵(entry/diary/dream/intrpt)을 기준으로 조회된 {@link JrnlDayDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param entryMap entry postNo → {@link JrnlState} 맵
     * @param diaryMap diary postNo → {@link JrnlState} 맵
     * @param dreamMap dream postNo → {@link JrnlState} 맵
     * @param intrptMap intrpt postNo → {@link JrnlState} 맵
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

            if (searchParam.isShowDiaries()) {
                JrnlDreamViewHelper.applyStates(day.getJrnlDreamList(), dreamMap, intrptMap);
            }
        }
    }

    /**
     * Entry가 collapsed 상태일 경우, 하위 {@link JrnlDiaryDto} 들에 포함된 태그를 수집하여 중복 제거된 "요약 태그 목록"을 Entry에 주입한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
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

    /**
     * 주어진 {@link JrnlDayDto} 객체에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param jrnlDayList 공휴일 및 주말 정보를 설정할 대상 DTO
     * @param hldyMap 날짜(String: yyyy-MM-dd) → 공휴일 이름 목록 매핑 정보
     */
    public static void setHldyInfo(final List<JrnlDayDto> jrnlDayList, final Map<String, List<String>> hldyMap) throws Exception {
        if (CollectionUtils.isEmpty(jrnlDayList) || hldyMap == null) return;

        for (final JrnlDayDto jrnlDay : jrnlDayList) {
            setHldyInfo(jrnlDay, hldyMap);
        }
    }

    /**
     * 주어진 {@link JrnlDayDto} 객체에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param jrnlDay 공휴일 및 주말 정보를 설정할 대상 DTO
     * @param hldyMap 날짜(String: yyyy-MM-dd) → 공휴일 이름 목록 매핑 정보
     */
    public static void setHldyInfo(final JrnlDayDto jrnlDay, final Map<String, List<String>> hldyMap) throws Exception {
        if (jrnlDay == null || hldyMap == null) return;

        final String stdrdDt = jrnlDay.getStdrdDt();
        final boolean isHldy = hldyMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        jrnlDay.setIsHldy(isHldy || isWeekend);
        if (isHldy) {
            final String concatHldyNm = String.join(", ", hldyMap.get(stdrdDt));
            jrnlDay.setHldyNm(concatHldyNm);
        }
    }

    /**
     * 각 게시물 타입(일기/꿈/해석)의 상태를 별도의 Map(postNo → JrnlState)으로 구성해 반환한다.
     * @param myJrnlDayEntityList 조회된 JrnlDayEntity 전체 목록
     * @param searchParam 조회 조건(연도/월 등). 캐시 키 생성은 호출부에서 수행하며, 본 메서드는 단순히 상태맵 생성만 담당한다.
     * @return {@link JrnlStateMaps}
     *  entryMap: 항목(postNo → JrnlState)
     *  diaryMap: 일기(postNo → JrnlState)
     *  dreamMap: 꿈(postNo → JrnlState)
     *  intrptMap: 해석(postNo → JrnlState)
     */
    public static JrnlStateMaps makeJrnlStateMaps(final List<JrnlDayEntity> myJrnlDayEntityList, final JrnlDaySearchParam searchParam) {
        final Map<Integer, JrnlState> entryMap = new HashMap<>();
        final Map<Integer, JrnlState> diaryMap = new HashMap<>();
        final Map<Integer, JrnlState> dreamMap = new HashMap<>();
        final Map<Integer, JrnlState> intrptMap = new HashMap<>();

        for (final JrnlDayEntity day : myJrnlDayEntityList) {
            final List<JrnlEntryEntity> myJrnlEntryList = day.getJrnlEntryList();
            if (CollectionUtils.isNotEmpty(myJrnlEntryList)) {
                for (final JrnlEntryEntity entry : myJrnlEntryList) {
                    final JrnlState entryState = JrnlState.builder()
                            .collapsed(entry.state.hasState(StateCd.COLLAPSED))
                            .build();
                    entryMap.put(entry.getPostNo(), entryState);

                    final List<JrnlDiaryEntity> myJrnlDiaryList = entry.getJrnlDiaryList();
                    if (CollectionUtils.isNotEmpty(myJrnlDiaryList)) {
                        for (final JrnlDiaryEntity diary : myJrnlDiaryList) {
                            final JrnlState diaryState = JrnlState.builder()
                                    .resolved(diary.state.hasState(StateCd.RESOLVED))
                                    .collapsed(diary.state.hasState(StateCd.COLLAPSED))
                                    .imprtc(diary.state.hasState(StateCd.IMPRTC))
                                    .refrnc(diary.state.hasState(StateCd.REFRNC))
                                    .build();
                            diaryMap.put(diary.getPostNo(), diaryState);
                        }
                    }
                }
            }

            final List<JrnlDreamEntity> myJrnlDreamList = day.getJrnlDreamList();
            if (CollectionUtils.isNotEmpty(myJrnlDreamList)) {
                for (final JrnlDreamEntity dream : myJrnlDreamList) {
                    final JrnlState dreamState = JrnlState.builder()
                            .resolved(dream.state.hasState(StateCd.RESOLVED))
                            .collapsed(dream.state.hasState(StateCd.COLLAPSED))
                            .imprtc(dream.state.hasState(StateCd.IMPRTC))
                            .refrnc(dream.state.hasState(StateCd.REFRNC))
                            .build();
                    dreamMap.put(dream.getPostNo(), dreamState);

                    final List<JrnlIntrptEntity> myJrnlIntrptList = dream.getJrnlIntrptList();
                    if (CollectionUtils.isNotEmpty(myJrnlIntrptList)) {
                        for (final JrnlIntrptEntity intrpt : myJrnlIntrptList) {
                            final JrnlState intrptState = JrnlState.builder()
                                    .resolved(intrpt.state.hasState(StateCd.RESOLVED))
                                    .collapsed(intrpt.state.hasState(StateCd.COLLAPSED))
                                    .build();
                            intrptMap.put(intrpt.getPostNo(), intrptState);
                        }
                    }
                }
            }
        }
        return JrnlStateMaps.builder().entryMap(entryMap).diaryMap(diaryMap).dreamMap(dreamMap).intrptMap(intrptMap).build();
    }
}
