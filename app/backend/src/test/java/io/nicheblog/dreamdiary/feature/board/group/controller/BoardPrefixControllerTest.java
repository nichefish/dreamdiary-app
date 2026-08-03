package io.nicheblog.dreamdiary.feature.board.group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.handler.SecurityErrorResponseWriter;
import io.nicheblog.dreamdiary.auth.security.matcher.PublicApiRequestMatcher;
import io.nicheblog.dreamdiary.auth.security.service.AuthSessionPolicyService;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardPrefixManagementDto;
import io.nicheblog.dreamdiary.feature.board.group.service.BoardPrefixService;
import io.nicheblog.dreamdiary.feature.board.group.service.BoardService;
import io.nicheblog.dreamdiary.feature.board.post.controller.BoardPostRestController;
import io.nicheblog.dreamdiary.feature.board.post.service.BoardPostService;
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
 * 게시판 Prefix 사용자·관리자 API의 HTTP 및 권한 경계 테스트.
 *
 * @author nichefish
 */
@WebMvcTest(controllers = {BoardRestController.class, BoardPostRestController.class})
@Import({WebMvcTestSliceSupportConfig.class, BoardPrefixControllerTest.MethodSecurityTestConfig.class})
@ActiveProfiles("test")
class BoardPrefixControllerTest {

    private static final Integer FIXTURE_BOARD_ID = 31;
    private static final Integer FIXTURE_PREFIX_ID = 41;
    private static final String FIXTURE_BOARD_KEY = "FIXTURE_BOARD";
    private static final String FIXTURE_BOARD_NAME = "Fixture Board";
    private static final String FIXTURE_PREFIX_NAME = "Fixture Prefix";

    /** MVC 슬라이스에서도 운영과 같은 {@code @Secured}/{@code @PreAuthorize} 평가를 활성화한다. */
    @TestConfiguration
    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, proxyTargetClass = true)
    static class MethodSecurityTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;
    @MockBean
    private BoardPostService boardPostService;
    @MockBean
    private BoardPrefixService boardPrefixService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private AuthSessionPolicyService authSessionPolicyService;
    @MockBean
    private SecurityErrorResponseWriter securityErrorResponseWriter;
    @MockBean
    private PublicApiRequestMatcher publicApiRequestMatcher;

    /** 일반 사용자는 게시판별 활성 Prefix 목록을 조회한다. */
    @Test
    @WithMockUser(roles = "USER")
    void userGetsActivePrefixesForBoard() throws Exception {
        when(boardPrefixService.getActive(FIXTURE_BOARD_KEY)).thenReturn(List.of(prefix()));

        mockMvc.perform(get("/api/board/{boardKey}/prefixes", FIXTURE_BOARD_KEY)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(true))
                .andExpect(jsonPath("$.rsltList[0].id").value(FIXTURE_PREFIX_ID))
                .andExpect(jsonPath("$.rsltList[0].name").value(FIXTURE_PREFIX_NAME));

        verify(boardPrefixService).getActive(FIXTURE_BOARD_KEY);
    }

    /** 사용자·관리자 역할이 없는 인증 사용자는 게시판 Prefix 선택지에 접근할 수 없다. */
    @Test
    @WithMockUser(authorities = "fixture.read")
    void unrelatedAuthorityCannotGetActivePrefixes() throws Exception {
        mockMvc.perform(get("/api/board/{boardKey}/prefixes", FIXTURE_BOARD_KEY)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(boardPrefixService);
    }

    /** 게시판 관리 권한은 boardKey와 비활성 항목을 포함한 관리 정보를 조회한다. */
    @Test
    @WithMockUser(authorities = "menu.admin.board")
    void boardAdminGetsPrefixManagement() throws Exception {
        when(boardPrefixService.getManagement(FIXTURE_BOARD_ID)).thenReturn(BoardPrefixManagementDto.builder()
                .boardId(FIXTURE_BOARD_ID)
                .boardKey(FIXTURE_BOARD_KEY)
                .boardName(FIXTURE_BOARD_NAME)
                .prefixes(List.of(prefix()))
                .build());

        mockMvc.perform(get("/api/board/groups/{id}/prefixes", FIXTURE_BOARD_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(true))
                .andExpect(jsonPath("$.rsltObj.boardId").value(FIXTURE_BOARD_ID))
                .andExpect(jsonPath("$.rsltObj.boardKey").value(FIXTURE_BOARD_KEY))
                .andExpect(jsonPath("$.rsltObj.prefixes[0].id").value(FIXTURE_PREFIX_ID));

        verify(boardPrefixService).getManagement(FIXTURE_BOARD_ID);
    }

    /** 게시판 관리 등록 API는 JSON Prefix 요청을 게시판 ID Scope로 전달한다. */
    @Test
    @WithMockUser(authorities = "menu.admin.board")
    void boardAdminCreatesPrefix() throws Exception {
        when(boardPrefixService.create(eq(FIXTURE_BOARD_ID), any(PrefixDto.class))).thenReturn(prefix());

        mockMvc.perform(post("/api/board/groups/{id}/prefixes", FIXTURE_BOARD_ID)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prefixRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rsltObj.id").value(FIXTURE_PREFIX_ID));

        verify(boardPrefixService).create(eq(FIXTURE_BOARD_ID), any(PrefixDto.class));
    }

    /** 게시판 관리 수정 API는 게시판 ID와 Prefix ID를 함께 전달한다. */
    @Test
    @WithMockUser(authorities = "menu.admin.board")
    void boardAdminUpdatesPrefix() throws Exception {
        when(boardPrefixService.update(
                eq(FIXTURE_BOARD_ID), eq(FIXTURE_PREFIX_ID), any(PrefixDto.class)))
                .thenReturn(prefix());

        mockMvc.perform(put("/api/board/groups/{id}/prefixes/{prefixId}",
                        FIXTURE_BOARD_ID, FIXTURE_PREFIX_ID)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prefixRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rsltObj.id").value(FIXTURE_PREFIX_ID));

        verify(boardPrefixService).update(
                eq(FIXTURE_BOARD_ID), eq(FIXTURE_PREFIX_ID), any(PrefixDto.class));
    }

    /** 게시판 관리 활성 상태 API는 요청한 boolean 값을 Scope 서비스로 전달한다. */
    @Test
    @WithMockUser(authorities = "menu.admin.board")
    void boardAdminChangesPrefixActiveState() throws Exception {
        mockMvc.perform(patch("/api/board/groups/{id}/prefixes/{prefixId}/active",
                        FIXTURE_BOARD_ID, FIXTURE_PREFIX_ID)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(true));

        verify(boardPrefixService).setActive(FIXTURE_BOARD_ID, FIXTURE_PREFIX_ID, false);
    }

    /** 게시판 관리 권한이 없으면 관리자 Prefix API를 호출할 수 없다. */
    @Test
    @WithMockUser(roles = "MNGR")
    void managerWithoutBoardAuthorityCannotManagePrefixes() throws Exception {
        mockMvc.perform(get("/api/board/groups/{id}/prefixes", FIXTURE_BOARD_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(boardPrefixService);
    }

    /** Prefix 요청의 이름·정렬 계약을 만족하지 않으면 서비스 호출 전에 거부한다. */
    @Test
    @WithMockUser(authorities = "menu.admin.board")
    void boardAdminCreateRejectsInvalidPrefixPayload() throws Exception {
        final PrefixDto invalidRequest = prefixRequest();
        invalidRequest.setName(" ");

        mockMvc.perform(post("/api/board/groups/{id}/prefixes", FIXTURE_BOARD_ID)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(boardPrefixService, never()).create(eq(FIXTURE_BOARD_ID), any(PrefixDto.class));
    }

    private PrefixDto prefixRequest() {
        return PrefixDto.builder()
                .name(FIXTURE_PREFIX_NAME)
                .color("#6B7280")
                .sortOrder(0)
                .build();
    }

    private PrefixDto prefix() {
        return PrefixDto.builder()
                .id(FIXTURE_PREFIX_ID)
                .name(FIXTURE_PREFIX_NAME)
                .color("#6B7280")
                .sortOrder(0)
                .activeYn("Y")
                .build();
    }
}
