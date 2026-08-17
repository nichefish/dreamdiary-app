package io.nicheblog.dreamdiary.feature.journal.setting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.feature.journal.setting.model.JournalUserSettingDto;
import io.nicheblog.dreamdiary.feature.journal.setting.service.JournalSettingService;
import io.nicheblog.dreamdiary.feature.journal.setting.type.JournalDefaultEntryView;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.annotation.Secured;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 사용자별 저널 설정 API의 요청·응답·역할 계약 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class JournalSettingRestControllerTest {

    @Mock
    private JournalSettingService journalSettingService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new JournalSettingRestController(journalSettingService))
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    /** 조회 응답은 사용자의 기본 진입 화면을 rsltObj에 담는다. */
    @Test
    void getMySettingsReturnsResolvedView() throws Exception {
        when(journalSettingService.getMySetting()).thenReturn(setting(JournalDefaultEntryView.DAILY));

        try (final MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getMessage("common.result.success")).thenReturn("success");

            mockMvc.perform(get(Url.JOURNAL_MY_SETTINGS).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rslt").value(true))
                    .andExpect(jsonPath("$.rsltObj.defaultEntryView").value("DAILY"));
        }

        verify(journalSettingService).getMySetting();
    }

    /** 유효한 기본 진입 화면은 서비스에 전달하고 저장된 값을 응답한다. */
    @Test
    void updateMySettingsAcceptsSupportedView() throws Exception {
        when(journalSettingService.updateMySetting(any(JournalUserSettingDto.class)))
                .thenReturn(setting(JournalDefaultEntryView.WEEKLY));

        try (final MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getMessage("common.result.success")).thenReturn("success");

            mockMvc.perform(put(Url.JOURNAL_MY_SETTINGS)
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(setting(JournalDefaultEntryView.WEEKLY))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rsltObj.defaultEntryView").value("WEEKLY"));
        }

        verify(journalSettingService).updateMySetting(any(JournalUserSettingDto.class));
    }

    /** 기본 진입 화면이 없는 요청은 서비스 호출 전에 HTTP 400으로 거절한다. */
    @Test
    void updateMySettingsRejectsMissingView() throws Exception {
        mockMvc.perform(put(Url.JOURNAL_MY_SETTINGS)
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(journalSettingService);
    }

    /** 사용자 설정 조회·저장은 일반 사용자와 관리자 역할에 같은 권한을 부여한다. */
    @Test
    void mySettingEndpointsAllowUserAndManagerRoles() throws Exception {
        assertRoles("getMySettings");
        assertRoles("updateMySettings", JournalUserSettingDto.class);
    }

    private void assertRoles(final String methodName, final Class<?>... parameterTypes) throws Exception {
        final Method method = JournalSettingRestController.class.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(Secured.class).value())
                .containsExactlyInAnyOrder(Constant.ROLE_USER, Constant.ROLE_MNGR);
    }

    private JournalUserSettingDto setting(final JournalDefaultEntryView view) {
        return JournalUserSettingDto.builder()
                .defaultEntryView(view)
                .build();
    }
}
