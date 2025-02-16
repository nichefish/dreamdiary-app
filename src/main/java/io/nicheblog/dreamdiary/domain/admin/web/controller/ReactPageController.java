package io.nicheblog.dreamdiary.domain.admin.web.controller;

import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
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
 * @see LogActvtyPageControllerAspect
 */
@Controller
@Log4j2
public class ReactPageController {

    /**
     * 리액트React 메인
     *
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(value = {Url.REACT_MAIN})
    public String getReactMain(
            final LogActvtyParam logParam
    ) {

        // TODO:

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "redirect:/static/react/index.html";
    }
}
