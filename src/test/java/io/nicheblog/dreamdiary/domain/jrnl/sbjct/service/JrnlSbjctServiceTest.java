package io.nicheblog.dreamdiary.domain.jrnl.sbjct.service;

import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.model.JrnlSbjctDto;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.model.JrnlSbjctDtoTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.config.TestAuditConfig;
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
 * JrnlSbjctServiceTest
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
class JrnlSbjctServiceTest {
    
    @Resource
    private JrnlSbjctService jrnlSbjctService;

    @MockBean
    @SuppressWarnings("unused")
    private AuthUtils authUtils;

    private JrnlSbjctDto.DTL jrnlSbjct;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JrnlSbjctDto 초기화
        jrnlSbjct = JrnlSbjctDtoTestFactory.create();

        // AuthUtils Mock
        try (MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::isAuthenticated).thenReturn(true);
            mockedStatic.when(AuthUtils::getLgnUserId).thenReturn(TestConstant.TEST_AUDITOR);
        }
    }

    /**
     * 저널 꿈 등록
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
    @Test
    void regist() throws Exception {
        // Given::

        // When::
        JrnlSbjctDto result = jrnlSbjctService.regist(jrnlSbjct);

        // Then::
        assertNotNull(result.getPostNo(), "등록이 정상적으로 이루어지지 않았습니다.");
    }

    /**
     * 저널 꿈 수정
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
    @Test
    void modify() throws Exception {
        // Given::
        JrnlSbjctDto base = jrnlSbjctService.regist(jrnlSbjct);
        Integer key = base.getKey();

        // When::
        JrnlSbjctDto.DTL toModify = JrnlSbjctDtoTestFactory.createWithKey(key);
        toModify.setCn("test");
        JrnlSbjctDto.DTL result = jrnlSbjctService.modify(toModify);

        // Then::
        assertNotNull(result.getPostNo(), "수정이 정상적으로 이루어지지 않았습니다.");
        assertEquals("test", result.getCn(), "수정이 정상적으로 이루어지지 않았습니다.");
    }

    /**
     * 저널 꿈 삭제
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
    @Test
    void delete() throws Exception {
        // Given::
        JrnlSbjctDto base = jrnlSbjctService.regist(jrnlSbjct);
        Integer key = base.getKey();

        // When::
        Boolean isDeleted = jrnlSbjctService.delete(key);

        // Then::
        assertTrue(isDeleted, "삭제가 정상적으로 이루어지지 않았습니다.");
        // 삭제된 엔티티 조회
        assertThrows(EntityNotFoundException.class,
                () -> jrnlSbjctService.getDtlDto(key),
                "삭제된 엔티티를 조회하려고 했으나 예외가 발생하지 않았습니다."
        );
    }
}