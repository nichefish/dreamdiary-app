package io.nicheblog.dreamdiary.feature.attachable.comment.controller;

import io.nicheblog.dreamdiary.feature.attachable.comment.model.CommentDto;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.CommentSearchParam;
import io.nicheblog.dreamdiary.feature.attachable.comment.service.CommentService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CommentControllerTest
 * <pre>
 *  댓글 컨트롤러 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
public class CommentApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentRestController commentApiController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentApiController).build();
    }

    @Test
    void commentListReturnsPagedContent() throws Exception {
        final Page<CommentDto> mockPage = createMockPage(false);
        when(commentService.getPageDto(any(CommentSearchParam.class), any(Pageable.class)))
                .thenReturn(mockPage);

        try (final MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getMessage("common.result.success")).thenReturn("success");

            mockMvc.perform(get(Url.COMMENTS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rslt").value(true))
                    .andExpect(jsonPath("$.rsltList.length()").value(2))
                    .andExpect(jsonPath("$.rsltList[0].id").value(101));
        }

        verify(commentService).getPageDto(any(CommentSearchParam.class), any(Pageable.class));
    }

    @Test
    void commentRegistrationAcceptsMultipartForm() throws Exception {
        final CommentDto savedComment = CommentDto.builder().id(101).content("댓글 내용").build();
        when(commentService.regist(any(CommentDto.class), any(MultipartHttpServletRequest.class)))
                .thenReturn(ServiceResponse.builder().rslt(true).rsltObj(savedComment).build());

        try (final MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getMessage("common.result.success")).thenReturn("success");

            mockMvc.perform(multipart(Url.COMMENTS)
                            .param("content", "댓글 내용")
                            .param("refId", "1001")
                            .param("refContentType", "BC001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rslt").value(true))
                    .andExpect(jsonPath("$.rsltObj.id").value(101));
        }

        verify(commentService).regist(any(CommentDto.class), any(MultipartHttpServletRequest.class));
    }

    @Test
    void commentModificationAcceptsMultipartForm() throws Exception {
        final CommentDto savedComment = CommentDto.builder().id(101).content("수정된 댓글").build();
        when(commentService.modify(any(CommentDto.class), any(MultipartHttpServletRequest.class)))
                .thenReturn(ServiceResponse.builder().rslt(true).rsltObj(savedComment).build());

        try (final MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getMessage("common.result.success")).thenReturn("success");

            mockMvc.perform(multipart(Url.COMMENT, 101)
                            .param("id", "101")
                            .param("content", "수정된 댓글")
                            .param("refId", "1001")
                            .param("refContentType", "BC001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rslt").value(true))
                    .andExpect(jsonPath("$.rsltObj.id").value(101));
        }

        verify(commentService).modify(any(CommentDto.class), any(MultipartHttpServletRequest.class));
    }

    @Test
    void commentDetailReturnsRequestedComment() throws Exception {
        when(commentService.getDtlDto(101))
                .thenReturn(CommentDto.builder().id(101).content("댓글 내용").build());

        try (final MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getMessage("common.result.success")).thenReturn("success");

            mockMvc.perform(get(Url.COMMENT, 101))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rslt").value(true))
                    .andExpect(jsonPath("$.rsltObj.id").value(101));
        }

        verify(commentService).getDtlDto(101);
    }

    @Test
    void commentDeletionUsesResourceIdentifier() throws Exception {
        when(commentService.delete(101))
                .thenReturn(ServiceResponse.builder().rslt(true).build());

        try (final MockedStatic<MessageUtils> messages = mockStatic(MessageUtils.class)) {
            messages.when(() -> MessageUtils.getMessage("common.result.success")).thenReturn("success");

            mockMvc.perform(delete(Url.COMMENT, 101))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rslt").value(true));
        }

        verify(commentService).delete(101);
    }

    /**
     * 댓글 목록 응답 검증에 사용할 페이지 픽스처를 생성한다.
     *
     * @param onlyMock 데이터 없는 Page 목 사용 여부
     * @return 댓글 페이지 픽스처
     */
    public static Page<CommentDto> createMockPage(Boolean onlyMock) {
        // 1. mock을 써서 간소화 객체 생성
        // mock을 써서 생성된 목 객체는 해당 유형의 응답이 온다는 것만 체크하고 실제 데이터나 동작을 포함하지 않으므로, json응답의 필드값을 검증할 수 없다.
        if (onlyMock) return mock(Page.class);
        // 2. 실제 구체적인 응답 데이터 생성
        // json응답의 필드값을 검증하려면? 구체적인 응답 객체를 만들어야 한다.
        // "실제 사용 사례를 반영하는 테스트 데이터를 사용하면 테스트의 유효성이 높아집니다."
        List<CommentDto> testData = Arrays.asList(
                new CommentDto() {{
                    setRnum(1L);
                    setId(101);
                    setRefId(1001);
                    setRefContentType("BC001");
                    setContent("첫 번째 댓글 내용입니다.");
                    setIsSuccess(true);
                }},
                new CommentDto() {{
                    setRnum(2L);
                    setId(102);
                    setRefId(1002);
                    setRefContentType("BC001");
                    setContent("두 번째 댓글 내용입니다.");
                    setIsSuccess(true);
                }}
                // 필요한 만큼 더 추가할 수 있습니다.
        );
        // 페이징 정보 설정 (예: 첫 번째 페이지, 페이지 당 10개 항목)
        PageRequest pageRequest = PageRequest.of(0, 10);
        // PageImpl 객체 생성

        return new PageImpl<>(testData, pageRequest, testData.size());
    }


}
