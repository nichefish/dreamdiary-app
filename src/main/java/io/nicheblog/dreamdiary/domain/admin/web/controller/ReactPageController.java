package io.nicheblog.dreamdiary.domain.admin.web.controller;

import io.nicheblog.dreamdiary.global.Url;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ReactPageController
 * <pre>
 *  React 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@Log4j2
public class ReactPageController {

    /**
     * 리액트React 메인
     *
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(value = {Url.REACT_MAIN})
    public String getReactMain() {

        return "redirect:/static/react/index.html";
    }
}
