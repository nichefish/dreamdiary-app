package io.nicheblog.dreamdiary.feature.journal.thread.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * JournalThreadPageController
 * <pre>
 *  저널 스레드 페이지 컨트롤러.
 *  변경(thread-3): FTL 렌더 → Vue SPA 리다이렉트로 전환.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class JournalThreadPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_THREAD_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;      // 작업 카테고리 (로그 적재용)

    /**
     * 저널 스레드 목록 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @return {@link String} -- Vue SPA 리다이렉트
     */
    @GetMapping(Url.JOURNAL_THREAD_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalThreadList() {
        return "redirect:/vue-app/thread";
    }

    /**
     * 저널 스레드 등록 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @return {@link String} -- Vue SPA 리다이렉트
     */
    @GetMapping(Url.JOURNAL_THREAD_REGIST_FORM)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalThreadRegistForm() {
        return "redirect:/vue-app/thread";
    }

    /**
     * 저널 스레드 상세 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link String} -- Vue SPA 리다이렉트
     */
    @GetMapping(Url.JOURNAL_THREAD_DETAIL)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalThreadDetail(
            final @RequestParam("id") Integer key
    ) {
        return "redirect:/vue-app/thread";
    }

    /**
     * 저널 스레드 수정 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link String} -- Vue SPA 리다이렉트
     */
    @GetMapping(Url.JOURNAL_THREAD_MODIFY_FORM)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalThreadModifyForm(
            final @RequestParam("id") Integer key
    ) {
        return "redirect:/vue-app/thread";
    }
}
