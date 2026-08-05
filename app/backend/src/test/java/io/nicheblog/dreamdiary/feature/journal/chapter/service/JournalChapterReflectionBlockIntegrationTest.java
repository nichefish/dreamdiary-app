package io.nicheblog.dreamdiary.feature.journal.chapter.service;

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
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 챕터 삭제 Block(Reference→Block) 계약을 검증한다.
 * <p>
 * 챕터 내 엔트리에 참조 Reflection 이 있으면 챕터 삭제를 거부한다. Hibernate cascade 로
 * 엔트리만 soft-delete 되어 Reflection 이 dangling 되는 경로를 막는다.
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
@WithMockUser(username = TestConstant.TEST_AUDITOR)
class JournalChapterReflectionBlockIntegrationTest {

    private static final String FIXTURE_DIARY_CONTENT = "평범한 하루 기록";
    private static final String FIXTURE_REFLECTION_CONTENT = "이 기록에 대한 사유 한 줄";

    @Resource
    private JournalChapterService journalChapterService;
    @Resource
    private JournalChapterRepository journalChapterRepository;
    @Resource
    private JournalEntryRepository journalEntryRepository;
    @Resource
    private JournalReflectionRepository journalReflectionRepository;
    @Resource
    private JournalDayRepository journalDayRepository;
    @PersistenceContext
    private EntityManager entityManager;

    /** 챕터 내 일기에 Reflection 이 있으면 챕터 삭제는 Block 된다. */
    @Test
    void deleteChapterWithAttachedReflectionIsBlocked() {
        final ChapterFixture fixture = saveDiaryChapterWithEntry();
        journalReflectionRepository.saveAndFlush(JournalReflectionEntity.builder()
                .refId(fixture.diaryId)
                .refContentType(ContentType.JOURNAL_DIARY)
                .content(FIXTURE_REFLECTION_CONTENT)
                .build());
        entityManager.clear();

        final BusinessException ex = assertThrows(
                BusinessException.class,
                () -> journalChapterService.delete(fixture.chapterId)
        );
        assertEquals("journal.chapter.delete.blocked-by-reflection", ex.getMessage());
        assertTrue(journalChapterRepository.findById(fixture.chapterId).isPresent());
        assertTrue(journalEntryRepository.findById(fixture.diaryId).isPresent());
    }

    /** Reflection 이 없으면 챕터 삭제가 성공한다. */
    @Test
    void deleteChapterWithoutReflectionSucceeds() throws Exception {
        final ChapterFixture fixture = saveDiaryChapterWithEntry();
        entityManager.clear();

        final ServiceResponse response = journalChapterService.delete(fixture.chapterId);
        assertTrue(Boolean.TRUE.equals(response.getRslt()));
        assertTrue(journalChapterRepository.findById(fixture.chapterId).isEmpty());
    }

    private ChapterFixture saveDiaryChapterWithEntry() {
        final Integer dayId = journalDayRepository.saveAndFlush(JournalDayEntity.builder()
                .journalDate(LocalDate.of(2026, 8, 5)).yy(2026).mnth(8).build()).getId();
        final Integer chapterId = journalChapterRepository.saveAndFlush(JournalChapterEntity.builder()
                .chapterType(ChapterType.DIARY).journalDayId(dayId).summaryYn("N").sortOrder(1).build()).getId();
        final Integer diaryId = journalEntryRepository.saveAndFlush(JournalEntryEntity.builder()
                .contentType(ContentType.JOURNAL_DIARY.key)
                .journalChapterId(chapterId)
                .content(FIXTURE_DIARY_CONTENT)
                .sortOrder(1)
                .build()).getId();
        return new ChapterFixture(chapterId, diaryId);
    }

    private record ChapterFixture(Integer chapterId, Integer diaryId) {}
}