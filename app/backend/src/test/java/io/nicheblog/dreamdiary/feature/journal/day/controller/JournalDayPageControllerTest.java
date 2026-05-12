package io.nicheblog.dreamdiary.feature.journal.day.controller;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.model.AuthInfoTestFactory;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import io.nicheblog.dreamdiary.infrastructure.freemarker.config.TestFreemarkerConfig;
import io.nicheblog.dreamdiary.infrastructure.web.config.WebMvcTestSliceSupportConfig;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerTestHelper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JournalDayControllerTest
 * <pre>
 *  저널 일자 컨트롤러 테스트 모듈
 * </pre>
 *
 * JWT 필터 체인이 슬라이스에 올라올 때 {@link JwtTokenProvider} 빈이 필요하므로 {@literal @}MockBean 으로 채운다.
 *
 * @author nichefish
 */
@WebMvcTest(JournalDayPageController.class)
@Import({ TestFreemarkerConfig.class, WebMvcTestSliceSupportConfig.class })
@ActiveProfiles("test")
@Log4j2
class JournalDayPageControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean(name = "journalDayService")
    private JournalDayService journalDayService;
    @MockBean(name = "journalDayRepository")
    private JournalDayRepository journalDayRepository;
    @MockBean
    private ApplicationEventPublisher publisher;
    @MockBean(name = "messageSource")
    private MessageSource messageSource;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean(name = "codeLookupService")
    private CodeLookupService codeLookupService;

    @BeforeEach
    void setUp() {
        // Stubbing here
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Expected Message");
        doNothing().when(codeLookupService).setCdListToModel(anyString(), any());
    }

    /**
     * 저널 일자 페이지 테스트
     */
    @Test
    @WithMockUser
    void journalDayPage() throws Exception {

        AuthInfo authInfo = AuthInfoTestFactory.createAuthInfo();

        // 월간 뷰 렌더링 분기: yy·mnth 미지정 시 리다이렉트(302) → 고정 파라미터로 200 응답 경로를 검증한다.
        MvcResult result = this.mockMvc.perform(get(Url.JOURNAL_DAY_MONTHLY)
                        .param("yy", "2026")
                        .param("mnth", "5")
                        .sessionAttr("authInfo", authInfo))  // 세션 어트리뷰트 추가
                .andExpect(status().isOk())
                .andReturn();
                // .andDo(document("journalDayPage"));

        String viewName = Objects.requireNonNull(result.getModelAndView()).getViewName();
        assertNotNull(viewName, "View name is null");
        assertTrue(BaseControllerTestHelper.viewFileExists(viewName), "View template file does not exist: " + viewName);
    }

    // mockMvc.perform(get(Url.JOURNAL_DAY_MONTHLY)
    // .sessionAttr("someAttribute", "value") // 세션 어트리뷰트 추가
    // .param("paramName", "paramValue")     // 요청 파라미터 추가
    // .header("headerName", "headerValue")) // 헤더 추가
    //         .andExpect(status().isOk())
    //         .andDo(document("journalDayPage"));



}

