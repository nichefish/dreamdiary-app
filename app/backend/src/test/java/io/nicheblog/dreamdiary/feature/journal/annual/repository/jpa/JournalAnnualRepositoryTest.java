package io.nicheblog.dreamdiary.feature.journal.annual.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.journal.annual.entity.JournalAnnualEntity;
import io.nicheblog.dreamdiary.feature.journal.annual.entity.JournalAnnualEntityTestFactory;
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
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JournalAnnualRepositoryTest
 * <pre>
 *  저널 결산 (JPA) Repository 테스트 모듈
 *  "@Transactional 환경에서는 flush가 의도한 대로 작동하지 않을 수 있다."
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
class JournalAnnualRepositoryTest {
    
    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private JournalAnnualRepository journalAnnualRepository;
    
    private JournalAnnualEntity journalAnnualEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 journalDayEntity 초기화
        journalAnnualEntity = JournalAnnualEntityTestFactory.create();
    }

    /**
     * regist 테스트
     */
    @Test
    public void testRegist() throws Exception {
        // Given::

        // When::
        final JournalAnnualEntity registered = journalAnnualRepository.save(journalAnnualEntity);
        final Integer key = registered.getId();
        final JournalAnnualEntity retrieved = journalAnnualRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

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
        JournalAnnualEntity registered = journalAnnualRepository.save(journalAnnualEntity);
        Integer key = registered.getId();

        // When::
        JournalAnnualEntity toModify = journalAnnualRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-modify")));
        toModify.setContent("modified");
        JournalAnnualEntity modified = journalAnnualRepository.saveAndFlush(toModify);

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
        final JournalAnnualEntity registered = journalAnnualRepository.save(journalAnnualEntity);
        final Integer key = registered.getId();

        // When::
        final JournalAnnualEntity toDelete = journalAnnualRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete")));
        journalAnnualRepository.delete(toDelete);

        final JournalAnnualEntity retrieved = journalAnnualRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "삭제가 제대로 이루어지지 않았습니다.");
    }
}
