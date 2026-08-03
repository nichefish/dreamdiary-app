package io.nicheblog.dreamdiary.feature.attachable.lifecycle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.handler.SecurityErrorResponseWriter;
import io.nicheblog.dreamdiary.auth.security.matcher.PublicApiRequestMatcher;
import io.nicheblog.dreamdiary.auth.security.service.AuthSessionPolicyService;
import io.nicheblog.dreamdiary.feature.attachable._shared.model.AttachableCacheContext;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.LifecycleSetDto;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.infrastructure.web.config.WebMvcTestSliceSupportConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 라이프사이클 설정 API의 HTTP·역할·소유권 예외 경계 테스트.
 *
 * @author nichefish
 */
@WebMvcTest(LifecycleRestController.class)
@Import({WebMvcTestSliceSupportConfig.class, LifecycleRestControllerTest.MethodSecurityTestConfig.class})
@ActiveProfiles("test")
class LifecycleRestControllerTest {

    private static final Integer FIXTURE_CONTENT_ID = 73;
    private static final Integer FIXTURE_YEAR = 2026;
    private static final Integer FIXTURE_MONTH = 8;
    private static final String FIXTURE_WEEK_START_DATE = "2026-08-03";

    /** MVC 슬라이스에서도 운영과 같은 {@code @Secured} 평가를 활성화한다. */
    @TestConfiguration
    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, proxyTargetClass = true)
    static class MethodSecurityTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LifecycleService lifecycleService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private AuthSessionPolicyService authSessionPolicyService;
    @MockBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;
    @MockBean
    private PublicApiRequestMatcher publicApiRequestMatcher;

    /** 일반 사용자의 설정 요청은 대상·라이프사이클·캐시 컨텍스트를 서비스에 그대로 전달한다. */
    @Test
    @WithMockUser(roles = "USER")
    void userSetsLifecycleWithCacheContext() throws Exception {
        when(lifecycleService.set(any(LifecycleSetDto.class))).thenReturn(successResponse());

        mockMvc.perform(put("/api/lifecycles")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(true))
                .andExpect(jsonPath("$.rsltObj.previousLifecycleKey").value("OPEN"))
                .andExpect(jsonPath("$.rsltObj.currentLifecycleKey").value("PENDING"));

        final org.mockito.ArgumentCaptor<LifecycleSetDto> captor =
                org.mockito.ArgumentCaptor.forClass(LifecycleSetDto.class);
        verify(lifecycleService).set(captor.capture());
        final LifecycleSetDto forwarded = captor.getValue();
        assertThat(forwarded.getId()).isEqualTo(FIXTURE_CONTENT_ID);
        assertThat(forwarded.getContentType()).isEqualTo(ContentType.JOURNAL_DIARY);
        assertThat(forwarded.getLifecycleKey()).isEqualTo(LifecycleKey.PENDING);
        assertThat(forwarded.getCacheContext().getYy()).isEqualTo(FIXTURE_YEAR);
        assertThat(forwarded.getCacheContext().getMnth()).isEqualTo(FIXTURE_MONTH);
        assertThat(forwarded.getCacheContext().getWeekStartDt()).isEqualTo(FIXTURE_WEEK_START_DATE);
    }

    /** 관리자 역할도 라이프사이클 설정 API를 호출할 수 있다. */
    @Test
    @WithMockUser(roles = "MNGR")
    void managerSetsLifecycle() throws Exception {
        when(lifecycleService.set(any(LifecycleSetDto.class))).thenReturn(successResponse());

        mockMvc.perform(put("/api/lifecycles")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(true));

        verify(lifecycleService).set(any(LifecycleSetDto.class));
    }

    /** 사용자·관리자 역할이 없는 인증 사용자는 라이프사이클 API에 접근할 수 없다. */
    @Test
    @WithMockUser(authorities = "fixture.read")
    void unrelatedAuthorityCannotSetLifecycle() throws Exception {
        mockMvc.perform(put("/api/lifecycles")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(lifecycleService);
    }

    /** 원본 콘텐츠 소유권이 없으면 공통 예외 처리기가 HTTP 403 응답으로 변환한다. */
    @Test
    @WithMockUser(roles = "USER")
    void ownershipFailureReturnsForbidden() throws Exception {
        when(lifecycleService.set(any(LifecycleSetDto.class)))
                .thenThrow(new NotAuthorizedException("common.result.access-not-authorized"));

        mockMvc.perform(put("/api/lifecycles")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.rslt").value(false))
                .andExpect(jsonPath("$.status").value(403));

        verify(lifecycleService).set(any(LifecycleSetDto.class));
    }

    /** 서비스가 규칙 위반을 결과로 반환하면 컨트롤러는 메시지와 실패 여부를 보존한다. */
    @Test
    @WithMockUser(roles = "USER")
    void serviceFailureReturnsFailureResponse() throws Exception {
        final String failureMessage = "Fixture lifecycle rule violation";
        when(lifecycleService.set(any(LifecycleSetDto.class))).thenReturn(
                ServiceResponse.builder()
                        .rslt(false)
                        .message(failureMessage)
                        .build()
        );

        mockMvc.perform(put("/api/lifecycles")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(false))
                .andExpect(jsonPath("$.message").value(failureMessage));

        verify(lifecycleService).set(any(LifecycleSetDto.class));
    }

    private LifecycleSetDto request() {
        return LifecycleSetDto.builder()
                .id(FIXTURE_CONTENT_ID)
                .contentType(ContentType.JOURNAL_DIARY)
                .lifecycleKey(LifecycleKey.PENDING)
                .cacheContext(AttachableCacheContext.builder()
                        .yy(FIXTURE_YEAR)
                        .mnth(FIXTURE_MONTH)
                        .weekStartDt(FIXTURE_WEEK_START_DATE)
                        .build())
                .build();
    }

    private ServiceResponse successResponse() {
        return ServiceResponse.builder()
                .rslt(true)
                .rsltObj(Map.of(
                        "previousLifecycleKey", "OPEN",
                        "currentLifecycleKey", "PENDING"
                ))
                .build();
    }
}
