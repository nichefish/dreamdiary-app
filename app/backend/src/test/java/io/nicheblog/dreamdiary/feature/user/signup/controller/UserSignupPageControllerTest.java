package io.nicheblog.dreamdiary.feature.user.signup.controller;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.feature.user.signup.model.UserSignupRequestDto;
import io.nicheblog.dreamdiary.feature.user.signup.model.UserSignupRequestDtoTestFactory;
import io.nicheblog.dreamdiary.feature.user.signup.service.UserSignupService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import io.nicheblog.dreamdiary.infrastructure.web.config.WebMvcTestSliceSupportConfig;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerTestHelper;
import io.nicheblog.dreamdiary.infrastructure.freemarker.config.TestFreemarkerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserSignupPageController / UserSignupRestController WebMvc 테스트
 * <pre>
 *  사용자 계정 신청 컨트롤러 테스트 모듈
 * </pre>
 *
 * 변경 전: JSON 파트 이름 `userReqst` 및 USER_REQST URL 상수, PageController 클래스명 UserReqst* 혼선.
 * 변경 후: {@link AutoConfigureMockMvc#addFilters()} 로 필터를 끄고, Ajax 등록은 {@code POST} + 폼 필드 {@code param} 으로 보낸다.
 * 변경 전 근본 원인: {@link UserSignupRequestDto} 가 {@link io.nicheblog.dreamdiary.feature.user.account.model.UserDto} 와 동일 필드를 재선언해 WebMvc 바인딩이 어긋났고(해당 중복 필드 선언 제거로 정리됨 — 본 테스트는 회귀 방지용).
 * 테스트 슬라이스에서 보안 자동설정이 {@link JwtTokenProvider} 빈을 요구하므로 {@code @MockBean} 으로 주입한다.
 * 변경 후: 필터 비활성화로 CSRF 필터가 없으므로 슬라이스 POST 에 {@code csrf()} 를 붙이지 않는다(운영 {@code WebSecurityAdapter} 와의 차이는 테스트 범위 밖).
 * {@link io.nicheblog.dreamdiary.infrastructure.web.config.WebMvcContextConfig} 경로의 Freemarker 인터셉터가
 * {@link io.nicheblog.dreamdiary.global.ActiveProfile}·{@link io.nicheblog.dreamdiary.global.ReleaseInfo} 등을 요구하므로
 * {@link WebMvcTestSliceSupportConfig},{@link TestFreemarkerConfig} 를 {@literal @}Import 한다.
 *
 * @author nichefish
 */
@WebMvcTest(controllers = { UserSignupPageController.class, UserSignupRestController.class })
@Import({ TestFreemarkerConfig.class, WebMvcTestSliceSupportConfig.class })
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserSignupPageControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserSignupService userSignupService;
    @MockBean(name = "codeLookupService")
    private CodeLookupService codeLookupService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 신규계정 등록 화면 조회 Test
     */
    @Test
    public void testUserSignupRegForm() throws Exception {
        // given::

        // when::
        final MvcResult result = mockMvc.perform(get(Url.USER_SIGNUP_PAGE))
                .andExpect(status().isOk())
                .andReturn();

        verify(codeLookupService, times(5)).setCdListToModel(anyString(), any());

        // then::
        final String viewName = Objects.requireNonNull(result.getModelAndView()).getViewName();
        assertNotNull(viewName, "View name is null");
        assertTrue(BaseControllerTestHelper.viewFileExists(viewName), "View template file does not exist: " + viewName);
    }

    /**
     * 신규계정 등록 (Ajax) Test — 폼 필드 이름은 Vue multipart 와 동일 패턴이다.
     */
    @Test
    public void testUserSignupRegAjax() throws Exception {
        // given::
        final UserSignupRequestDto dto = UserSignupRequestDtoTestFactory.create();

        final UserSignupRequestDto rsltDto = UserSignupRequestDtoTestFactory.create();
        rsltDto.setId(0);
        final ServiceResponse result = ServiceResponse.builder().rsltObj(rsltDto).rslt(true).message("신규계정이 성공적으로 신청되었습니다.").build();
        when(userSignupService.regist(any(UserSignupRequestDto.class))).thenReturn(result);

        // 변경 전: {@link io.nicheblog.dreamdiary.feature.user.signup.model.UserSignupRequestDto} 에 UserDto 필드 재선언이 있을 때 폼 바인딩이 실패했다.
        // 변경 후: Dto 정리 후 {@code POST} + urlencoded 명목 + 폼 이름은 Vue(multipart)·레거시와 동일 키로 맞춘다.
        // when::
        mockMvc.perform(post(Url.USER_SIGNUP_REQUESTS)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("username", dto.getUsername())
                        .param("password", dto.getPassword())
                        .param("nickname", dto.getNickname())
                        .param("emailId", dto.getEmailId())
                        .param("emailDomain", dto.getEmailDomain())
                        .param("phoneNumber", dto.getPhoneNumber() != null ? dto.getPhoneNumber() : "")
                        .param("content", dto.getContent() != null ? dto.getContent() : "")
                        .param("roleKeysStr", dto.getRoleKeysStr())
                        .param("userRoles[0].roleKey", dto.getRoleKeysStr()))
                .andDo(print()) // 요청/응답 디테일 출력 (선택적)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.rslt").value(true))
                .andExpect(jsonPath("$.message").value("신규계정이 성공적으로 신청되었습니다."));
    }

    @Test
    @WithMockUser(username = "mngr-test", authorities = "ROLE_MNGR")
    public void testUserCfAjax() throws Exception {
        final ServiceResponse result = ServiceResponse.builder().rslt(true).message(MessageUtils.getMessage("common.result.success")).build();
        when(userSignupService.cf(anyInt())).thenReturn(result);

        mockMvc.perform(post(Url.USER_SIGNUP_REQUEST_APPROVAL.replace("{id}", "123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(true));
    }

    @Test
    @WithMockUser(username = "mngr-test", authorities = "ROLE_MNGR")
    public void testUserUncfAjax() throws Exception {
        final ServiceResponse result = ServiceResponse.builder().rslt(true).message(MessageUtils.getMessage("common.result.success")).build();
        when(userSignupService.uncf(anyInt())).thenReturn(result);

        mockMvc.perform(post(Url.USER_SIGNUP_REQUEST_REJECTION.replace("{id}", "123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(true));
    }

}
