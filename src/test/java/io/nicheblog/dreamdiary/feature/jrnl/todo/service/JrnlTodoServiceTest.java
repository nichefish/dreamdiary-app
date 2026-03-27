package io.nicheblog.dreamdiary.feature.jrnl.todo.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.todo.model.JrnlTodoDto;
import io.nicheblog.dreamdiary.feature.jrnl.todo.model.JrnlTodoDtoTestFactory;
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

/**
 * JrnlTodoServiceTest
 * <pre>
 *  Service-level CRUD tests for JrnlTodo.
 *  Tests run in transaction and roll back after each case.
 * </pre>
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class JrnlTodoServiceTest {

    @Resource
    private JrnlTodoService jrnlTodoService;

    @MockBean
    @SuppressWarnings("unused")
    private AuthUtils authUtils;

    private JrnlTodoDto jrnlTodo;

    /**
     * Initialize test data before each case.
     */
    @BeforeEach
    void setUp() throws Exception {
        jrnlTodo = JrnlTodoDtoTestFactory.create();

        try (final MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::isAuthenticated).thenReturn(true);
            mockedStatic.when(AuthUtils::getLgnUserId).thenReturn(TestConstant.TEST_AUDITOR);
        }
    }

    /**
     * Create todo.
     */
    @Test
    void regist() throws Exception {
        // Given::

        // When::
        final ServiceResponse registResult = jrnlTodoService.regist(jrnlTodo);
        final JrnlTodoDto registered = (JrnlTodoDto) registResult.getRsltObj();

        // Then::
        assertNotNull(registered, "Register result is null.");
        assertNotNull(registered.getPostNo(), "Register did not assign key.");
        assertEquals(1, registered.getIdx(), "Unexpected index for first item.");
    }

    /**
     * Update todo.
     */
    @Test
    void modify() throws Exception {
        // Given::
        final ServiceResponse registResult = jrnlTodoService.regist(jrnlTodo);
        final JrnlTodoDto registered = (JrnlTodoDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final JrnlTodoDto toModify = JrnlTodoDtoTestFactory.createWithKey(key);
        toModify.setCn("test");
        final ServiceResponse modifyResult = jrnlTodoService.modify(toModify);
        final JrnlTodoDto modified = (JrnlTodoDto) modifyResult.getRsltObj();

        // Then::
        assertNotNull(modified.getPostNo(), "Modify did not keep key.");
        assertEquals("test", modified.getCn(), "Modify did not update content.");
    }

    /**
     * Delete todo.
     */
    @Test
    void delete() throws Exception {
        // Given::
        final ServiceResponse registResult = jrnlTodoService.regist(jrnlTodo);
        final JrnlTodoDto registered = (JrnlTodoDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final ServiceResponse deleteResult = jrnlTodoService.delete(key);
        final Boolean isDeleted = deleteResult.getRslt();

        // Then::
        assertTrue(isDeleted, "Delete result was false.");
        assertThrows(EntityNotFoundException.class,
                () -> jrnlTodoService.getDtlDto(key),
                "Deleted entity was still retrievable.");
    }
}
