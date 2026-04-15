package io.nicheblog.dreamdiary.feature.journal.diary.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryPostDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDtoTestFactory;
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
 * JournalDiaryServiceTest
 * <pre>
 *  저널 일기 서비스 테스트 모듈
 *  "@Transactional 어노테이션 적용시 테스트 이후 트랜잭션이 롤백된다."
 * </pre>
 * 
 * @author nichefish 
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class JournalDiaryServiceTest {
    
    @Resource
    private JournalDiaryService journalDiaryService;

    @MockBean
    @SuppressWarnings("unused")
    private AuthUtils authUtils;

    private JournalDiaryPostDto journalDiary;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JournalDiaryDto 초기화
        journalDiary = JournalDiaryDtoTestFactory.createPost();

        // AuthUtils Mock
        try (final MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::isAuthenticated).thenReturn(true);
            mockedStatic.when(AuthUtils::getLgnUsername).thenReturn(TestConstant.TEST_AUDITOR);
        }
    }

    /**
     * 저널 일기 등록
     */
    @Test
    void regist() throws Exception {
        // Given::

        // When::
        final ServiceResponse registResult = journalDiaryService.regist(journalDiary);
        final JournalDiaryDto registered = (JournalDiaryDto) registResult.getRsltObj();

        // Then::
        assertNotNull(registered.getId(), "등록이 정상적으로 이루어지지 않았습니다.");
    }

    /**
     * 저널 일기 수정
     */
    @Test
    void modify() throws Exception {
        // Given::
        final ServiceResponse registResult = journalDiaryService.regist(journalDiary);
        final JournalDiaryDto registered = (JournalDiaryDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final JournalDiaryPostDto toModify = JournalDiaryDtoTestFactory.createPostWithKey(key);
        toModify.setContent("test");
        final ServiceResponse modifyResult = journalDiaryService.modify(toModify);
        final JournalDiaryDto modified = (JournalDiaryDto) modifyResult.getRsltObj();

        // Then::
        assertNotNull(modified.getId(), "수정이 정상적으로 이루어지지 않았습니다.");
        assertEquals("test", modified.getContent(), "수정이 정상적으로 이루어지지 않았습니다.");
    }

    /**
     * 저널 일기 삭제
     */
    @Test
    void delete() throws Exception {
        // Given::
        final ServiceResponse registResult = journalDiaryService.regist(journalDiary);
        final JournalDiaryDto registered = (JournalDiaryDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final ServiceResponse deletetResult = journalDiaryService.delete(key);
        final Boolean isDeleted = deletetResult.getRslt();

        // Then::
        assertTrue(isDeleted, "삭제가 정상적으로 이루어지지 않았습니다.");
        // 삭제된 엔티티 조회
        assertThrows(EntityNotFoundException.class,
                () -> journalDiaryService.getDtlDto(key),
                "삭제된 엔티티를 조회하려고 했으나 예외가 발생하지 않았습니다."
        );
    }
}

