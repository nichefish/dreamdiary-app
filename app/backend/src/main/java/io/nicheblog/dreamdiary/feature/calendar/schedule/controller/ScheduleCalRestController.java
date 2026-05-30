package io.nicheblog.dreamdiary.feature.calendar.schedule.controller;

import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleSearchParam;
import io.nicheblog.dreamdiary.feature.calendar.schedule.service.ScheduleCalService;
import io.nicheblog.dreamdiary.feature.user.account.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.account.service.UserService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ScheduleCalRestController
 * <pre>
 *  일정 달력 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class ScheduleCalRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.SCHEDULE_CAL;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.SCHEDULE;      // 작업 카테고리 (로그 적재용)

    private final ScheduleCalService scheduleCalService;
    private final CodeLookupService codeLookupService;
    private final UserService userService;

    @GetMapping(Url.SCHEDULE_BOOTSTRAP)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> scheduleBootstrap() throws Exception {

        final List<UserDto> crtdUserList = userService.getCrdtUserList(
                DateUtils.getCurrDateAddDayStr(-40),
                DateUtils.getCurrDateAddDayStr(40)
        );
        final List<UserDto> userList = crtdUserList == null ? List.of() : crtdUserList;

        final Map<String, Object> payload = new HashMap<>();
        payload.put("vcatnCd", Code.SCHEDULE_VCATN);
        payload.put("brthdyCd", Code.SCHEDULE_BRTHDY);
        payload.put("holyDayCode", Code.SCHEDULE_HOLYDAY);
        payload.put("codeOptions", codeLookupService.getCdItemListByGroupCode(Code.SCHEDULE_CD));
        payload.put("jandiTopicOptions", codeLookupService.getCdItemListByGroupCode(Code.JANDI_TOPIC_CD));
        payload.put("userOptions", userList.stream()
                .map(user -> {
                    final Map<String, String> item = new HashMap<>();
                    item.put("username", user.getUsername());
                    item.put("userNm", user.getUserNm());
                    return item;
                })
                .toList()
        );

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(payload));
    }

    /**
     * 일정 > 전체 일정 (달력) 목록 데이터 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.SCHEDULE_CAL_LIST_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> scheduleCalListAjax(
            final ScheduleSearchParam searchParam
    ) throws Exception {

        final List<BaseCalDto> scheduleCalList = scheduleCalService.getScheduleTotalCalList(searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(scheduleCalList));
    }
}
