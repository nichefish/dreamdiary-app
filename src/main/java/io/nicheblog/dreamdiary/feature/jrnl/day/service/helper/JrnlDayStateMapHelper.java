package io.nicheblog.dreamdiary.feature.jrnl.day.service.helper;

import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlState;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlStateMaps;
import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.feature.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.feature.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.feature.jrnl.entry.entity.JrnlEntryEntity;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.entity.JrnlIntrptEntity;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JrnlDayStateMapHelper
 *
 * @author nichefish
 */
@UtilityClass
public final class JrnlDayStateMapHelper {

    /**
     * JrnlDayEntity 리스트를 순회하여 각 postNo 기준의 JrnlState 맵(entry, diary, dream, intrpt)을 생성한다.
     * @param myJrnlDayEntityList 조회된 JrnlDayEntity 리스트
     * @return {@link JrnlStateMaps}
     *  entryMap: entry postNo -> JrnlState
     *  diaryMap: diary postNo -> JrnlState
     *  dreamMap: dream postNo -> JrnlState
     *  intrptMap: intrpt postNo -> JrnlState
     */
    public static JrnlStateMaps makeJrnlStateMaps(final List<JrnlDayEntity> myJrnlDayEntityList) {
        final Map<Integer, JrnlState> entryMap = new HashMap<>();
        final Map<Integer, JrnlState> diaryMap = new HashMap<>();
        final Map<Integer, JrnlState> dreamMap = new HashMap<>();
        final Map<Integer, JrnlState> intrptMap = new HashMap<>();

        if (CollectionUtils.isEmpty(myJrnlDayEntityList)) {
            return JrnlStateMaps.builder().entryMap(entryMap).diaryMap(diaryMap).dreamMap(dreamMap).intrptMap(intrptMap).build();
        }

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
