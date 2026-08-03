package io.nicheblog.dreamdiary.feature.journal.todo.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.journal.todo.model.JournalTodoDto;
import io.nicheblog.dreamdiary.feature.journal.todo.model.JournalTodoDtoTestFactory;
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
import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JournalTodoServiceTest
 * <pre>
 *  Service-level CRUD tests for JournalTodo.
 *  Tests run in transaction and roll back after each case.
 * </pre>
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
@WithMockUser(username = TestConstant.TEST_AUDITOR)
class JournalTodoServiceTest {

    @Resource
    private JournalTodoService journalTodoService;

    private JournalTodoDto journalTodo;

    /**
     * Initialize test data before each case.
     */
    @BeforeEach
    void setUp() throws Exception {
        journalTodo = JournalTodoDtoTestFactory.create();
    }

    /**
     * Create todo.
     */
    @Test
    void regist() throws Exception {
        // Given::

        // When::
        final ServiceResponse registResult = journalTodoService.regist(journalTodo);
        final JournalTodoDto registered = (JournalTodoDto) registResult.getRsltObj();

        // Then::
        assertNotNull(registered, "Register result is null.");
        assertNotNull(registered.getId(), "Register did not assign key.");
        assertEquals(1, registered.getSortOrder(), "Unexpected index for first item.");
    }

    /**
     * Update todo.
     */
    @Test
    void modify() throws Exception {
        // Given::
        final ServiceResponse registResult = journalTodoService.regist(journalTodo);
        final JournalTodoDto registered = (JournalTodoDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final JournalTodoDto toModify = JournalTodoDtoTestFactory.createWithKey(key);
        toModify.setContent("test");
        final ServiceResponse modifyResult = journalTodoService.modify(toModify);
        final JournalTodoDto modified = (JournalTodoDto) modifyResult.getRsltObj();

        // Then::
        assertNotNull(modified.getId(), "Modify did not keep key.");
        assertEquals("test", modified.getContent(), "Modify did not update content.");
    }

    /**
     * Delete todo.
     */
    @Test
    void delete() throws Exception {
        // Given::
        final ServiceResponse registResult = journalTodoService.regist(journalTodo);
        final JournalTodoDto registered = (JournalTodoDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final ServiceResponse deleteResult = journalTodoService.delete(key);
        final Boolean isDeleted = deleteResult.getRslt();

        // Then::
        assertTrue(isDeleted, "Delete result was false.");
        assertThrows(EntityNotFoundException.class,
                () -> journalTodoService.getDtlDto(key),
                "Deleted entity was still retrievable.");
    }
}
