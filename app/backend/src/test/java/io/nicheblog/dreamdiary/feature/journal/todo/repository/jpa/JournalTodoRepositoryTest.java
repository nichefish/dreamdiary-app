package io.nicheblog.dreamdiary.feature.journal.todo.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.journal.todo.entity.JournalTodoEntity;
import io.nicheblog.dreamdiary.feature.journal.todo.entity.JournalTodoEntityTestFactory;
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
 * JournalTodoRepositoryTest
 * <pre>
 *  JPA repository tests for JournalTodo.
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
class JournalTodoRepositoryTest {

    @Resource
    private JournalTodoRepository journalTodoRepository;

    private JournalTodoEntity journalTodoEntity;

    /**
     * Initialize test data before each case.
     */
    @BeforeEach
    void setUp() throws Exception {
        journalTodoEntity = JournalTodoEntityTestFactory.create();
    }

    /**
     * Persist entity.
     */
    @Test
    void testRegist() throws Exception {
        // Given::

        // When::
        final JournalTodoEntity registered = journalTodoRepository.save(journalTodoEntity);
        final Integer key = registered.getId();
        final JournalTodoEntity retrieved = journalTodoRepository.findById(key)
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
        final JournalTodoEntity registered = journalTodoRepository.save(journalTodoEntity);
        final Integer key = registered.getId();

        // When::
        final JournalTodoEntity toModify = journalTodoRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-modify")));
        toModify.setContent("modified");
        final JournalTodoEntity modified = journalTodoRepository.saveAndFlush(toModify);

        // Then::
        assertNotNull(modified, "Could not load modified entity.");
        assertNotNull(modified.getId(), "Modified entity key is null.");
        assertNotNull(modified.getUpdatedAt(), "updatedAt audit was not set.");
        assertNotNull(modified.getUpdatedBy(), "updatedBy audit was not set.");
        assertEquals(TestConstant.TEST_AUDITOR, modified.getUpdatedBy(), "Modified updatedBy is unexpected.");
        assertEquals("modified", modified.getContent(), "Content was not updated.");
    }

    /**
     * Delete entity.
     */
    @Test
    void testDelete() throws Exception {
        // Given::
        final JournalTodoEntity registered = journalTodoRepository.save(journalTodoEntity);
        final Integer key = registered.getId();

        // When::
        final JournalTodoEntity toDelete = journalTodoRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete")));
        journalTodoRepository.delete(toDelete);
        final JournalTodoEntity retrieved = journalTodoRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "Delete did not remove the entity.");
    }
}
