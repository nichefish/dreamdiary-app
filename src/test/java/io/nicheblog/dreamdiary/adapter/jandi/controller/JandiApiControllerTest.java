package io.nicheblog.dreamdiary.adapter.jandi.controller;

import io.nicheblog.dreamdiary.adapter.jandi.JandiTopic;
import io.nicheblog.dreamdiary.adapter.jandi.model.JandiParam;
import io.nicheblog.dreamdiary.adapter.jandi.service.JandiApiService;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImplTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JandiApiControllerTest
 * <pre>
 *  API:: JANDI (incoming/outgoing)webhook 컨트롤러에 대한 슬라이스 테스트
 * </pre>
 *
 * @author nichefish
 */
@WebMvcTest(JandiApiController.class)
@ActiveProfiles("test")
@Log4j2
public class JandiApiControllerTest
        extends BaseControllerImplTest {

    // Mock Bean은 기존 Bean의 껍데기만 가져오고 내부 구현은 사용자에게 위임한 형태이다.
    // Mock을 사용한다면 내부 구현도 알아야 하고, 테스트 코드를 작성하며 테스트의 성공을 의도할 수 있기 때문에 완벽한 테스트라 보기 힘들다.
    @MockBean
    private JandiApiService jandiApiService;

    /**
     * JANDI:: 메세지 송신 검증
     */
    @Test
    public void testSendMsg() throws Exception {
        // Given::

        // When::
        when(request.getRequestURL())
                .thenReturn(new StringBuffer(Url.API_JANDI_SND_MSG));
        when(jandiApiService.sendMsg(any(JandiParam.class)))
                .thenReturn(true);

        // Then::
        mockMvc.perform(
                    post(Url.API_JANDI_SND_MSG)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)  // 폼 데이터를 전송하는 경우
                        .param("trgetTopic", JandiTopic.TEST.name())         // JandiParam의 필드 설정
                        .param("msg", "MSG")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(TestConstant.EXPECTED_JSON_RESPONSE));
    }
}
