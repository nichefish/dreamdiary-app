package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDtoTestFactory;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class JournalEntryDiaryTypedServiceTest {

    @Resource
    private JournalEntryService journalEntryService;

    @MockBean
    @SuppressWarnings("unused")
    private AuthUtils authUtils;

    private JournalEntryPostDto journalDiary;

    @BeforeEach
    void setUp() throws Exception {
        journalDiary = JournalEntryDtoTestFactory.createDiaryPost();

        try (final MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::isAuthenticated).thenReturn(true);
            mockedStatic.when(AuthUtils::getLoginUsername).thenReturn(TestConstant.TEST_AUDITOR);
        }
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
