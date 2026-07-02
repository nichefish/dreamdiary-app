package io.nicheblog.dreamdiary.auth.security.controller;

import io.nicheblog.dreamdiary.feature.user.my.service.UserMyService;
import io.nicheblog.dreamdiary.global.Url;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LoginControllerTest
 * <pre>
 *  로그인 컨트롤러 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@WebMvcTest(LoginPageController.class)
@ActiveProfiles("test")
@AutoConfigureRestDocs(outputDir = "build/snippets")
class LoginPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "userMyService")
    private UserMyService userMyService;

    /**
     * 로그인 화면 조회 Test
     * 로그인 사용자가 아닐 때 로그인 페이지 접근
     *
     * 변경 전: FTL 뷰 이름과 템플릿 파일 존재를 검증했다 (FreeMarker 렌더 시절).
     * 변경 후: 컨트롤러가 Vue SPA 로그인 화면으로 리다이렉트만 반환하므로 리다이렉트 경로를 검증한다.
     */
    @Test
    void testLoginFormAnonymous() throws Exception {

        this.mockMvc.perform(get(Url.APP_AUTH_LGN_FORM))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(Url.VUE_SIGN_IN))
                .andDo(document("main"));
    }

    /**
     * 로그인 화면 조회 Test
     * 로그인 사용자일 떄 메인 화면으로 리다이렉트
     *
     * 변경 전: 미사용 지역변수(authInfo, result)가 남아 있었다. 검증(302)은 동일하게 유지하고,
     * 변경 후: 리다이렉트 목적지(메인 화면)까지 명시 검증한다.
     */
    @Test
    @WithMockUser
    void testLoginFormAuthenticated() throws Exception {

        this.mockMvc.perform(get(Url.APP_AUTH_LGN_FORM))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(Url.MAIN))
                .andDo(document("main"));
    }
}
