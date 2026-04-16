package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.state.StateCd;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateMaps;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntity;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.intrpt.entity.JournalIntrptEntity;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JournalDayStateMapHelper
 *
 * @author nichefish
 */
@UtilityClass
public final class JournalDayStateMapHelper {

    /**
     * JournalDayEntity 리스트를 순회하여 각 id 기준의 JournalState 맵(entry, diary, dream, intrpt)을 생성한다.
     * @param journalDayEntityList 조회된 JournalDayEntity 리스트
     * @return {@link JournalStateMaps}
     *  chapterMap: entry id -> JournalState
     *  diaryMap: diary id -> JournalState
     *  dreamMap: dream id -> JournalState
     *  intrptMap: intrpt id -> JournalState
     */
    public static JournalStateMaps makeJournalStateMaps(final List<JournalDayEntity> journalDayEntityList) {
        final Map<Integer, JournalState> chapterMap = new HashMap<>();
        final Map<Integer, JournalState> diaryMap = new HashMap<>();
        final Map<Integer, JournalState> dreamMap = new HashMap<>();
        final Map<Integer, JournalState> intrptMap = new HashMap<>();

        if (CollectionUtils.isEmpty(journalDayEntityList)) {
            return JournalStateMaps.builder().chapterMap(chapterMap).diaryMap(diaryMap).dreamMap(dreamMap).intrptMap(intrptMap).build();
        }

        for (final JournalDayEntity day : journalDayEntityList) {
            final List<JournalChapterEntity> journalChapterList = day.getJournalChapterList();
            if (CollectionUtils.isNotEmpty(journalChapterList)) {
                for (final JournalChapterEntity entry : journalChapterList) {
                    final JournalState entryState = JournalState.builder()
                            .collapsed(entry.state.hasState(StateCd.COLLAPSED))
                            .build();
                    chapterMap.put(entry.getId(), entryState);

                    final List<JournalDiaryEntity> journalDiaryList = entry.getJournalDiaryList();
                    if (CollectionUtils.isNotEmpty(journalDiaryList)) {
                        for (final JournalDiaryEntity diary : journalDiaryList) {
                            final JournalState diaryState = JournalState.builder()
                                    .resolved(diary.state.hasState(StateCd.RESOLVED))
                                    .collapsed(diary.state.hasState(StateCd.COLLAPSED))
                                    .imprtc(diary.state.hasState(StateCd.IMPRTC))
                                    .refrnc(diary.state.hasState(StateCd.REFRNC))
                                    .build();
                            diaryMap.put(diary.getId(), diaryState);
                        }
                    }
                }
            }

            final List<JournalDreamEntity> journalDreamList = day.getJournalDreamList();
            if (CollectionUtils.isNotEmpty(journalDreamList)) {
                for (final JournalDreamEntity dream : journalDreamList) {
                    final JournalState dreamState = JournalState.builder()
                            .resolved(dream.state.hasState(StateCd.RESOLVED))
                            .collapsed(dream.state.hasState(StateCd.COLLAPSED))
                            .imprtc(dream.state.hasState(StateCd.IMPRTC))
                            .refrnc(dream.state.hasState(StateCd.REFRNC))
                            .build();
                    dreamMap.put(dream.getId(), dreamState);

                    final List<JournalIntrptEntity> journalIntrptList = dream.getJournalIntrptList();
                    if (CollectionUtils.isNotEmpty(journalIntrptList)) {
                        for (final JournalIntrptEntity intrpt : journalIntrptList) {
                            final JournalState intrptState = JournalState.builder()
                                    .resolved(intrpt.state.hasState(StateCd.RESOLVED))
                                    .collapsed(intrpt.state.hasState(StateCd.COLLAPSED))
                                    .build();
                            intrptMap.put(intrpt.getId(), intrptState);
                        }
                    }
                }
            }
        }
        return JournalStateMaps.builder().chapterMap(chapterMap).diaryMap(diaryMap).dreamMap(dreamMap).intrptMap(intrptMap).build();
    }
}

