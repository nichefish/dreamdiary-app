package io.nicheblog.dreamdiary.feature.board.group.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntityTestFactory;
import io.nicheblog.dreamdiary.feature.board.group.jpa.BoardRepository;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(DataSourceConfig.class)
@Import(TestAuditConfig.class)
@Log4j2
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    private BoardEntity boardEntity;

    @BeforeEach
    void setUp() throws Exception {
        boardEntity = BoardEntityTestFactory.create("CMPY_LIFE");
    }

    @Test
    void testRegist() throws Exception {
        final BoardEntity registered = boardRepository.save(boardEntity);
        final String boardKey = registered.getBoardKey();
        final BoardEntity retrieved = boardRepository.findByBoardKey(boardKey)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        assertNotNull(retrieved);
        assertNotNull(retrieved.getId());
        assertNotNull(retrieved.getBoardKey());
        assertNotNull(retrieved.getCreatedAt());
        assertNotNull(retrieved.getCreatedBy());
        assertEquals(TestConstant.TEST_AUDITOR, retrieved.getCreatedBy());
    }

    @Test
    void testModify() throws Exception {
        final BoardEntity registered = boardRepository.save(boardEntity);
        final String boardKey = registered.getBoardKey();

        final BoardEntity toModify = boardRepository.findByBoardKey(boardKey)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-modify")));
        toModify.setDescription("modified");
        final BoardEntity modified = boardRepository.save(toModify);

        assertNotNull(modified);
        assertNotNull(modified.getId());
        assertNotNull(modified.getBoardKey());
        assertNotNull(modified.getUpdatedAt());
        assertNotNull(modified.getUpdatedBy());
        assertEquals(TestConstant.TEST_AUDITOR, modified.getUpdatedBy());
        assertEquals("modified", modified.getDescription());
    }

    @Test
    void testDelete() throws Exception {
        final BoardEntity registered = boardRepository.save(boardEntity);
        final String boardKey = registered.getBoardKey();

        final BoardEntity toDelete = boardRepository.findByBoardKey(boardKey)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete")));
        boardRepository.delete(toDelete);

        final BoardEntity retrieved = boardRepository.findByBoardKey(boardKey).orElse(null);
        assertNull(retrieved);
    }
}
