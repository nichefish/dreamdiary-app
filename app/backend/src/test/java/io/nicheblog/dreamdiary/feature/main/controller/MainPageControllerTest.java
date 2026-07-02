package io.nicheblog.dreamdiary.feature.main.controller;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.model.AuthInfoTestFactory;
import io.nicheblog.dreamdiary.feature.admin.web.controller.MainPageController;
import io.nicheblog.dreamdiary.global.Url;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
     *
     * 변경 전: FTL 뷰 이름과 템플릿 파일 존재를 검증했다 (FreeMarker 렌더 시절).
     * 변경 후: 컨트롤러가 저널 주간 화면(Vue SPA)으로 리다이렉트만 반환하므로 리다이렉트 경로를 검증한다.
     */
    @Test
    @WithMockUser
    void testMain() throws Exception {

        AuthInfo authInfo = AuthInfoTestFactory.createAuthInfo();

        this.mockMvc.perform(get(Url.MAIN)
                .sessionAttr("authInfo", authInfo))  // 세션 어트리뷰트 추가
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vue-app/journal/weekly"))
                .andDo(document("main"));
    }

    /**
     * 관리자 메인 화면 조회 Test
     *
     * 변경 전: FTL 뷰 이름과 템플릿 파일 존재를 검증했다 (FreeMarker 렌더 시절).
     * 변경 후: 컨트롤러가 사이트 관리 화면으로 리다이렉트만 반환하므로 리다이렉트 경로를 검증한다.
     */
    @Test
    @WithMockUser
    void testAdminMain() throws Exception {

        AuthInfo authInfo = AuthInfoTestFactory.createAuthInfo();

        this.mockMvc.perform(get(Url.ADMIN_MAIN)
                .sessionAttr("authInfo", authInfo))  // 세션 어트리뷰트 추가
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Url.ADMIN_PAGE))
                .andDo(document("adminMain"));
    }
}
