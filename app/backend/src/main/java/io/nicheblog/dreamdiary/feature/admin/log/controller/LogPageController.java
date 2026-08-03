package io.nicheblog.dreamdiary.feature.admin.log.controller;

import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * LogPageController
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class LogPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.LOG_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LOG;

    /**
     * 로그 목록 화면은 Vue SPA로 위임한다.
     */
    @GetMapping(Url.LOG_LIST)
    @PreAuthorize("hasAuthority('menu.admin.log')")
    public String logList() {
        return "redirect:/vue-app/admin/log";
    }

    /**
     * 사용자별 로그 통계 화면은 Vue SPA placeholder로 위임한다.
     */
    @GetMapping(Url.LOG_STATS_USER_LIST)
    @PreAuthorize("hasAuthority('menu.admin.log_stats')")
    public String logStatsUserList() {
        return "redirect:/vue-app/admin/log/stats-user";
    }
}
