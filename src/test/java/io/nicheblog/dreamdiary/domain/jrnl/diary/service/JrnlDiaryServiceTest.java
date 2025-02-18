package io.nicheblog.dreamdiary.domain.jrnl.diary.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDtoTestFactory;
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
 * JrnlDiaryServiceTest
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
class JrnlDiaryServiceTest {
    
    @Resource
    private JrnlDiaryService jrnlDiaryService;

    @MockBean
    @SuppressWarnings("unused")
    private AuthUtils authUtils;

    private JrnlDiaryDto jrnlDiary;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JrnlDiaryDto 초기화
        jrnlDiary = JrnlDiaryDtoTestFactory.create();

        // AuthUtils Mock
        try (final MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::isAuthenticated).thenReturn(true);
            mockedStatic.when(AuthUtils::getLgnUserId).thenReturn(TestConstant.TEST_AUDITOR);
        }
    }

    /**
     * 저널 일기 등록
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
    @Test
    void regist() throws Exception {
        // Given::

        // When::
        final ServiceResponse registResult = jrnlDiaryService.regist(jrnlDiary);
        final JrnlDiaryDto registered = (JrnlDiaryDto) registResult.getRsltObj();

        // Then::
        assertNotNull(registered.getPostNo(), "등록이 정상적으로 이루어지지 않았습니다.");
    }

    /**
     * 저널 일기 수정
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
    @Test
    void modify() throws Exception {
        // Given::
        final ServiceResponse registResult = jrnlDiaryService.regist(jrnlDiary);
        final JrnlDiaryDto registered = (JrnlDiaryDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final JrnlDiaryDto toModify = JrnlDiaryDtoTestFactory.createWithKey(key);
        toModify.setCn("test");
        final ServiceResponse modifyResult = jrnlDiaryService.modify(toModify);
        final JrnlDiaryDto modified = (JrnlDiaryDto) modifyResult.getRsltObj();

        // Then::
        assertNotNull(modified.getPostNo(), "수정이 정상적으로 이루어지지 않았습니다.");
        assertEquals("test", modified.getCn(), "수정이 정상적으로 이루어지지 않았습니다.");
    }

    /**
     * 저널 일기 삭제
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
    @Test
    void delete() throws Exception {
        // Given::
        final ServiceResponse registResult = jrnlDiaryService.regist(jrnlDiary);
        final JrnlDiaryDto registered = (JrnlDiaryDto) registResult.getRsltObj();
        final Integer key = registered.getKey();

        // When::
        final ServiceResponse deletetResult = jrnlDiaryService.delete(key);
        final Boolean isDeleted = deletetResult.getRslt();

        // Then::
        assertTrue(isDeleted, "삭제가 정상적으로 이루어지지 않았습니다.");
        // 삭제된 엔티티 조회
        assertThrows(EntityNotFoundException.class,
                () -> jrnlDiaryService.getDtlDto(key),
                "삭제된 엔티티를 조회하려고 했으나 예외가 발생하지 않았습니다."
        );
    }
}