package io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadPeriodSummaryProjection;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 기간별 스레드 집계 repository 계약 검증.
 * <p>
 * 주·월 범위, 사용자 소유권, 스레드별 기간 내 엔트리 수와 최초 등장일을 실제 조인으로 확인한다.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(DataSourceConfig.class)
@Import(TestAuditConfig.class)
class JournalThreadPeriodSummaryRepositoryTest {

    private static final String FIXTURE_USERNAME = "period_summary_user";
    private static final String OTHER_USERNAME = "period_summary_other";

    @Resource
    private JournalDayRepository journalDayRepository;
    @Resource
    private JournalChapterRepository journalChapterRepository;
    @Resource
    private JournalEntryRepository journalEntryRepository;
    @Resource
    private JournalThreadRepository journalThreadRepository;
    @Resource
    private JournalThreadEntryRepository journalThreadEntryRepository;

    /** 주간 집계는 같은 주의 엔트리만 세고 다른 사용자와 다른 주의 소속을 제외한다. */
    @Test
    void findPeriodSummaryByWeekStartDtAggregatesOnlyOwnedEntriesInWeek() {
        final LocalDate weekStartDt = LocalDate.of(2026, 7, 6);
        final JournalThreadEntity firstThread = saveThread("첫 번째 스레드", FIXTURE_USERNAME);
        final JournalThreadEntity secondThread = saveThread("두 번째 스레드", FIXTURE_USERNAME);
        final JournalThreadEntity otherThread = saveThread("다른 사용자 스레드", OTHER_USERNAME);

        final JournalEntryEntity firstEntry = saveEntry(LocalDate.of(2026, 7, 6), weekStartDt, FIXTURE_USERNAME);
        final JournalEntryEntity secondEntry = saveEntry(LocalDate.of(2026, 7, 8), weekStartDt, FIXTURE_USERNAME);
        final JournalEntryEntity outsideEntry = saveEntry(
                LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 13), FIXTURE_USERNAME);
        final JournalEntryEntity otherEntry = saveEntry(LocalDate.of(2026, 7, 7), weekStartDt, OTHER_USERNAME);

        saveMembership(firstThread.getId(), firstEntry.getId(), FIXTURE_USERNAME);
        saveMembership(firstThread.getId(), secondEntry.getId(), FIXTURE_USERNAME);
        saveMembership(firstThread.getId(), outsideEntry.getId(), FIXTURE_USERNAME);
        saveMembership(secondThread.getId(), secondEntry.getId(), FIXTURE_USERNAME);
        saveMembership(otherThread.getId(), otherEntry.getId(), OTHER_USERNAME);
        journalThreadEntryRepository.flush();

        final Map<Integer, JournalThreadPeriodSummaryProjection> result =
                journalThreadEntryRepository.findPeriodSummaryByWeekStartDt(FIXTURE_USERNAME, weekStartDt)
                        .stream()
                        .collect(Collectors.toMap(
                                JournalThreadPeriodSummaryProjection::getThreadId,
                                Function.identity()
                        ));

        assertEquals(2, result.size());
        assertEquals(2L, result.get(firstThread.getId()).getEntryCount().longValue());
        assertEquals(LocalDate.of(2026, 7, 6), result.get(firstThread.getId()).getFirstEntryDate());
        assertEquals(1L, result.get(secondThread.getId()).getEntryCount().longValue());
        assertEquals(LocalDate.of(2026, 7, 8), result.get(secondThread.getId()).getFirstEntryDate());
    }

    /** 월간 집계는 월 경계를 넘는 같은 스레드의 소속을 해당 월 건수에 포함하지 않는다. */
    @Test
    void findPeriodSummaryByMonthUsesRequestedMonthBoundary() {
        final JournalThreadEntity thread = saveThread("월간 경계 스레드", FIXTURE_USERNAME);
        final JournalEntryEntity julyEntry = saveEntry(
                LocalDate.of(2026, 7, 31), LocalDate.of(2026, 7, 27), FIXTURE_USERNAME);
        final JournalEntryEntity augustEntry = saveEntry(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 7, 27), FIXTURE_USERNAME);
        saveMembership(thread.getId(), julyEntry.getId(), FIXTURE_USERNAME);
        saveMembership(thread.getId(), augustEntry.getId(), FIXTURE_USERNAME);
        journalThreadEntryRepository.flush();

        final List<JournalThreadPeriodSummaryProjection> result =
                journalThreadEntryRepository.findPeriodSummaryByMonth(FIXTURE_USERNAME, 2026, 7);

        assertEquals(1, result.size());
        assertEquals(thread.getId(), result.get(0).getThreadId());
        assertEquals(1L, result.get(0).getEntryCount().longValue());
        assertEquals(LocalDate.of(2026, 7, 31), result.get(0).getFirstEntryDate());
    }

    /** 기간 집계 테스트용 가상 스레드를 저장한다. */
    private JournalThreadEntity saveThread(final String title, final String username) {
        return journalThreadRepository.saveAndFlush(JournalThreadEntity.builder()
                .contentType("JOURNAL_THREAD")
                .title(title)
                .createdBy(username)
                .build());
    }

    /** 지정 일자의 가상 일자·챕터·엔트리를 저장한다. */
    private JournalEntryEntity saveEntry(
            final LocalDate journalDate,
            final LocalDate weekStartDt,
            final String username
    ) {
        final JournalDayEntity day = journalDayRepository.saveAndFlush(JournalDayEntity.builder()
                .contentType("JOURNAL_DAY")
                .journalDate(journalDate)
                .weekStartDt(weekStartDt)
                .yy(journalDate.getYear())
                .mnth(journalDate.getMonthValue())
                .createdBy(username)
                .build());
        final JournalChapterEntity chapter = journalChapterRepository.saveAndFlush(JournalChapterEntity.builder()
                .contentType("JOURNAL_CHAPTER")
                .chapterType(ChapterType.DIARY)
                .journalDayId(day.getId())
                .createdBy(username)
                .build());
        return journalEntryRepository.saveAndFlush(JournalEntryEntity.builder()
                .contentType("JOURNAL_DIARY")
                .journalChapterId(chapter.getId())
                .createdBy(username)
                .build());
    }

    /** 가상 스레드 소속을 저장한다. */
    private void saveMembership(final Integer threadId, final Integer entryId, final String username) {
        journalThreadEntryRepository.save(JournalThreadEntryEntity.builder()
                .threadId(threadId)
                .entryId(entryId)
                .createdBy(username)
                .build());
    }
}
