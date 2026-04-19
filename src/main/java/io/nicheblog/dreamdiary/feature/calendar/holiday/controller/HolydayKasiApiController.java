package io.nicheblog.dreamdiary.feature.calendar.holiday.controller;

import io.nicheblog.dreamdiary.feature.calendar.holiday.service.HolydayKasiApiService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;

/**
 * HolydayKasiApiController
 * <pre>
 *  한국천문연구원(KASI):: 특일 정보 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")   // CORS 에러 해결 위한 조치
@RequiredArgsConstructor
@Log4j2
@Tag(
    name = "한국천문연구원 특일 정보 API",
    description = "한국천문연구원 특일 정보 API입니다."
)
public class HolydayKasiApiController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.API_KASI;      // 작업 카테고리 (로그 적재용)

    private final HolydayKasiApiService holydayKasiApiService;

    /**
     * 한국천문연구원(KASI):: 휴일 정보 조회 및 DB 저장
     *
     * @param yy 조회할 연도의 문자열 (nullable, 지정되지 않을 경우 현재 연도를 사용)
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @Operation(
            summary = "휴일 정보 조회 및 DB 저장",
            description = "잔디 메신저로부터 웹훅 메세지 수신한다."
    )
    @PostMapping(Url.API_HOLYDAY_GET)
    public ResponseEntity<AjaxResponse> getHolydayInfo(
            final @Nullable String yy
    ) throws Exception {

        log.info("requestUrl: {}", request.getRequestURL() + "?" + request.getQueryString());

        // 기존 정보 (API로 받아온 휴일) 삭제 후 재등록
        final String yyStr = !StringUtils.isEmpty(yy) ? yy : DateUtils.getCurrYyStr();
        final boolean isSuccess = holydayKasiApiService.resyncHolyday(yyStr);
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }
}
