package io.nicheblog.dreamdiary.feature.user.my.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.nicheblog.dreamdiary.feature.user.account.service.UserService;
import io.nicheblog.dreamdiary.feature.user.my.model.UserMyUpdateRequest;
import io.nicheblog.dreamdiary.feature.user.my.service.UserMyService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 내 정보 수정 API 요청 경계 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class UserMyRestControllerTest {

    private static final String FIXTURE_NICKNAME = "Alice";

    @Mock
    private UserMyService userMyService;
    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserMyRestController(userMyService, userService))
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void modifyMyInfoAcceptsPersonalProfilePayload() throws Exception {
        when(userMyService.modifyMyInfo(any(UserMyUpdateRequest.class))).thenReturn(true);

        try (final MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getMessage("common.result.success")).thenReturn("success");

            mockMvc.perform(put(Url.USER_MY_INFO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rslt").value(true));
        }

        verify(userMyService).modifyMyInfo(any(UserMyUpdateRequest.class));
    }

    @Test
    void modifyMyInfoRejectsBlankNickname() throws Exception {
        final UserMyUpdateRequest request = validRequest();
        request.setNickname(" ");

        mockMvc.perform(put(Url.USER_MY_INFO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userMyService);
    }

    @Test
    void modifyMyInfoRejectsFutureBirthDate() throws Exception {
        final UserMyUpdateRequest request = validRequest();
        request.setBrthdy(LocalDate.now().plusDays(1));

        mockMvc.perform(put(Url.USER_MY_INFO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userMyService);
    }

    private UserMyUpdateRequest validRequest() {
        final UserMyUpdateRequest request = new UserMyUpdateRequest();
        request.setNickname(FIXTURE_NICKNAME);
        request.setPhoneNumber("010-0000-0000");
        request.setBrthdy(LocalDate.of(2000, 1, 1));
        request.setLunarYn("N");
        request.setProflCn("가상 사용자 소개");
        return request;
    }
}
