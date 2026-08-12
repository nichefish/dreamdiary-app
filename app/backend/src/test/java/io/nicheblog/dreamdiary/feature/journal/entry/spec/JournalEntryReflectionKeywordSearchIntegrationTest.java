package io.nicheblog.dreamdiary.feature.journal.entry.spec;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import io.nicheblog.dreamdiary.global.TestConstant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * "원문·해석 한 몸" 검색 계약을 검증한다.
 * <p>
 * Reflection 은 별도 Aggregate(journal_reflection)이고 대상 필수(About-A)라 검색 결과 행이 되지 않는다.
 * 다만 대상 Primary(일기·꿈) 검색 시, 그 대상을 가리키는 Reflection 본문에 키워드가 있으면 대상 Primary 가
 * 결과에 포함된다({@link JournalEntrySpec#targetReflectionKeywordSubquery} 가 journal_reflection 을 EXISTS 로 조회).
 * 태그·state 는 대상을 매칭시키지 않으며 검색 스코프는 요청 타입 단독이다.
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class JournalEntryReflectionKeywordSearchIntegrationTest {

    /** 가상 픽스처 — Reflection 본문에만 등장하는 키워드(대상 일기 본문에는 없음). */
    private static final String FIXTURE_REFLECTION_KEYWORD = "물놀이해석";
    /** 가상 픽스처 — 어느 본문에도 없는 키워드. */
    private static final String FIXTURE_ABSENT_KEYWORD = "존재하지않는키워드";

    @Resource
    private JournalDayRepository journalDayRepository;
    @Resource
    private JournalChapterRepository journalChapterRepository;
    @Resource
    private JournalEntryRepository journalEntryRepository;
    @Resource
    private JournalReflectionRepository journalReflectionRepository;
    @PersistenceContext
    private EntityManager entityManager;

    private final JournalEntrySpec spec = new JournalEntrySpec();

    /** 대상 일기 본문에 없고 딸린 Reflection 본문에만 있는 키워드로도 대상 일기가 검색된다. */
    @Test
    void diaryFoundByKeywordInAttachedReflection() throws Exception {
        final Integer dayId = saveDay();
        final Integer chapterId = saveDiaryChapter(dayId);
        final Integer diaryId = saveDiary(chapterId, "평범한 하루 기록");
        saveReflection(diaryId, ContentType.JOURNAL_DIARY, "이 기록에 대한 " + FIXTURE_REFLECTION_KEYWORD + " 사유");
        entityManager.flush();
        entityManager.clear();

        final List<JournalEntryEntity> found = journalEntryRepository.findAll(spec.searchWith(Map.of(
                "contentType", ContentType.JOURNAL_DIARY,
                "searchKeywords", List.of(FIXTURE_REFLECTION_KEYWORD),
                "createdBy", TestConstant.TEST_AUDITOR
        )));

        assertEquals(1, found.size());
        assertEquals(diaryId, found.get(0).getId());
    }

    /** 어느 본문에도 없는 키워드는 대상 일기를 반환하지 않는다. */
    @Test
    void diaryNotFoundWhenKeywordAbsentEverywhere() throws Exception {
        final Integer dayId = saveDay();
        final Integer chapterId = saveDiaryChapter(dayId);
        final Integer diaryId = saveDiary(chapterId, "평범한 하루 기록");
        saveReflection(diaryId, ContentType.JOURNAL_DIARY, "이 기록에 대한 " + FIXTURE_REFLECTION_KEYWORD + " 사유");
        entityManager.flush();
        entityManager.clear();

        final List<JournalEntryEntity> found = journalEntryRepository.findAll(spec.searchWith(Map.of(
                "contentType", ContentType.JOURNAL_DIARY,
                "searchKeywords", List.of(FIXTURE_ABSENT_KEYWORD),
                "createdBy", TestConstant.TEST_AUDITOR
        )));

        assertTrue(found.isEmpty());
    }

    private Integer saveDay() {
        return journalDayRepository.saveAndFlush(JournalDayEntity.builder()
                .journalDate(LocalDate.of(2026, 8, 4)).yy(2026).mnth(8).build()).getId();
    }

    private Integer saveDiaryChapter(final Integer dayId) {
        return journalChapterRepository.saveAndFlush(JournalChapterEntity.builder()
                .chapterType(ChapterType.DIARY).journalDayId(dayId).summaryYn("N").sortOrder(1).build()).getId();
    }

    private Integer saveDiary(final Integer chapterId, final String content) {
        return journalEntryRepository.saveAndFlush(JournalEntryEntity.builder()
                .contentType(ContentType.JOURNAL_DIARY.key)
                .journalChapterId(chapterId)
                .content(content)
                .sortOrder(1)
                .build()).getId();
    }

    private void saveReflection(final Integer refId, final ContentType refContentType, final String content) {
        journalReflectionRepository.saveAndFlush(JournalReflectionEntity.builder()
                .contentType(ContentType.JOURNAL_REFLECTION.key)
                .refId(refId)
                .refContentType(refContentType)
                .content(content)
                .build());
    }
}
