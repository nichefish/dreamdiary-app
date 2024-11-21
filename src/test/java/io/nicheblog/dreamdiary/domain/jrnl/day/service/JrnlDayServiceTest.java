package io.nicheblog.dreamdiary.domain.jrnl.day.service;

import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDtoTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global._common.auth.util.AuthUtils;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

/**
 * JrnlDayServiceTest
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
class JrnlDayServiceTest {

    @Resource
    private JrnlDayService jrnlDayService;

    @MockBean
    @SuppressWarnings("unused")
    private AuthUtils authUtils;

    private JrnlDayDto jrnlDay;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JrnlDayDto 초기화
        jrnlDay = JrnlDayDtoTestFactory.createWithJrnlDt("2000-01-01");

        // AuthUtils Mock
        try (MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::isAuthenticated).thenReturn(true);
            mockedStatic.when(AuthUtils::getLgnUserId).thenReturn(TestConstant.TEST_AUDITOR);
        }
    }

    /**
     * 저널 일자 등록
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
    @Test
    void regist() throws Exception {
        // Given::

        // When::
        JrnlDayDto registered = jrnlDayService.regist(jrnlDay);

        // Then::
        assertNotNull(registered, "등록이 정상적으로 이루어지지 않았습니다.");
        assertNotNull(registered.getPostNo(), "등록이 정상적으로 이루어지지 않았습니다.");
        // audit
        assertNotNull(registered.getRegDt(), "등록일자 audit 처리가 되지 않았습니다.");
        assertNotNull(registered.getRegstrId(),  "등록자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, registered.getRegstrId(), "등록자가 예상 값과 일치하지 않습니다.");
    }

    /**
     * 저널 일자 수정
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
    @Test
    void modify() throws Exception {
        // Given::
        JrnlDayDto registered = jrnlDayService.regist(jrnlDay);
        Integer key = registered.getKey();

        // When::
        JrnlDayDto toModify = JrnlDayDtoTestFactory.createWithKey(key);
        toModify.setJrnlDt("2020-01-01");
        JrnlDayDto updated = jrnlDayService.modify(toModify);

        // Then::
        assertNotNull(updated.getPostNo(), "수정이 정상적으로 이루어지지 않았습니다.");
        assertEquals("2020-01-01", updated.getJrnlDt(), "수정이 정상적으로 이루어지지 않았습니다.");
        // audit
        assertNotNull(updated.getMdfDt(), "수정일자 audit 처리가 되지 않았습니다.");
        assertNotNull(updated.getMdfusrId(),  "수정자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, updated.getMdfusrId(), "수정자가 예상 값과 일치하지 않습니다.");
    }

    /**
     * 저널 일자 삭제
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
    @Test
    void delete() throws Exception {
        // Given::
        JrnlDayDto registered = jrnlDayService.regist(jrnlDay);
        Integer key = registered.getKey();

        // When::
        Boolean isDeleted = jrnlDayService.delete(key);

        // Then::
        assertTrue(isDeleted, "삭제가 정상적으로 이루어지지 않았습니다.");
        // 삭제된 엔티티 조회
        assertThrows(EntityNotFoundException.class,
                () -> jrnlDayService.getDtlDto(key),
                "삭제된 엔티티를 조회하려고 했으나 예외가 발생하지 않았습니다."
        );
    }
}