package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateMaps;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
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

    private static JournalState dreamStateFromEntry(final JournalEntryEntity journalEntry) {
        return JournalState.builder()
                .collapsed(journalEntry.state.hasState(StateKey.COLLAPSED))
                .imprtc(journalEntry.state.hasState(StateKey.IMPRTC))
                .refrnc(journalEntry.state.hasState(StateKey.REFRNC))
                .nhtmr(journalEntry.state.hasState(StateKey.NHTMR))
                .halluc(journalEntry.state.hasState(StateKey.HALLUC))
                .build();
    }

    public static JournalStateMaps makeJournalStateMaps(final List<JournalDayEntity> journalDayEntityList) {
        final Map<Integer, JournalState> chapterMap = new HashMap<>();
        final Map<Integer, JournalState> diaryMap = new HashMap<>();
        final Map<Integer, JournalState> dreamMap = new HashMap<>();
        final Map<Integer, JournalState> interpretationMap = new HashMap<>();

        if (CollectionUtils.isEmpty(journalDayEntityList)) {
            return JournalStateMaps.builder()
                    .chapterMap(chapterMap)
                    .diaryMap(diaryMap)
                    .dreamMap(dreamMap)
                    .interpretationMap(interpretationMap)
                    .build();
        }

        for (final JournalDayEntity journalDayEntity : journalDayEntityList) {
            final List<JournalChapterEntity> journalChapterList = journalDayEntity.getJournalChapterList();
            if (CollectionUtils.isEmpty(journalChapterList)) continue;

            for (final JournalChapterEntity journalChapterEntity : journalChapterList) {
                if (journalChapterEntity == null) continue;

                chapterMap.put(
                        journalChapterEntity.getId(),
                        JournalState.builder()
                                .collapsed(journalChapterEntity.state.hasState(StateKey.COLLAPSED))
                                .build()
                );

                if (CollectionUtils.isEmpty(journalChapterEntity.getJournalEntryList())) continue;

                for (final JournalEntryEntity journalEntry : journalChapterEntity.getJournalEntryList()) {
                    if (journalEntry == null || journalEntry.getId() == null) continue;

                    final ContentType contentType = ContentType.get(journalEntry.getContentType());
                    if (contentType == ContentType.JOURNAL_DIARY) {
                        diaryMap.put(
                                journalEntry.getId(),
                                JournalState.builder()
                                        .collapsed(journalEntry.state.hasState(StateKey.COLLAPSED))
                                        .imprtc(journalEntry.state.hasState(StateKey.IMPRTC))
                                        .refrnc(journalEntry.state.hasState(StateKey.REFRNC))
                                        .build()
                        );
                        continue;
                    }

                    if (contentType == ContentType.JOURNAL_DREAM) {
                        dreamMap.put(journalEntry.getId(), dreamStateFromEntry(journalEntry));
                    }
                }
            }
        }

        return JournalStateMaps.builder()
                .chapterMap(chapterMap)
                .diaryMap(diaryMap)
                .dreamMap(dreamMap)
                .interpretationMap(interpretationMap)
                .build();
    }
}
