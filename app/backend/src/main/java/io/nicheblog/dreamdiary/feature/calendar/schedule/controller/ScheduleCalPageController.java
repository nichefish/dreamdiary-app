package io.nicheblog.dreamdiary.feature.calendar.schedule.controller;

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

/**
 * Legacy schedule calendar entrypoint.
 *
 * <p>The screen is owned by the Vue app; this controller only preserves
 * {@link Url#SCHEDULE_CAL} and {@link Url#SCHEDULE_CAL_LEGACY} links during the FreeMarker removal.</p>
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class ScheduleCalPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.SCHEDULE_CAL;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.SCHEDULE;

    @GetMapping({Url.SCHEDULE_CAL, Url.SCHEDULE_CAL_LEGACY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String scheduleCal() {
        return "redirect:/vue-app/schedule";
    }
}
