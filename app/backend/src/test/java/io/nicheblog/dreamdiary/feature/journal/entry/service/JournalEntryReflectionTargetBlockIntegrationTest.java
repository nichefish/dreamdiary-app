package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.JournalTestUserSupport;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
 * 대상 삭제 Block(Reference→Block) 계약을 검증한다.
 * <p>
 * 일기·꿈·노트에 참조 Reflection 이 있으면 대상 삭제를 거부한다. nullify·orphan 화는 하지 않는다.
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class JournalEntryReflectionTargetBlockIntegrationTest {

    /** 가상 픽스처 — 대상 일기 본문. */
    private static final String FIXTURE_DIARY_CONTENT = "평범한 하루 기록";
    /** 가상 픽스처 — Reflection 본문. */
    private static final String FIXTURE_REFLECTION_CONTENT = "이 기록에 대한 사유 한 줄";

    @Resource
    private JournalEntryService journalEntryService;
    @Resource
    private JournalEntryRepository journalEntryRepository;
    @Resource
    private JournalReflectionRepository journalReflectionRepository;
    @Resource
    private JournalDayRepository journalDayRepository;
    @Resource
    private JournalChapterRepository journalChapterRepository;
    @Resource
    private UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    private Integer ownerId;

    @BeforeEach
    void setUpOwner() throws Exception {
        ownerId = JournalTestUserSupport.ensureUser(userRepository, TestConstant.TEST_AUDITOR);
        JournalTestUserSupport.authenticate(ownerId, TestConstant.TEST_AUDITOR);
    }

    /** 참조 Reflection 이 있는 대상 일기 삭제는 Block 된다. */
    @Test
    void deleteTargetWithAttachedReflectionIsBlocked() {
        final Integer diaryId = saveDiary();
        journalReflectionRepository.saveAndFlush(JournalReflectionEntity.builder()
                .refId(diaryId)
                .refContentType(ContentType.JOURNAL_DIARY)
                .content(FIXTURE_REFLECTION_CONTENT)
                .build());
        entityManager.clear();

        final BusinessException ex = assertThrows(
                BusinessException.class,
                () -> journalEntryService.delete(diaryId, ContentType.JOURNAL_DIARY)
        );
        assertEquals("journal.entry.delete.blocked-by-reflection", ex.getMessage());

        assertTrue(journalEntryRepository.findById(diaryId).isPresent());
    }

    /** 참조 Reflection 이 없으면 대상 일기 삭제가 성공한다. */
    @Test
    void deleteTargetWithoutReflectionSucceeds() throws Exception {
        final Integer diaryId = saveDiary();
        entityManager.clear();

        final ServiceResponse response = journalEntryService.delete(diaryId, ContentType.JOURNAL_DIARY);
        assertTrue(Boolean.TRUE.equals(response.getRslt()));
        assertTrue(journalEntryRepository.findById(diaryId).isEmpty());
    }

    private Integer saveDiary() {
        final Integer dayId = journalDayRepository.saveAndFlush(JournalDayEntity.builder()
                .ownerId(ownerId).journalDate(LocalDate.of(2026, 8, 5)).yy(2026).mnth(8).build()).getId();
        final Integer chapterId = journalChapterRepository.saveAndFlush(JournalChapterEntity.builder()
                .chapterType(ChapterType.DIARY).journalDayId(dayId).summaryYn("N").sortOrder(1).build()).getId();
        return journalEntryRepository.saveAndFlush(JournalEntryEntity.builder()
                .contentType(ContentType.JOURNAL_DIARY.key)
                .journalChapterId(chapterId)
                .content(FIXTURE_DIARY_CONTENT)
                .sortOrder(1)
                .build()).getId();
    }
}
