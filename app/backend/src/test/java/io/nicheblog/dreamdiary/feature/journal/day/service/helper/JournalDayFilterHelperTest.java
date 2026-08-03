package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryViewProjectionHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JournalDayFilterHelperTest {

    @Test
    void filterInMemory_doesNotReduceMonthListToAnchorDayWhenChapterPrefixFilterIsUsed() {
        final JournalDayDto firstDay = JournalDayDto.builder()
                .journalDate("2026-04-01")
                .journalChapterList(List.of(createChapter(11, "first diary")))
                .build();
        final JournalDayDto secondDay = JournalDayDto.builder()
                .journalDate("2026-04-02")
                .journalChapterList(List.of(createChapter(11, "second diary")))
                .build();

        final JournalDaySearchParam searchParam = JournalDaySearchParam.builder()
                .showDiaries(true)
                .showDreams(true)
                .stdrdDt("2026-04-01")
                .chapterPrefixIds(List.of(11))
                .build();

        final List<JournalDayDto> filtered = JournalDayFilterHelper.filterInMemory(List.of(firstDay, secondDay), searchParam);

        assertEquals(2, filtered.size());
        assertEquals(List.of("2026-04-01", "2026-04-02"), filtered.stream().map(JournalDayDto::getStdrdDt).toList());
    }

    private JournalChapterDto createChapter(final Integer prefixId, final String diaryContent) {
        final JournalChapterDto chapter = JournalChapterDto.builder()
                .prefixId(prefixId)
                .prefix(PrefixDto.builder().id(prefixId).name("회고").color("#287D94").build())
                .build();
        JournalEntryViewProjectionHelper.applyChapterEntries(chapter, List.of(
                JournalEntryDto.builder()
                        .contentType(ContentType.JOURNAL_DIARY.key)
                        .content(diaryContent)
                        .build()
        ));
        return chapter;
    }

    /**
     * Prefix 필터는 사용자 말머리가 다른 일반 챕터만 숨기고 시스템 요약·미선택 챕터는 유지한다.
     */
    @Test
    void filterInMemory_keepsSummaryAndUnprefixedChaptersAndReturnsHiddenPrefixHint() {
        final JournalChapterDto summary = JournalChapterDto.builder().summaryYn("Y").build();
        final JournalChapterDto unprefixed = JournalChapterDto.builder().summaryYn("N").build();
        final JournalChapterDto selected = JournalChapterDto.builder()
                .summaryYn("N")
                .prefixId(11)
                .prefix(PrefixDto.builder().id(11).name("회고").build())
                .build();
        final JournalChapterDto hidden = JournalChapterDto.builder()
                .summaryYn("N")
                .prefixId(12)
                .prefix(PrefixDto.builder().id(12).name("관계").color("#336699").build())
                .build();
        final JournalDayDto day = JournalDayDto.builder()
                .journalDate("2026-04-01")
                .journalChapterList(List.of(summary, unprefixed, selected, hidden))
                .build();
        final JournalDaySearchParam searchParam = JournalDaySearchParam.builder()
                .showDiaries(true)
                .showDreams(true)
                .chapterPrefixIds(List.of(11))
                .build();

        final JournalDayDto filtered = JournalDayFilterHelper.filterInMemory(List.of(day), searchParam).get(0);

        assertEquals(List.of(summary, unprefixed, selected), filtered.getJournalChapterList());
        assertEquals(1, filtered.getHiddenChapterPrefixList().size());
        assertEquals(12, filtered.getHiddenChapterPrefixList().get(0).getPrefixId());
        assertEquals("관계", filtered.getHiddenChapterPrefixList().get(0).getPrefixName());
        assertEquals("#336699", filtered.getHiddenChapterPrefixList().get(0).getPrefixColor());
    }

    @Test
    void filterInMemory_keepsCanonicalChapterEntriesInSyncWhenDiaryKeywordMatches() {
        final JournalChapterDto chapter = JournalChapterDto.builder()
                .prefixId(11)
                .prefix(PrefixDto.builder().id(11).name("회고").build())
                .build();
        JournalEntryViewProjectionHelper.applyChapterEntries(chapter, List.of(
                JournalEntryDto.builder().contentType(ContentType.JOURNAL_DIARY.key).content("matched diary").build(),
                JournalEntryDto.builder().contentType(ContentType.JOURNAL_DIARY.key).content("other diary").build(),
                JournalEntryDto.builder().contentType(ContentType.JOURNAL_DIARY.key).content("kept note").build()
        ));

        final JournalDayDto day = JournalDayDto.builder()
                .journalDate("2026-04-01")
                .journalChapterList(List.of(chapter))
                .build();

        final JournalDaySearchParam searchParam = JournalDaySearchParam.builder()
                .showDiaries(true)
                .showDreams(true)
                .diaryKeyword("matched")
                .build();

        final List<JournalDayDto> filtered = JournalDayFilterHelper.filterInMemory(List.of(day), searchParam);

        assertEquals(1, filtered.size());
        assertEquals(1, filtered.get(0).getJournalChapterList().get(0).getJournalEntryList().size());
        assertEquals("matched diary", filtered.get(0).getJournalChapterList().get(0).getJournalEntryList().get(0).getContent());
    }
}
