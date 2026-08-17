package io.nicheblog.dreamdiary.feature.journal.day.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.journal.JournalTestUserSupport;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDtoTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JournalDayServiceTest
 * <pre>
 *  저널 일자 관리 서비스 테스트 모듈
 *  "@Transactional 어노테이션 적용시 테스트 이후 트랜잭션이 롤백된다."
 * </pre>
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class JournalDayServiceTest {

    @Resource
    private JournalDayService journalDayService;
    @Resource
    private UserRepository userRepository;

    private JournalDayDto journalDay;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        final Integer ownerId = JournalTestUserSupport.ensureUser(userRepository, TestConstant.TEST_AUDITOR);
        JournalTestUserSupport.authenticate(ownerId, TestConstant.TEST_AUDITOR);
        // 공통적으로 사용할 JournalDayDto 초기화
        journalDay = JournalDayDtoTestFactory.createWithJournalDt("2000-01-01");
    }

    /**
     * 저널 일자 등록
     */
    @Test
    void regist() throws Exception {
        // Given::

        // When::
        final ServiceResponse result = journalDayService.regist(journalDay);
        final JournalDayDto registered = (JournalDayDto) result.getRsltObj();

        // Then::
        assertNotNull(registered, "등록이 정상적으로 이루어지지 않았습니다.");
        assertNotNull(registered.getId(), "등록이 정상적으로 이루어지지 않았습니다.");
        assertNotNull(registered.getOwnerId(), "저널 일자 소유자 ID가 설정되지 않았습니다.");
        // audit
        assertNotNull(registered.getCreatedAt(), "등록일자 audit 처리가 되지 않았습니다.");
        assertNotNull(registered.getCreatedBy(),  "등록자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, registered.getCreatedBy(), "등록자가 예상 값과 일치하지 않습니다.");
    }

    /**
     * 저널 일자 수정
     */
    @Test
    void modify() throws Exception {
        // Given::
        final ServiceResponse registResult = journalDayService.regist(journalDay);
        final JournalDayDto registered = (JournalDayDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final JournalDayDto toModify = JournalDayDtoTestFactory.createWithKey(key);
        toModify.setJournalDate("2020-01-01");
        toModify.setOwnerId(registered.getOwnerId() + 1);
        final ServiceResponse modifyResult =  journalDayService.modify(toModify);
        final JournalDayDto updated = (JournalDayDto) modifyResult.getRsltObj();

        // Then::
        assertNotNull(updated.getId(), "수정이 정상적으로 이루어지지 않았습니다.");
        assertEquals("2020-01-01", updated.getJournalDate(), "수정이 정상적으로 이루어지지 않았습니다.");
        assertEquals(registered.getOwnerId(), updated.getOwnerId(), "일반 수정 요청이 소유자를 변경했습니다.");
        // audit
        assertNotNull(updated.getUpdatedAt(), "수정일자 audit 처리가 되지 않았습니다.");
        assertNotNull(updated.getUpdatedBy(),  "수정자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, updated.getUpdatedBy(), "수정자가 예상 값과 일치하지 않습니다.");
    }

    /**
     * 저널 일자 삭제
     */
    @Test
    void delete() throws Exception {
        // Given::
        final ServiceResponse registResult = journalDayService.regist(journalDay);
        final JournalDayDto registered = (JournalDayDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final ServiceResponse deletedResult = journalDayService.delete(key);
        final Boolean isDeleted = deletedResult.getRslt();

        // Then::
        assertTrue(isDeleted, "삭제가 정상적으로 이루어지지 않았습니다.");
        // 삭제된 엔티티 조회
        assertThrows(EntityNotFoundException.class,
                () -> journalDayService.getDtlDto(key),
                "삭제된 엔티티를 조회하려고 했으나 예외가 발생하지 않았습니다."
        );
    }

    /**
     * 같은 사용자의 같은 일자 중복 등록 방지
     */
    @Test
    void regist_duplicateJournalDate_rejected() throws Exception {
        // Given::
        journalDayService.regist(journalDay);
        final JournalDayDto duplicate = JournalDayDtoTestFactory.createWithJournalDt("2000-01-01");

        // When & Then::
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> journalDayService.regist(duplicate),
                "같은 사용자와 같은 일자의 저널 일자 중복 등록이 차단되지 않았습니다."
        );
        assertEquals("journal.day.duplicate", exception.getMessage());
    }
}
