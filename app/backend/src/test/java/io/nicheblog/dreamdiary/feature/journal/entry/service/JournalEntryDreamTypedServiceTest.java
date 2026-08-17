package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.JournalTestUserSupport;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDtoTestFactory;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
class JournalEntryDreamTypedServiceTest {

    @Resource
    private JournalEntryService journalEntryService;
    @Resource
    private JournalChapterRepository journalChapterRepository;
    @Resource
    private JournalDayRepository journalDayRepository;
    @Resource
    private UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    private JournalEntryPostDto journalDream;
    private Integer journalChapterId;

    @BeforeEach
    void setUp() throws Exception {
        final Integer ownerId = JournalTestUserSupport.ensureUser(userRepository, TestConstant.TEST_AUDITOR);
        JournalTestUserSupport.authenticate(ownerId, TestConstant.TEST_AUDITOR);
        final JournalDayEntity journalDay = JournalDayEntityTestFactory.createWithJournalDt("2000-01-01", ownerId);
        journalDay.setYy(2000);
        journalDay.setMnth(1);
        final Integer journalDayId = journalDayRepository.saveAndFlush(journalDay).getId();
        journalChapterId = journalChapterRepository.saveAndFlush(JournalChapterEntity.builder()
                .chapterType(ChapterType.DREAM)
                .title("Dream chapter")
                .journalDayId(journalDayId)
                .build()).getId();
        entityManager.clear();
        journalDream = JournalEntryDtoTestFactory.createDreamPost();
        journalDream.setJournalChapterId(journalChapterId);
    }

    @Test
    void regist() throws Exception {
        final ServiceResponse registResult = journalEntryService.regist(ContentType.JOURNAL_DREAM, journalDream, null);
        final JournalEntryDto result = (JournalEntryDto) registResult.getRsltObj();

        assertNotNull(result.getId());
    }

    @Test
    void modify() throws Exception {
        final ServiceResponse registResult = journalEntryService.regist(ContentType.JOURNAL_DREAM, journalDream, null);
        final JournalEntryDto registered = (JournalEntryDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        final JournalEntryPostDto toModify = JournalEntryDtoTestFactory.createDreamPostWithKey(key);
        toModify.setJournalChapterId(journalChapterId);
        toModify.setContent("test");
        final ServiceResponse modifyResult = journalEntryService.modify(ContentType.JOURNAL_DREAM, toModify, null);
        final JournalEntryDto modified = (JournalEntryDto) modifyResult.getRsltObj();

        assertNotNull(modified.getId());
        assertEquals("test", modified.getContent());
    }

    @Test
    void delete() throws Exception {
        final ServiceResponse registResult = journalEntryService.regist(ContentType.JOURNAL_DREAM, journalDream, null);
        final JournalEntryDto registered = (JournalEntryDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        final ServiceResponse deleteResult = journalEntryService.delete(key, ContentType.JOURNAL_DREAM);
        final Boolean isDeleted = deleteResult.getRslt();

        assertTrue(isDeleted);
        assertThrows(EntityNotFoundException.class,
                () -> journalEntryService.getDtlDto(key)
        );
    }
}
