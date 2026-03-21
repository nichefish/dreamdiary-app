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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        final Integer key = registered.getPostNo();
        final JrnlTodoEntity retrieved = jrnlTodoRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        // Then::
        assertNotNull(retrieved, "Could not load saved entity.");
        assertNotNull(retrieved.getPostNo(), "Saved entity key is null.");
        assertNotNull(retrieved.getRegDt(), "regDt audit was not set.");
        assertNotNull(retrieved.getRegstrId(), "regstrId audit was not set.");
        assertEquals(TestConstant.TEST_AUDITOR, retrieved.getRegstrId(), "Saved regstrId is unexpected.");
    }

    /**
     * Update entity.
     */
    @Test
    void testModify() throws Exception {
        // Given::
        final JrnlTodoEntity registered = jrnlTodoRepository.save(jrnlTodoEntity);
        final Integer key = registered.getPostNo();

        // When::
        final JrnlTodoEntity toModify = jrnlTodoRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-modify")));
        toModify.setCn("modified");
        final JrnlTodoEntity modified = jrnlTodoRepository.save(toModify);

        // Then::
        assertNotNull(modified, "Could not load modified entity.");
        assertNotNull(modified.getPostNo(), "Modified entity key is null.");
        assertNotNull(modified.getMdfDt(), "mdfDt audit was not set.");
        assertNotNull(modified.getMdfusrId(), "mdfusrId audit was not set.");
        assertEquals(TestConstant.TEST_AUDITOR, modified.getMdfusrId(), "Modified mdfusrId is unexpected.");
        assertEquals("modified", modified.getCn(), "Content was not updated.");
    }

    /**
     * Delete entity.
     */
    @Test
    void testDelete() throws Exception {
        // Given::
        final JrnlTodoEntity registered = jrnlTodoRepository.save(jrnlTodoEntity);
        final Integer key = registered.getPostNo();

        // When::
        final JrnlTodoEntity toDelete = jrnlTodoRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete")));
        jrnlTodoRepository.delete(toDelete);
        final JrnlTodoEntity retrieved = jrnlTodoRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "Delete did not remove the entity.");
    }
}
