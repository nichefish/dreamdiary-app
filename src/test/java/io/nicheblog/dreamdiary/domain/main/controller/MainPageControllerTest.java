package io.nicheblog.dreamdiary.domain.main.controller;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.model.AuthInfoTestFactory;
import io.nicheblog.dreamdiary.domain.admin.web.controller.MainPageController;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.BaseControllerTestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MainControllerTest
 * <pre>
 *  메인 컨트롤러 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@WebMvcTest(MainPageController.class)
@ActiveProfiles("test")
@AutoConfigureRestDocs(outputDir = "build/snippets")
public class MainPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 메인 화면 조회 Test
     */
    @Test
    @WithMockUser
    void testMain() throws Exception {

        AuthInfo authInfo = AuthInfoTestFactory.createAuthInfo();

        MvcResult result = this.mockMvc.perform(get(Url.MAIN)
                .sessionAttr("authInfo", authInfo))  // 세션 어트리뷰트 추가
                .andExpect(status().isOk())
                .andDo(document("main"))
                .andReturn();

        String viewName = Objects.requireNonNull(result.getModelAndView()).getViewName();
        assertNotNull(viewName, "View name is null");
        assertTrue(BaseControllerTestHelper.viewFileExists(viewName), "View template file does not exist: " + viewName);
    }

    /**
     * 관리자 메인 화면 조회 Test
     */
    @Test
    @WithMockUser
    void testAdminMain() throws Exception {

        AuthInfo authInfo = AuthInfoTestFactory.createAuthInfo();

        MvcResult result = this.mockMvc.perform(get(Url.ADMIN_MAIN)
                .sessionAttr("authInfo", authInfo))  // 세션 어트리뷰트 추가
                .andExpect(status().isOk())
                .andDo(document("adminMain"))
                .andReturn();

        String viewName = Objects.requireNonNull(result.getModelAndView()).getViewName();
        assertNotNull(viewName, "View name is null");
        assertTrue(BaseControllerTestHelper.viewFileExists(viewName), "View template file does not exist: " + viewName);
    }
}