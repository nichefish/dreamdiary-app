package io.nicheblog.dreamdiary.feature.user.signup.controller;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.handler.SecurityErrorResponseWriter;
import io.nicheblog.dreamdiary.auth.security.matcher.PublicApiRequestMatcher;
import io.nicheblog.dreamdiary.auth.security.service.AuthSessionPolicyService;
import io.nicheblog.dreamdiary.feature.user.signup.model.UserSignupRequestDto;
import io.nicheblog.dreamdiary.feature.user.signup.model.UserSignupRequestDtoTestFactory;
import io.nicheblog.dreamdiary.feature.user.signup.repository.jpa.UserSignupRequestRepository;
import io.nicheblog.dreamdiary.feature.user.signup.service.UserSignupService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import io.nicheblog.dreamdiary.infrastructure.web.config.WebMvcTestSliceSupportConfig;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
 * Vue 화면 리다이렉트와 폼 기반 가입 API 바인딩을 검증한다.
 * 보안 필터는 비활성화하고, WebMvc 슬라이스가 참조하는 보안·웹 레이어 협력 객체는 MockBean으로 제공한다.
 * {@link WebMvcTestSliceSupportConfig} 는 공통 웹 레이어 빈을 보충한다.
 *
 * @author nichefish
 */
@WebMvcTest(controllers = { UserSignupPageController.class, UserSignupRestController.class })
@Import({ WebMvcTestSliceSupportConfig.class })
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
    @MockBean
    private AuthSessionPolicyService authSessionPolicyService;
    @MockBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;
    @MockBean
    private PublicApiRequestMatcher publicApiRequestMatcher;
    @MockBean
    private UserSignupRequestRepository userSignupRequestRepository;

    /**
     * 신규계정 등록 화면 조회 Test
     * Vue SPA 가입 화면으로 이동하는 리다이렉트 경로를 검증한다.
     */
    @Test
    public void testUserSignupRegForm() throws Exception {
        // given::

        // when:: / then::
        mockMvc.perform(get(Url.USER_SIGNUP_PAGE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vue-app/user/signup"));
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

        // Vue 가입 요청과 동일한 필드 이름으로 폼 바인딩을 검증한다.
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
