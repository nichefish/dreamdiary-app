package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateMaps;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntity;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.feature.journal.note.entity.JournalNoteEntity;
import io.nicheblog.dreamdiary.feature.journal.interpretation.entity.JournalInterpretationEntity;
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

    private static JournalState dreamStateFromEntity(final JournalDreamEntity dream) {
        final boolean nhtmr = dream.state.hasState(StateKey.NHTMR) || "Y".equals(dream.getNhtmrYn());
        final boolean halluc = dream.state.hasState(StateKey.HALLUC) || "Y".equals(dream.getHallucYn());
        return JournalState.builder()
                .resolved(dream.state.hasState(StateKey.RESOLVED))
                .collapsed(dream.state.hasState(StateKey.COLLAPSED))
                .imprtc(dream.state.hasState(StateKey.IMPRTC))
                .refrnc(dream.state.hasState(StateKey.REFRNC))
                .nhtmr(nhtmr)
                .halluc(halluc)
                .build();
    }

    /**
     * JournalDayEntity 리스트를 순회하여 각 id 기준의 JournalState 맵(entry, diary, note, dream, interpretation)을 생성한다.
     * @param journalDayEntityList 조회된 JournalDayEntity 리스트
     * @return {@link JournalStateMaps}
     *  chapterMap: entry id -> JournalState
     *  diaryMap: diary id -> JournalState
     *  noteMap: note id -> JournalState
     *  dreamMap: dream id -> JournalState
     *  interpretationMap: interpretation id -> JournalState
     */
    public static JournalStateMaps makeJournalStateMaps(final List<JournalDayEntity> journalDayEntityList) {
        final Map<Integer, JournalState> chapterMap = new HashMap<>();
        final Map<Integer, JournalState> diaryMap = new HashMap<>();
        final Map<Integer, JournalState> noteMap = new HashMap<>();
        final Map<Integer, JournalState> dreamMap = new HashMap<>();
        final Map<Integer, JournalState> interpretationMap = new HashMap<>();

        if (CollectionUtils.isEmpty(journalDayEntityList)) {
            return JournalStateMaps.builder()
                    .chapterMap(chapterMap)
                    .diaryMap(diaryMap)
                    .noteMap(noteMap)
                    .dreamMap(dreamMap)
                    .interpretationMap(interpretationMap)
                    .build();
        }

        for (final JournalDayEntity day : journalDayEntityList) {
            final List<JournalChapterEntity> journalChapterList = day.getJournalChapterList();
            if (CollectionUtils.isNotEmpty(journalChapterList)) {
                for (final JournalChapterEntity entry : journalChapterList) {
                    final JournalState entryState = JournalState.builder()
                            .collapsed(entry.state.hasState(StateKey.COLLAPSED))
                            .build();
                    chapterMap.put(entry.getId(), entryState);

                    if (ChapterType.DREAM == entry.getChapterType()) {
                        // DREAM 챕터: 꿈 및 해몽 상태 처리
                        final List<JournalDreamEntity> dreamList = entry.getJournalDreamList();
                        if (CollectionUtils.isNotEmpty(dreamList)) {
                            for (final JournalDreamEntity dream : dreamList) {
                                final JournalState dreamState = dreamStateFromEntity(dream);
                                dreamMap.put(dream.getId(), dreamState);

                                final List<JournalInterpretationEntity> journalInterpretationList = dream.getJournalInterpretationList();
                                if (CollectionUtils.isNotEmpty(journalInterpretationList)) {
                                    for (final JournalInterpretationEntity interpretation : journalInterpretationList) {
                                        final JournalState interpretationState = JournalState.builder()
                                                .resolved(interpretation.state.hasState(StateKey.RESOLVED))
                                                .collapsed(interpretation.state.hasState(StateKey.COLLAPSED))
                                                .build();
                                        interpretationMap.put(interpretation.getId(), interpretationState);
                                    }
                                }
                            }
                        }
                        final List<JournalDreamEntity> elseDreamList = entry.getJournalElseDreamList();
                        if (CollectionUtils.isNotEmpty(elseDreamList)) {
                            for (final JournalDreamEntity dream : elseDreamList) {
                                final JournalState dreamState = dreamStateFromEntity(dream);
                                dreamMap.put(dream.getId(), dreamState);
                            }
                        }
                    } else if (ChapterType.DIARY == entry.getChapterType()) {
                        // DIARY 챕터: 일기 상태 처리
                        final List<JournalDiaryEntity> journalDiaryList = entry.getJournalDiaryList();
                        if (CollectionUtils.isNotEmpty(journalDiaryList)) {
                            for (final JournalDiaryEntity diary : journalDiaryList) {
                                final JournalState diaryState = JournalState.builder()
                                        .resolved(diary.state.hasState(StateKey.RESOLVED))
                                        .collapsed(diary.state.hasState(StateKey.COLLAPSED))
                                        .imprtc(diary.state.hasState(StateKey.IMPRTC))
                                        .refrnc(diary.state.hasState(StateKey.REFRNC))
                                        .build();
                                diaryMap.put(diary.getId(), diaryState);

                                final List<JournalInterpretationEntity> journalInterpretationList = diary.getJournalInterpretationList();
                                if (CollectionUtils.isNotEmpty(journalInterpretationList)) {
                                    for (final JournalInterpretationEntity interpretation : journalInterpretationList) {
                                        final JournalState interpretationState = JournalState.builder()
                                                .resolved(interpretation.state.hasState(StateKey.RESOLVED))
                                                .collapsed(interpretation.state.hasState(StateKey.COLLAPSED))
                                                .build();
                                        interpretationMap.put(interpretation.getId(), interpretationState);
                                    }
                                }
                            }
                        }
                        final List<JournalNoteEntity> journalNoteList = entry.getJournalNoteList();
                        if (CollectionUtils.isNotEmpty(journalNoteList)) {
                            for (final JournalNoteEntity note : journalNoteList) {
                                final JournalState noteState = JournalState.builder()
                                        .resolved(note.state.hasState(StateKey.RESOLVED))
                                        .collapsed(note.state.hasState(StateKey.COLLAPSED))
                                        .imprtc(note.state.hasState(StateKey.IMPRTC))
                                        .refrnc(note.state.hasState(StateKey.REFRNC))
                                        .build();
                                noteMap.put(note.getId(), noteState);
                            }
                        }
                    }
                }
            }
        }
        return JournalStateMaps.builder()
                .chapterMap(chapterMap)
                .diaryMap(diaryMap)
                .noteMap(noteMap)
                .dreamMap(dreamMap)
                .interpretationMap(interpretationMap)
                .build();
    }
}
