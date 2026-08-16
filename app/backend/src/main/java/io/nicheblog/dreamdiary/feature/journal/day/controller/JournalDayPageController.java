package io.nicheblog.dreamdiary.feature.journal.day.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * JournalDayPageController
 * <pre>
 *  프론트엔드 구현체와 독립적인 저널 일자 화면 진입 URL을 제공한다.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@Log4j2
public class JournalDayPageController {

    /**
     * 저널 일자 공통 화면으로 진입한다.
     * 현재 화면 구현체인 Vue SPA의 사용자별 기본 보기 해석 route로 연결한다.
     *
     * @return {@link String} -- Vue SPA 리다이렉트
     */
    @GetMapping(Url.JOURNAL_DAY_HOME)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalDayHome() {
        log.debug("[journal-day-home] canonical screen URL requested; redirecting to active frontend");
        return "redirect:/vue-app/journal/day/home";
    }
}
