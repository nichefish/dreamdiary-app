package io.nicheblog.dreamdiary.feature.jrnl.todo.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.jrnl.todo.entity.JrnlTodoEntity;
import io.nicheblog.dreamdiary.feature.jrnl.todo.entity.JrnlTodoEntityTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JrnlTodoRepositoryTest
 * <pre>
 *  JPA repository tests for JrnlTodo.
 * </pre>
 *
 * @author nichefish
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(DataSourceConfig.class)
@Import(TestAuditConfig.class)
@Log4j2
class JrnlTodoRepositoryTest {

    @Resource
    private JrnlTodoRepository jrnlTodoRepository;

    private JrnlTodoEntity jrnlTodoEntity;

    /**
     * Initialize test data before each case.
     */
    @BeforeEach
    void setUp() throws Exception {
        jrnlTodoEntity = JrnlTodoEntityTestFactory.create();
    }

    /**
     * Persist entity.
     */
    @Test
    void testRegist() throws Exception {
        // Given::

        // When::
        final JrnlTodoEntity registered = jrnlTodoRepository.save(jrnlTodoEntity);
        final Integer key = registered.getId();
        final JrnlTodoEntity retrieved = jrnlTodoRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        // Then::
        assertNotNull(retrieved, "Could not load saved entity.");
        assertNotNull(retrieved.getId(), "Saved entity key is null.");
        assertNotNull(retrieved.getCreatedAt(), "createdAt audit was not set.");
        assertNotNull(retrieved.getCreatedBy(), "createdBy audit was not set.");
        assertEquals(TestConstant.TEST_AUDITOR, retrieved.getCreatedBy(), "Saved createdBy is unexpected.");
    }

    /**
     * Update entity.
     */
    @Test
    void testModify() throws Exception {
        // Given::
        final JrnlTodoEntity registered = jrnlTodoRepository.save(jrnlTodoEntity);
        final Integer key = registered.getId();

        // When::
        final JrnlTodoEntity toModify = jrnlTodoRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-modify")));
        toModify.setCn("modified");
        final JrnlTodoEntity modified = jrnlTodoRepository.save(toModify);

        // Then::
        assertNotNull(modified, "Could not load modified entity.");
        assertNotNull(modified.getId(), "Modified entity key is null.");
        assertNotNull(modified.getUpdatedAt(), "updatedAt audit was not set.");
        assertNotNull(modified.getUpdatedById(), "updatedBy audit was not set.");
        assertEquals(TestConstant.TEST_AUDITOR, modified.getUpdatedById(), "Modified updatedBy is unexpected.");
        assertEquals("modified", modified.getCn(), "Content was not updated.");
    }

    /**
     * Delete entity.
     */
    @Test
    void testDelete() throws Exception {
        // Given::
        final JrnlTodoEntity registered = jrnlTodoRepository.save(jrnlTodoEntity);
        final Integer key = registered.getId();

        // When::
        final JrnlTodoEntity toDelete = jrnlTodoRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete")));
        jrnlTodoRepository.delete(toDelete);
        final JrnlTodoEntity retrieved = jrnlTodoRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "Delete did not remove the entity.");
    }
}
