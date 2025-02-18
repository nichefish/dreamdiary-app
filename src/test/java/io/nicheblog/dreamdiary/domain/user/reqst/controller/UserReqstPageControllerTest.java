package io.nicheblog.dreamdiary.domain.user.reqst.controller;

import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDto;
import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDtoTestFactory;
import io.nicheblog.dreamdiary.domain.user.reqst.service.UserReqstService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.extension.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.global.intrfc.controller.BaseControllerTestHelper;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserReqstControllerTest
 * <pre>
 *  사용자 계정 신청 컨트롤러 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@WebMvcTest(UserReqstPageController.class)
@ActiveProfiles("test")
class UserReqstPageControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean(name = "userReqstService")
    private UserReqstService userReqstService;
    @MockBean(name = "dtlCdService")
    private DtlCdService dtlCdService;

    @BeforeEach
    public void setup(final WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 신규계정 등록 화면 조회 Test
     */
    @Test
    public void testUserReqstRegForm() throws Exception {
        // given::

        // when::
        final MvcResult result = mockMvc.perform(get(Url.USER_REQST_REG_FORM))
                .andExpect(status().isOk())
                .andReturn();

        verify(dtlCdService, times(5)).setCdListToModel(anyString(), any());

        // then::
        final String viewName = Objects.requireNonNull(result.getModelAndView()).getViewName();
        assertNotNull(viewName, "View name is null");
        assertTrue(BaseControllerTestHelper.viewFileExists(viewName), "View template file does not exist: " + viewName);
    }

    /**
     * 신규계정 등록 (Ajax) Test
     */
    @Test
    public void testUserReqstRegAjax() throws Exception {
        // given::
        // 요청 객체 생성
        final UserReqstDto userReqstDto = UserReqstDtoTestFactory.create();
        final String userReqstJsonContent = UserReqstDtoTestFactory.createJson();
        // Multipart 요청을 위한 MockMultipartFile 객체 생성
        final MockMultipartFile jsonFile = new MockMultipartFile("userReqst", "", "application/json", userReqstJsonContent.getBytes());
        // 응답 객체 설정
        final UserReqstDto rsltDto = UserReqstDtoTestFactory.create();
        rsltDto.setUserNo(0);
        final ServiceResponse result = ServiceResponse.builder().rsltObj(rsltDto).rslt(true).message("신규계정이 성공적으로 신청되었습니다.").build();
        when(userReqstService.regist(any(UserReqstDto.class))).thenReturn(result);

        // when::
        mockMvc.perform(MockMvcRequestBuilders.multipart(Url.USER_REQST_REG_AJAX)
                        .file(jsonFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print()) // 요청/응답 디테일 출력 (선택적)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.rslt").value(true))
                .andExpect(jsonPath("$.message").value("신규계정이 성공적으로 신청되었습니다."));
    }

    @Test
    public void testUserCfAjax() throws Exception {
        final ServiceResponse result = ServiceResponse.builder().rslt(true).message(MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS)).build();
        when(userReqstService.cf(anyInt())).thenReturn(result);

        mockMvc.perform(post(Url.USER_REQST_CF_AJAX)
                .param("userNo", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(true));
    }

    @Test
    public void testUserUncfAjax() throws Exception {
        final ServiceResponse result = ServiceResponse.builder().rslt(true).message(MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS)).build();
        when(userReqstService.uncf(anyInt())).thenReturn(result);

        mockMvc.perform(post(Url.USER_REQST_UNCF_AJAX)
                        .param("userNo", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rslt").value(true));
    }

}