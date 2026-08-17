package io.nicheblog.dreamdiary.feature.journal.reflection.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.JournalTestUserSupport;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.model.JournalReflectionPostDto;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reflection 쓰기 경로(R4a) 계약을 검증한다.
 * <p>
 * Reflection 등록은 journal_reflection 에 영속되며 대상 필수(About-A)를 검증한다.
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class JournalReflectionServiceIntegrationTest {

    /** 가상 픽스처 — Reflection 본문. */
    private static final String FIXTURE_CONTENT = "이 기록에 대한 사유 한 줄";
    /** 가상 픽스처 — 마이그레이션 감사 스탬프. */
    private static final String FIXTURE_MIGRATION_AUDIT = "MIGRATION_SPLIT";
    /** 가상 픽스처 — 다른 일자 소유자. */
    private static final String FIXTURE_OTHER_OWNER = "fx_other_owner";

    @Resource
    private JournalReflectionService journalReflectionService;
    @Resource
    private JournalReflectionRepository journalReflectionRepository;
    @Resource
    private JournalDayRepository journalDayRepository;
    @Resource
    private JournalChapterRepository journalChapterRepository;
    @Resource
    private JournalEntryRepository journalEntryRepository;
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

    /** 대상을 지정한 Reflection 등록은 journal_reflection 에 영속된다. */
    @Test
    void registPersistsReflectionToOwnTable() throws Exception {
        final Integer diaryId = saveDiaryTarget();

        final ServiceResponse response = journalReflectionService.regist(JournalReflectionPostDto.builder()
                .refId(diaryId)
                .refContentType(ContentType.JOURNAL_DIARY)
                .content(FIXTURE_CONTENT)
                .build());
        entityManager.flush();
        entityManager.clear();

        assertTrue(Boolean.TRUE.equals(response.getRslt()));
        final List<JournalReflectionEntity> reflections =
                journalReflectionRepository.findAllByRefIdInOrderByCreatedAtAsc(List.of(diaryId));
        assertEquals(1, reflections.size());
        assertEquals(ContentType.JOURNAL_DIARY, reflections.get(0).getRefContentType());
    }

    /** 대상 없는(About-A 위반) Reflection 등록은 거부된다. */
    @Test
    void registWithoutTargetIsRejected() {
        assertThrows(BusinessException.class, () -> journalReflectionService.regist(JournalReflectionPostDto.builder()
                .content(FIXTURE_CONTENT)
                .build()));
    }

    /** 하위 Reflection 이 가리키는 부모 Reflection 삭제는 Block 된다(R→R). */
    @Test
    void deleteReflectionWithChildReflectionIsBlocked() throws Exception {
        final Integer diaryId = saveDiaryTarget();
        final Integer parentId = journalReflectionRepository.saveAndFlush(JournalReflectionEntity.builder()
                .refId(diaryId)
                .refContentType(ContentType.JOURNAL_DIARY)
                .content(FIXTURE_CONTENT)
                .build()).getId();
        journalReflectionRepository.saveAndFlush(JournalReflectionEntity.builder()
                .refId(parentId)
                .refContentType(ContentType.JOURNAL_REFLECTION)
                .content("부모 사유에 대한 한 줄 더")
                .build());
        entityManager.clear();

        assertThrows(BusinessException.class, () -> journalReflectionService.delete(parentId));
        assertTrue(journalReflectionRepository.findById(parentId).isPresent());
    }

    /** 대상 일자 소유자면 감사 created_by 가 마이그레이션 스탬프여도 삭제된다. */
    @Test
    void deleteSucceedsWhenDayOwnerMatchesEvenIfCreatedByIsMigrationStamp() throws Exception {
        final Integer diaryId = saveDiaryTarget();
        final Integer reflectionId = journalReflectionRepository.saveAndFlush(JournalReflectionEntity.builder()
                .refId(diaryId)
                .refContentType(ContentType.JOURNAL_DIARY)
                .content(FIXTURE_CONTENT)
                .build()).getId();
        stampCreatedBy(reflectionId, FIXTURE_MIGRATION_AUDIT);

        final ServiceResponse response = journalReflectionService.delete(reflectionId);
        entityManager.flush();
        entityManager.clear();

        assertTrue(Boolean.TRUE.equals(response.getRslt()));
        assertTrue(journalReflectionRepository.findById(reflectionId).isEmpty());
    }

    /** 수정 로드 DTO 의 isOwnedBy 는 대상 일자 소유이며 createdBy 감사 스탬프는 유지된다. */
    @Test
    void getDtlDtoByUserReturnsDayOwnershipFlagWhenCreatedByIsMigrationStamp() throws Exception {
        final Integer diaryId = saveDiaryTarget();
        final Integer reflectionId = journalReflectionRepository.saveAndFlush(JournalReflectionEntity.builder()
                .refId(diaryId)
                .refContentType(ContentType.JOURNAL_DIARY)
                .content(FIXTURE_CONTENT)
                .build()).getId();
        stampCreatedBy(reflectionId, FIXTURE_MIGRATION_AUDIT);

        final JournalEntryDto dto = journalReflectionService.getDtlDtoByUser(reflectionId);

        assertTrue(Boolean.TRUE.equals(dto.getIsOwnedBy()));
        assertFalse(Boolean.TRUE.equals(dto.getIsCreatedBy()));
        assertEquals(FIXTURE_MIGRATION_AUDIT, dto.getCreatedBy());
    }

    /** 대상 일자가 다른 사용자 소유이면 해석 created_by 가 현재 사용자여도 삭제를 거부한다. */
    @Test
    void deleteIsRejectedWhenTargetDayBelongsToAnotherUser() throws Exception {
        final Integer otherOwnerId = JournalTestUserSupport.ensureUser(userRepository, FIXTURE_OTHER_OWNER);
        final Integer diaryId = saveDiaryTargetForOwner(otherOwnerId, LocalDate.of(2026, 8, 5));
        final Integer reflectionId = journalReflectionRepository.saveAndFlush(JournalReflectionEntity.builder()
                .refId(diaryId)
                .refContentType(ContentType.JOURNAL_DIARY)
                .content(FIXTURE_CONTENT)
                .build()).getId();
        entityManager.clear();

        assertThrows(NotAuthorizedException.class, () -> journalReflectionService.delete(reflectionId));
        assertTrue(journalReflectionRepository.findById(reflectionId).isPresent());
    }

    private Integer saveDiaryTarget() {
        return saveDiaryTargetForOwner(ownerId, LocalDate.of(2026, 8, 4));
    }

    private Integer saveDiaryTargetForOwner(final Integer dayOwnerId, final LocalDate journalDate) {
        final Integer dayId = journalDayRepository.saveAndFlush(JournalDayEntity.builder()
                .ownerId(dayOwnerId).journalDate(journalDate).yy(journalDate.getYear()).mnth(journalDate.getMonthValue()).build()).getId();
        final Integer chapterId = journalChapterRepository.saveAndFlush(JournalChapterEntity.builder()
                .chapterType(ChapterType.DIARY).journalDayId(dayId).summaryYn("N").sortOrder(1).build()).getId();
        return journalEntryRepository.saveAndFlush(JournalEntryEntity.builder()
                .contentType(ContentType.JOURNAL_DIARY.key)
                .journalChapterId(chapterId)
                .content("평범한 하루 기록")
                .sortOrder(1)
                .build()).getId();
    }

    private void stampCreatedBy(final Integer reflectionId, final String createdBy) {
        entityManager.createNativeQuery("UPDATE journal_reflection SET created_by = :createdBy WHERE id = :id")
                .setParameter("createdBy", createdBy)
                .setParameter("id", reflectionId)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }
}
