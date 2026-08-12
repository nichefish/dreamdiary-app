package io.nicheblog.dreamdiary.feature.calendar.schedule.controller;

import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleSearchParam;
import io.nicheblog.dreamdiary.feature.calendar.schedule.service.ScheduleCalService;
import io.nicheblog.dreamdiary.feature.calendar.schedule.service.ScheduleService;
import io.nicheblog.dreamdiary.feature.user.account.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.account.service.UserService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleDto;

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
    private final ScheduleService scheduleService;
    private final CodeLookupService codeLookupService;
    private final UserService userService;

    /**
     * 월별 공휴일 날짜 목록 조회 (미니 달력용 경량 API).
     * 캐시된 전체 공휴일 엔티티에서 해당 년/월에 속하는 날짜만 필터링하여 반환한다.
     *
     * @param yy 연도
     * @param mnth 월 (1-based)
     * @return {@link ResponseEntity} -- 'YYYY-MM-DD' 형식의 공휴일 날짜 리스트
     */
    @GetMapping(Url.SCHEDULE_HOLIDAYS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> holidayListByMonth(
            @RequestParam final int yy,
            @RequestParam final int mnth
    ) throws Exception {

        final List<String> holidayDates = scheduleService.getHolydayEntityList().stream()
                .filter(entity -> entity.getBgnDt() != null
                        && entity.getBgnDt().getYear() == yy
                        && entity.getBgnDt().getMonthValue() == mnth)
                .map(entity -> {
                    try {
                        return DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .distinct()
                .toList();

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(holidayDates));
    }

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
        payload.put("vcatnCodeOptions", codeLookupService.getCdItemListByGroupCode(Code.VCATN_CD));
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

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(payload));
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
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(scheduleCalList));
    }

    /**
     * 일정 > 전체 일정 (목록 VIEW) 데이터 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param page 페이지 번호 (0-base)
     * @param size 페이지 크기
     * @return {@link ResponseEntity} -- 처리 결과와 페이징 목록
     */
    @GetMapping(Url.SCHEDULE_LIST_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> scheduleListAjax(
            final ScheduleSearchParam searchParam,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "25") final int size
    ) throws Exception {

        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bgnDt"));
        final Page<ScheduleDto> pageResult = scheduleCalService.getScheduleListPage(searchParam, pageRequest);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(pageResult));
    }
}
