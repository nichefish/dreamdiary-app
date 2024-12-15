package io.nicheblog.dreamdiary.auth.controller;

import io.nicheblog.dreamdiary.domain.admin.web.controller.LgnPageController;
import io.nicheblog.dreamdiary.domain.user.my.service.UserMyService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.model.AuthInfoTestFactory;
import io.nicheblog.dreamdiary.global.intrfc.controller.BaseControllerTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LgnControllerTest
 * <pre>
 *  로그인 컨트롤러 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@WebMvcTest(LgnPageController.class)
@ActiveProfiles("test")
@AutoConfigureRestDocs(outputDir = "target/snippets")
class LgnPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "userMyService")
    private UserMyService userMyService;

    /**
     * 로그인 화면 조회 Test
     * 로그인 사용자가 아닐 때 로그인 페이지 접근
     */
    @Test
    void testLgnFormAnonymous() throws Exception {

        AuthInfo authInfo = AuthInfoTestFactory.createAuthInfo();

        MvcResult result = this.mockMvc.perform(get(Url.AUTH_LGN_FORM))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("main"))
                .andReturn();

        String viewName = Objects.requireNonNull(result.getModelAndView()).getViewName();
        assertNotNull(viewName, "View name is null");
        Assertions.assertTrue(BaseControllerTestHelper.viewFileExists(viewName), "View template file does not exist: " + viewName);
    }

    /**
     * 로그인 화면 조회 Test
     * 로그인 사용자일 떄 메인 화면으로 리다이렉트
     */
    @Test
    @WithMockUser
    void testLgnFormAuthenticated() throws Exception {

        AuthInfo authInfo = AuthInfoTestFactory.createAuthInfo();

        MvcResult result = this.mockMvc.perform(get(Url.AUTH_LGN_FORM))
                .andDo(print())
                .andExpect(status().isFound())
                .andDo(document("main"))
                .andReturn();
    }
}