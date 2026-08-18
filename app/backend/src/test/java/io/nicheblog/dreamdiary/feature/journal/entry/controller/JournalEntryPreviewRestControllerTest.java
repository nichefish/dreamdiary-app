package io.nicheblog.dreamdiary.feature.journal.entry.controller;

import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPreviewRequest;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 작성 중 본문 미리보기 API 계약 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class JournalEntryPreviewRestControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new JournalEntryPreviewRestController()).build();
    }

    /** 미저장 HTML은 MarkdownUtils를 거쳐 markdownContent로 돌아온다. */
    @Test
    void previewRendersDialogMarkup() throws Exception {
        try (final MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getMessage("common.result.success")).thenReturn("success");

            mockMvc.perform(post(Url.JOURNAL_ENTRY_PREVIEW)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"<p>\\\"hello\\\"</p>\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rslt").value(true))
                    .andExpect(jsonPath("$.rsltObj.markdownContent").value(org.hamcrest.Matchers.containsString("md-text-dialog")));
        }
    }

    /** 본문이 비어도 저장 없이 빈 렌더를 반환한다. */
    @Test
    void previewAcceptsEmptyContent() throws Exception {
        try (final MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getMessage("common.result.success")).thenReturn("success");

            mockMvc.perform(post(Url.JOURNAL_ENTRY_PREVIEW)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rslt").value(true))
                    .andExpect(jsonPath("$.rsltObj.markdownContent").exists());
        }
    }

    /** 미리보기는 일반 사용자와 관리자 역할에 같은 권한을 부여한다. */
    @Test
    void previewAllowsUserAndManagerRoles() throws Exception {
        final Method method = JournalEntryPreviewRestController.class.getMethod(
                "preview",
                JournalEntryPreviewRequest.class
        );
        assertThat(method.getAnnotation(Secured.class).value())
                .containsExactlyInAnyOrder(Constant.ROLE_USER, Constant.ROLE_MNGR);
    }
}
