package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDtoTestFactory;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
@WithMockUser(username = TestConstant.TEST_AUDITOR)
class JournalEntryDiaryTypedServiceTest {

    @Resource
    private JournalEntryService journalEntryService;
    @Resource
    private JournalChapterRepository journalChapterRepository;
    @Resource
    private JournalDayRepository journalDayRepository;
    @PersistenceContext
    private EntityManager entityManager;

    private JournalEntryPostDto journalDiary;
    private Integer journalChapterId;

    @BeforeEach
    void setUp() throws Exception {
        final JournalDayEntity journalDay = JournalDayEntityTestFactory.createWithJournalDt("2000-01-01");
        journalDay.setYy(2000);
        journalDay.setMnth(1);
        final Integer journalDayId = journalDayRepository.saveAndFlush(journalDay).getId();
        journalChapterId = journalChapterRepository.saveAndFlush(JournalChapterEntity.builder()
                .chapterType(ChapterType.DIARY)
                .title("Diary chapter")
                .journalDayId(journalDayId)
                .build()).getId();
        entityManager.clear();
        journalDiary = JournalEntryDtoTestFactory.createDiaryPost();
        journalDiary.setJournalChapterId(journalChapterId);
    }

    @Test
    void regist() throws Exception {
        final ServiceResponse registResult = journalEntryService.regist(ContentType.JOURNAL_DIARY, journalDiary, null);
        final JournalEntryDto registered = (JournalEntryDto) registResult.getRsltObj();

        assertNotNull(registered.getId());
    }

    @Test
    void modify() throws Exception {
        final ServiceResponse registResult = journalEntryService.regist(ContentType.JOURNAL_DIARY, journalDiary, null);
        final JournalEntryDto registered = (JournalEntryDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        final JournalEntryPostDto toModify = JournalEntryDtoTestFactory.createDiaryPostWithKey(key);
        toModify.setJournalChapterId(journalChapterId);
        toModify.setContent("test");
        final ServiceResponse modifyResult = journalEntryService.modify(ContentType.JOURNAL_DIARY, toModify, null);
        final JournalEntryDto modified = (JournalEntryDto) modifyResult.getRsltObj();

        assertNotNull(modified.getId());
        assertEquals("test", modified.getContent());
    }

    @Test
    void delete() throws Exception {
        final ServiceResponse registResult = journalEntryService.regist(ContentType.JOURNAL_DIARY, journalDiary, null);
        final JournalEntryDto registered = (JournalEntryDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        final ServiceResponse deletetResult = journalEntryService.delete(key, ContentType.JOURNAL_DIARY);
        final Boolean isDeleted = deletetResult.getRslt();

        assertTrue(isDeleted);
        assertThrows(EntityNotFoundException.class,
                () -> journalEntryService.getDtlDto(key)
        );
    }
}
