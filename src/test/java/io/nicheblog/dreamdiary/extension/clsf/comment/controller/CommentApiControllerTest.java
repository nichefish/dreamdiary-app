package io.nicheblog.dreamdiary.extension.clsf.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.extension.clsf.comment.controller.CommentRestController;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.CommentDto;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.CommentSearchParam;
import io.nicheblog.dreamdiary.extension.clsf.comment.service.CommentService;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
@WebMvcTest(CommentRestController.class)
@ActiveProfiles("test")
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

    /**
     * TEST:: boardCommentListAjax
     * 목록 조회
     * TODO: 케이스별로 쪼개야 하나??? 얼마나??
     */
    @Test
    public void test_boardCommentListAjax() throws Exception {
        /* 준비arrange = 테스트에 필요한 조건 및 데이터 준비 */
        // 헬퍼 메소드 호출 (mock객체 또는 구체적인 인스턴스)
        Boolean onlyMock = false;
        Page<CommentDto> mockPage = createMockPage(onlyMock);

        /* 실행조건when: 모킹 프레임워크 사용시 처리하는 단계. (준비arrange의 일부) */
        // "you can mock the service's response here."
        when(commentService.getPageDto(new CommentSearchParam(), Pageable.unpaged()))
            .thenReturn(mockPage);
        
        /* 실행act & 검증assert */
        // 이 부분에서는 실제로 컨트롤러의 특정 경로에 대한 요청을 보내고 (Act), 그 결과를 검증합니다 (Assert). */
        mockMvc.perform(get(Url.COMMENT_LIST_AJAX))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.rslt").value(true))
               .andExpect(jsonPath("$.rsltList").value(mockPage));
    }

    @Test
    public void testCommentListAjax_WithInvalidParameters() throws Exception {
        mockMvc.perform(get(Url.COMMENT_LIST_AJAX)
                           .param("invalidParam", "invalidValue"))
                .andExpect(status().isBadRequest());

        // 잘못된 페이지 번호 (예: 음수)와 페이지 크기를 파라미터로 전달
        mockMvc.perform(get(Url.COMMENT_LIST_AJAX)
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest()); // 잘못된 요청에 대한 HTTP 상태 400 (Bad Request)을 기대

        // 잘못된 페이지 크기 (예: 너무 크거나 작은 값)를 파라미터로 전달
        mockMvc.perform(get(Url.COMMENT_LIST_AJAX)
                        .param("page", "0")
                        .param("size", "1000")) // 페이지 크기가 너무 큼
                .andExpect(status().isBadRequest());
    }




    @Test
    public void testCommentRegAjax() throws Exception {
        // Arrange
        CommentDto boardCommentDto = new CommentDto();
        // Initialize the DTO as needed
        // when(boardCommentService.regist(...)).thenReturn(...);

        // Act & Assert
        mockMvc.perform(post(Url.COMMENT_REG_AJAX) // Replace with actual URL
                            .contentType(ContentType.APPLICATION_JSON.toString())
                            .content(asJsonString(boardCommentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.someField").value("expectedValue")); // Replace with actual JSON path and value
    }

    @Test
    public void testCommentMdfAjax() throws Exception {
        // Arrange
        CommentDto boardCommentDto = new CommentDto();
        // Initialize the DTO as needed
        // when(boardCommentService.regist(...)).thenReturn(...);

        // Act & Assert
        mockMvc.perform(post(Url.COMMENT_MDF_AJAX) // Replace with actual URL
                            .contentType(ContentType.APPLICATION_JSON.toString())
                            .content(asJsonString(boardCommentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.someField").value("expectedValue")); // Replace with actual JSON path and value
    }

    @Test
    public void testCommentDelAjax() throws Exception {
        // Arrange
        String commentNo = "1";
        /* when: you can mock the service's response here */
        // when(boardCommentService.delete(...)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post(Url.COMMENT_DEL_AJAX) // Replace with actual URL
                .param("commentNo", commentNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.someField").value("expectedValue")); // Replace with actual JSON path and value
    }

    // Helper method to convert object to JSON string
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to create Page<CommentDto>
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
                    setPostNo(101);
                    setRefPostNo(1001);
                    setRefContentType("BC001");
                    setCn("첫 번째 댓글 내용입니다.");
                    setIsSuccess(true);
                }},
                new CommentDto() {{
                    setRnum(2L);
                    setPostNo(102);
                    setRefPostNo(1002);
                    setRefContentType("BC001");
                    setCn("두 번째 댓글 내용입니다.");
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