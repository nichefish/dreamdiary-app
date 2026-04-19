package io.nicheblog.dreamdiary.feature.calendar.schedule.kasi.controller;

import io.nicheblog.dreamdiary.feature.calendar.holiday.controller.HolydayKasiApiController;
import io.nicheblog.dreamdiary.feature.calendar.holiday.service.HolydayKasiApiService;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.Url;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HolydayKasiApiControllerTest
 * <pre>
 *  API:: 한국천문연구원(KASI):: 특일 정보 API 컨트롤러 테스트
 * </pre>
 *
 * @author nichefish
 */
@Log4j2
public class HolydayKasiApiControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private HolydayKasiApiController holydayKasiApiController;

    @Mock
    private HolydayKasiApiService holydayKasiApiService;
    @Mock
    private HttpServletRequest request;
    @Mock
    protected ApplicationEventPublisher publisher;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(holydayKasiApiController).build();
    }

    /**
     * 한국천문연구원(KASI):: 휴일 정보 조회 및 DB 저장 검증
     */
    @Test
    @DisplayName("한국천문연구원(KASI):: 휴일 정보 조회 및 DB 저장 검증")
    @Disabled
    public void testGetHolydayInfo() throws Exception {
        // Given::

        // When::
        when(request.getRequestURL())
                .thenReturn(new StringBuffer(Url.API_HOLYDAY_GET));
        when(holydayKasiApiService.getHolydayList(any(String.class)))
                .thenReturn(new ArrayList<>());
        when(holydayKasiApiService.regHolydayList(any()))
                .thenReturn(true);

        // Then::
        mockMvc.perform(
                        MockMvcRequestBuilders.post(Url.API_HOLYDAY_GET)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)  // 폼 데이터를 전송하는 경우
                                .param("yy", "2024")         // JandiParam의 필드 설정
                )
                .andExpect(status().isOk())
                .andExpect(content().json(TestConstant.EXPECTED_JSON_RESPONSE));
    }
}
