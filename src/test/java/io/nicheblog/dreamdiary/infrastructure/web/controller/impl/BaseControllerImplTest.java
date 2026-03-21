package io.nicheblog.dreamdiary.infrastructure.web.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * BaseControllerImpl 관련 테스트 의존성 상속 클래스
 *
 * @author nichefish
 */
@WebMvcTest
public class BaseControllerImplTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected HttpServletRequest request;
    @MockBean
    protected HttpServletResponse response;
    @MockBean
    protected ApplicationEventPublisher publisher;
}
