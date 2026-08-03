package io.nicheblog.dreamdiary.feature.journal.thread.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDtoTestFactory;
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
 * JournalThreadServiceTest
 * <pre>
 *  저널 꿈 서비스 테스트 모듈
 *  "@Transactional 어노테이션 적용시 테스트 이후 트랜잭션이 롤백된다."
 * </pre>
 * 
 * @author nichefish 
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
@WithMockUser(username = TestConstant.TEST_AUDITOR)
class JournalThreadServiceTest {
    
    @Resource
    private JournalThreadService journalThreadService;

    private JournalThreadDto journalThread;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JournalThreadDto 초기화
        journalThread = JournalThreadDtoTestFactory.create();

        // 인증 사용자는 @WithMockUser가 테스트마다 설정한다.
    }

    /**
     * 저널 꿈 등록
     */
    @Test
    void regist() throws Exception {
        // Given::

        // When::
        final ServiceResponse registResult = journalThreadService.regist(journalThread);
        final JournalThreadDto registered = (JournalThreadDto) registResult.getRsltObj();

        // Then::
        assertNotNull(registered.getId(), "등록이 정상적으로 이루어지지 않았습니다.");
    }

    /**
     * 저널 꿈 수정
     */
    @Test
    void modify() throws Exception {
        // Given::
        final ServiceResponse registResult = journalThreadService.regist(journalThread);
        final JournalThreadDto registered = (JournalThreadDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final JournalThreadDto toModify = JournalThreadDtoTestFactory.createWithKey(key);
        toModify.setContent("test");
        final ServiceResponse modifyResult = journalThreadService.modify(toModify);
        final JournalThreadDto modified = (JournalThreadDto) modifyResult.getRsltObj();

        // Then::
        assertNotNull(modified.getId(), "수정이 정상적으로 이루어지지 않았습니다.");
        assertEquals("test", modified.getContent(), "수정이 정상적으로 이루어지지 않았습니다.");
    }

    /**
     * 저널 꿈 삭제
     */
    @Test
    void delete() throws Exception {
        // Given::
        final ServiceResponse registResult = journalThreadService.regist(journalThread);
        final JournalThreadDto registered = (JournalThreadDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final ServiceResponse deleteResult = journalThreadService.delete(key);
        final Boolean isDeleted = deleteResult.getRslt();

        // Then::
        assertTrue(isDeleted, "삭제가 정상적으로 이루어지지 않았습니다.");
        // 삭제된 엔티티 조회
        assertThrows(EntityNotFoundException.class,
                () -> journalThreadService.getDtlDto(key),
                "삭제된 엔티티를 조회하려고 했으나 예외가 발생하지 않았습니다."
        );
    }
}
