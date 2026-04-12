package io.nicheblog.dreamdiary.feature.jrnl.day.service.helper;

import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlState;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlStateMaps;
import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.feature.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.feature.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.entity.JrnlChapterEntity;
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
     * @param jrnlDayEntityList 조회된 JrnlDayEntity 리스트
     * @return {@link JrnlStateMaps}
     *  chapterMap: entry postNo -> JrnlState
     *  diaryMap: diary postNo -> JrnlState
     *  dreamMap: dream postNo -> JrnlState
     *  intrptMap: intrpt postNo -> JrnlState
     */
    public static JrnlStateMaps makeJrnlStateMaps(final List<JrnlDayEntity> jrnlDayEntityList) {
        final Map<Integer, JrnlState> chapterMap = new HashMap<>();
        final Map<Integer, JrnlState> diaryMap = new HashMap<>();
        final Map<Integer, JrnlState> dreamMap = new HashMap<>();
        final Map<Integer, JrnlState> intrptMap = new HashMap<>();

        if (CollectionUtils.isEmpty(jrnlDayEntityList)) {
            return JrnlStateMaps.builder().chapterMap(chapterMap).diaryMap(diaryMap).dreamMap(dreamMap).intrptMap(intrptMap).build();
        }

        for (final JrnlDayEntity day : jrnlDayEntityList) {
            final List<JrnlChapterEntity> jrnlChapterList = day.getJrnlChapterList();
            if (CollectionUtils.isNotEmpty(jrnlChapterList)) {
                for (final JrnlChapterEntity entry : jrnlChapterList) {
                    final JrnlState entryState = JrnlState.builder()
                            .collapsed(entry.state.hasState(StateCd.COLLAPSED))
                            .build();
                    chapterMap.put(entry.getPostNo(), entryState);

                    final List<JrnlDiaryEntity> jrnlDiaryList = entry.getJrnlDiaryList();
                    if (CollectionUtils.isNotEmpty(jrnlDiaryList)) {
                        for (final JrnlDiaryEntity diary : jrnlDiaryList) {
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

            final List<JrnlDreamEntity> jrnlDreamList = day.getJrnlDreamList();
            if (CollectionUtils.isNotEmpty(jrnlDreamList)) {
                for (final JrnlDreamEntity dream : jrnlDreamList) {
                    final JrnlState dreamState = JrnlState.builder()
                            .resolved(dream.state.hasState(StateCd.RESOLVED))
                            .collapsed(dream.state.hasState(StateCd.COLLAPSED))
                            .imprtc(dream.state.hasState(StateCd.IMPRTC))
                            .refrnc(dream.state.hasState(StateCd.REFRNC))
                            .build();
                    dreamMap.put(dream.getPostNo(), dreamState);

                    final List<JrnlIntrptEntity> jrnlIntrptList = dream.getJrnlIntrptList();
                    if (CollectionUtils.isNotEmpty(jrnlIntrptList)) {
                        for (final JrnlIntrptEntity intrpt : jrnlIntrptList) {
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
        return JrnlStateMaps.builder().chapterMap(chapterMap).diaryMap(diaryMap).dreamMap(dreamMap).intrptMap(intrptMap).build();
    }
}
