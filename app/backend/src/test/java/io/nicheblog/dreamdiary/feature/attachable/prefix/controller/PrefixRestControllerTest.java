package io.nicheblog.dreamdiary.feature.attachable.prefix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.handler.SecurityErrorResponseWriter;
import io.nicheblog.dreamdiary.auth.security.matcher.PublicApiRequestMatcher;
import io.nicheblog.dreamdiary.auth.security.service.AuthSessionPolicyService;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 개인 Prefix API의 HTTP·검증·역할 경계 테스트.
 *
 * @author nichefish
 */
@WebMvcTest(PrefixRestController.class)
@Import({WebMvcTestSliceSupportConfig.class, PrefixRestControllerTest.MethodSecurityTestConfig.class})
@ActiveProfiles("test")
class PrefixRestControllerTest {

    private static final Integer FIXTURE_PREFIX_ID = 51;
    private static final String FIXTURE_CONTENT_TYPE = "JOURNAL_THREAD";
    private static final String FIXTURE_PREFIX_NAME = "Fixture Personal Prefix";

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
    private PrefixService prefixService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private AuthSessionPolicyService authSessionPolicyService;
    @MockBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;
    @MockBean
    private PublicApiRequestMatcher publicApiRequestMatcher;

    /** 일반 사용자는 contentType별 비활성 포함 개인 Prefix 관리 목록을 조회한다. */
    @Test
    @WithMockUser(roles = "USER")
    void userGetsAllPersonalPrefixes() throws Exception {
        when(prefixService.getMine(FIXTURE_CONTENT_TYPE)).thenReturn(List.of(prefix("N")));

        mockMvc.perform(get("/api/my/prefixes")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("contentType", FIXTURE_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(true))
                .andExpect(jsonPath("$.rsltList[0].id").value(FIXTURE_PREFIX_ID))
                .andExpect(jsonPath("$.rsltList[0].activeYn").value("N"));

        verify(prefixService).getMine(FIXTURE_CONTENT_TYPE);
    }

    /** 일반 사용자는 편집·검색에서 사용할 활성 개인 Prefix 선택지를 조회한다. */
    @Test
    @WithMockUser(roles = "USER")
    void userGetsActivePersonalPrefixOptions() throws Exception {
        when(prefixService.getActiveMine(FIXTURE_CONTENT_TYPE)).thenReturn(List.of(prefix("Y")));

        mockMvc.perform(get("/api/my/prefixes/options")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("contentType", FIXTURE_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rsltList[0].id").value(FIXTURE_PREFIX_ID))
                .andExpect(jsonPath("$.rsltList[0].activeYn").value("Y"));

        verify(prefixService).getActiveMine(FIXTURE_CONTENT_TYPE);
    }

    /** 관리자 역할도 개인 Prefix 선택지 API를 사용할 수 있다. */
    @Test
    @WithMockUser(roles = "MNGR")
    void managerGetsActivePersonalPrefixOptions() throws Exception {
        when(prefixService.getActiveMine(FIXTURE_CONTENT_TYPE)).thenReturn(List.of(prefix("Y")));

        mockMvc.perform(get("/api/my/prefixes/options")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("contentType", FIXTURE_CONTENT_TYPE))
                .andExpect(status().isOk());

        verify(prefixService).getActiveMine(FIXTURE_CONTENT_TYPE);
    }

    /** 개인 Prefix 등록 API는 contentType과 JSON 요청을 서비스에 전달한다. */
    @Test
    @WithMockUser(roles = "USER")
    void userCreatesPersonalPrefix() throws Exception {
        when(prefixService.create(eq(FIXTURE_CONTENT_TYPE), any(PrefixDto.class)))
                .thenReturn(prefix("Y"));

        mockMvc.perform(post("/api/my/prefixes")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("contentType", FIXTURE_CONTENT_TYPE)
                        .content(objectMapper.writeValueAsString(prefixRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rsltObj.id").value(FIXTURE_PREFIX_ID));

        verify(prefixService).create(eq(FIXTURE_CONTENT_TYPE), any(PrefixDto.class));
    }

    /** 개인 Prefix 수정 API는 contentType과 Prefix ID를 함께 전달한다. */
    @Test
    @WithMockUser(roles = "USER")
    void userUpdatesPersonalPrefix() throws Exception {
        when(prefixService.update(
                eq(FIXTURE_CONTENT_TYPE), eq(FIXTURE_PREFIX_ID), any(PrefixDto.class)))
                .thenReturn(prefix("Y"));

        mockMvc.perform(put("/api/my/prefixes/{prefixId}", FIXTURE_PREFIX_ID)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("contentType", FIXTURE_CONTENT_TYPE)
                        .content(objectMapper.writeValueAsString(prefixRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rsltObj.id").value(FIXTURE_PREFIX_ID));

        verify(prefixService).update(
                eq(FIXTURE_CONTENT_TYPE), eq(FIXTURE_PREFIX_ID), any(PrefixDto.class));
    }

    /** 개인 Prefix 활성 상태 API는 contentType·Prefix ID·boolean 값을 함께 전달한다. */
    @Test
    @WithMockUser(roles = "USER")
    void userChangesPersonalPrefixActiveState() throws Exception {
        mockMvc.perform(patch("/api/my/prefixes/{prefixId}/active", FIXTURE_PREFIX_ID)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .param("contentType", FIXTURE_CONTENT_TYPE)
                        .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(true));

        verify(prefixService).setActive(FIXTURE_CONTENT_TYPE, FIXTURE_PREFIX_ID, false);
    }

    /** 사용자·관리자 역할이 없는 인증 사용자는 개인 Prefix API에 접근할 수 없다. */
    @Test
    @WithMockUser(authorities = "fixture.read")
    void unrelatedAuthorityCannotAccessPersonalPrefixes() throws Exception {
        mockMvc.perform(get("/api/my/prefixes")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("contentType", FIXTURE_CONTENT_TYPE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(prefixService);
    }

    /** 잘못된 Prefix 요청은 서비스 호출 전에 Bean Validation으로 거부한다. */
    @Test
    @WithMockUser(roles = "USER")
    void createRejectsInvalidPrefixPayload() throws Exception {
        final PrefixDto invalidRequest = prefixRequest();
        invalidRequest.setSortOrder(-1);

        mockMvc.perform(post("/api/my/prefixes")
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("contentType", FIXTURE_CONTENT_TYPE)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(prefixService, never()).create(eq(FIXTURE_CONTENT_TYPE), any(PrefixDto.class));
    }

    private PrefixDto prefixRequest() {
        return PrefixDto.builder()
                .name(FIXTURE_PREFIX_NAME)
                .color("#6B7280")
                .sortOrder(0)
                .build();
    }

    private PrefixDto prefix(final String activeYn) {
        return PrefixDto.builder()
                .id(FIXTURE_PREFIX_ID)
                .name(FIXTURE_PREFIX_NAME)
                .color("#6B7280")
                .sortOrder(0)
                .activeYn(activeYn)
                .build();
    }
}
