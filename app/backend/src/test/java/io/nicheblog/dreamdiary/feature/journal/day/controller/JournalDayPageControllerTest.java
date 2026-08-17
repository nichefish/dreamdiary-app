package io.nicheblog.dreamdiary.feature.journal.day.controller;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.handler.SecurityErrorResponseWriter;
import io.nicheblog.dreamdiary.auth.security.matcher.PublicApiRequestMatcher;
import io.nicheblog.dreamdiary.auth.security.service.AuthSessionPolicyService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.web.config.WebMvcTestSliceSupportConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JournalDayPageControllerTest
 * <pre>
 *  저널 일자 제품 화면 URL과 현재 프론트엔드 연결 계약을 검증한다.
 * </pre>
 *
 * @author nichefish
 */
@WebMvcTest(JournalDayPageController.class)
@Import(WebMvcTestSliceSupportConfig.class)
@ActiveProfiles("test")
class JournalDayPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthSessionPolicyService authSessionPolicyService;

    @MockBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;

    @MockBean
    private PublicApiRequestMatcher publicApiRequestMatcher;

    /** 제품 화면 URL이 현재 Vue 기본 보기 해석 route로 연결되는지 검증한다. */
    @Test
    @WithMockUser(roles = "USER")
    void journalDayHomeRedirectsToActiveFrontend() throws Exception {
        mockMvc.perform(get(Url.JOURNAL_DAY_HOME))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vue-app/journal/day/home"));
    }
}
