package io.nicheblog.dreamdiary.feature.board.post.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntityTestFactory;
import io.nicheblog.dreamdiary.feature.board.group.jpa.BoardRepository;
import io.nicheblog.dreamdiary.feature.board.post.entity.BoardPostEntity;
import io.nicheblog.dreamdiary.feature.board.post.entity.BoardPostEntityTestFactory;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * BoardPostRepositoryTest
 * <pre>
 *  게시판 게시물 (JPA) Repository 테스트 모듈
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
class BoardPostRepositoryTest {

    @Autowired
    private BoardPostRepository boardPostRepository;

    @Autowired
    private BoardRepository boardRepository;

    private BoardEntity boardEntity;
    private BoardPostEntity boardPostEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 boardPostEntity 초기화
        boardEntity = boardRepository.saveAndFlush(BoardEntityTestFactory.create("CMPY_LIFE"));
        boardPostEntity = BoardPostEntityTestFactory.create(boardEntity.getBoardKey());
    }

    /**
     * regist 테스트
     */
    @Test
    void testRegist() {
        // Given::

        // When::
        final BoardPostEntity registered = boardPostRepository.save(boardPostEntity);
        final Integer key = registered.getId();
        final BoardPostEntity retrieved = boardPostRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        // Then::
        assertNotNull(retrieved);
        assertNotNull(retrieved.getId());
        // audit
        assertNotNull(retrieved.getCreatedAt());
        assertNotNull(retrieved.getCreatedBy());
        assertEquals(TestConstant.TEST_AUDITOR, retrieved.getCreatedBy());
        assertEquals(boardEntity.getBoardKey(), retrieved.getContentType());
    }

    /**
     * modify 테스트
     */
    @Test
    void testModify() {
        // Given::
        final BoardPostEntity registered = boardPostRepository.save(boardPostEntity);
        final Integer key = registered.getId();

        // When::
        final BoardPostEntity toModify = boardPostRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-modify")));
        toModify.setContent("modified");
        final BoardPostEntity modified = boardPostRepository.saveAndFlush(toModify);

        // Then::
        assertNotNull(modified);
        assertNotNull(modified.getId());
        // audit
        assertNotNull(modified.getUpdatedAt());
        assertNotNull(modified.getUpdatedBy());
        assertEquals(TestConstant.TEST_AUDITOR, modified.getUpdatedBy());
        // value
        assertEquals("modified", modified.getContent());
    }

    /**
     * delete 테스트
     */
    @Test
    void testDelete() {
        // Given::
        final BoardPostEntity registered = boardPostRepository.save(boardPostEntity);
        final Integer key = registered.getId();

        // When::
        final BoardPostEntity toDelete = boardPostRepository.findById(key)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete")));
        boardPostRepository.delete(toDelete);

        // Then::
        final BoardPostEntity retrieved = boardPostRepository.findById(key).orElse(null);
        assertNull(retrieved);
    }
}
