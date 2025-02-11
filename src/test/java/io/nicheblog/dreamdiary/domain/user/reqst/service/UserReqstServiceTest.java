package io.nicheblog.dreamdiary.domain.user.reqst.service;

import io.nicheblog.dreamdiary.domain.user.emplym.model.UserEmplymDtoTestFactory;
import io.nicheblog.dreamdiary.domain.user.info.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.domain.user.info.model.profl.UserProflDto;
import io.nicheblog.dreamdiary.domain.user.profl.model.UserProflDtoTestFactory;
import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDto;
import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDtoTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * UserReqstServiceTest
 * <pre>
 *  사용자 신규계정 신청 프로세스 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class UserReqstServiceTest {

    @Resource(name = "userReqstService")
    private UserReqstService userReqstService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    /**
     * 사용자 산규계정 신청 등록 테스트
     */
    @Test
    void testRegist() throws Exception {
        // Given:: 등록할 사용자 계정 신청 정보를 준비한다.
        UserProflDto profl = UserProflDtoTestFactory.create();
        UserEmplymDto emplym = UserEmplymDtoTestFactory.create();
        UserReqstDto registDto = UserReqstDtoTestFactory.create(profl, emplym);

        // When::
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");       // 패스워드 인코딩 Mock
        UserReqstDto registered = userReqstService.regist(registDto);

        // Then::
        assertNotNull(registered, "사용자 계정 신청이 제대로 이루어지지 않았습니다.");
        // audit
        assertNotNull(registered.getRegDt(), "등록일자 audit 처리가 되지 않았습니다.");
        assertNotNull(registered.getRegstrId(),  "등록자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, registered.getRegstrId(), "등록자가 예상 값과 일치하지 않습니다.");
    }
}