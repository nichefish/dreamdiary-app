package io.nicheblog.dreamdiary.feature.journal.diary.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntityTestFactory;
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
 * JournalDiaryRepositoryTest
 * <pre>
 *  저널 일기 (JPA) Repository 테스트 모듈
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
class JournalDiaryRepositoryTest {

    @Resource
    private JournalDiaryRepository journalDiaryRepository;

    private JournalDiaryEntity journalDiaryEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 journalDiaryEntity 초기화
        journalDiaryEntity = JournalDiaryEntityTestFactory.createWithJournalDt("2000-01-01");
    }

    /**
     * regist 테스트
     */
    @Test
    public void testRegist() throws Exception {
        // Given::

        // When::
        final JournalDiaryEntity registered = journalDiaryRepository.save(journalDiaryEntity);
        final Integer key = registered.getId();
        final JournalDiaryEntity retrieved = journalDiaryRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        // Then::
        assertNotNull(retrieved, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(retrieved.getId(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(retrieved.getCreatedAt(), "등록일자 audit 처리가 되지 않았습니다.");
        assertNotNull(retrieved.getCreatedBy(),  "등록자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, retrieved.getCreatedBy(), "등록자가 예상 값과 일치하지 않습니다.");
    }

    /**
     * modify 테스트
     */
    @Test
    public void testModify() throws Exception {
        // Given::
        JournalDiaryEntity registered = journalDiaryRepository.save(journalDiaryEntity);
        Integer key = registered.getId();

        // When::
        JournalDiaryEntity toModify = journalDiaryRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-modify")));
        toModify.setContent("modified");
        JournalDiaryEntity modified = journalDiaryRepository.save(toModify);

        // Then::
        assertNotNull(modified, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(modified.getId(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(modified.getUpdatedAt(), "수정일자 audit 처리가 되지 않았습니다.");
        assertNotNull(modified.getUpdatedBy(),  "수정자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, modified.getUpdatedBy(), "수정자가 예상 값과 일치하지 않습니다.");
        // value
        assertEquals("modified", modified.getContent(), "값이 정상적으로 수정되지 않았습니다.");
    }

    /**
     * delete 테스트
     */
    @Test
    public void testDelete() throws Exception {
        // Given::
        final JournalDiaryEntity registered = journalDiaryRepository.save(journalDiaryEntity);
        final Integer key = registered.getId();

        // When::
        final JournalDiaryEntity toDelete = journalDiaryRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete")));
        journalDiaryRepository.delete(toDelete);

        final JournalDiaryEntity retrieved = journalDiaryRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "삭제가 제대로 이루어지지 않았습니다.");
    }
}

