package io.nicheblog.dreamdiary.feature.journal.annual.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * JournalAnnualPageController
 * <pre>
 *  저널 결산 페이지 Controller.
 *  변경(Sub-4): FTL 렌더 → Vue SPA 리다이렉트로 전환.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
public class JournalAnnualPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_ANNUAL_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    /**
     * 저널 결산 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * 변경(Sub-4): FTL journal_annual_list 렌더 → Vue SPA 리다이렉트로 전환.
     *  → @param searchParam, model 제거(리다이렉트 전환으로 불필요).
     *
     * @return {@link String} -- 리다이렉트 경로 (Vue SPA /vue-app/annual)
     */
    @GetMapping(Url.JOURNAL_ANNUAL_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalAnnualPage() {
        return "redirect:/vue-app/annual";
    }

    /**
     * 저널 결산 상세 화면 조회
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * 변경(Sub-4): FTL journal_annual_detail 렌더 → Vue SPA 리다이렉트로 전환.
     *  → @param section 제거(SPA가 자체 상태로 관리), model 제거(리다이렉트 전환으로 불필요).
     *
     * @param yy 년도
     * @return {@link String} -- 리다이렉트 경로 (Vue SPA /vue-app/annual/{yy})
     */
    @GetMapping(value = Url.JOURNAL_ANNUAL_VIEW)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String journalAnnualView(
            final @PathVariable("yy") Integer yy
    ) {
        return "redirect:/vue-app/annual/" + yy;
    }
}
