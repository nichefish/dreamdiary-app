package io.nicheblog.dreamdiary.adapter.kasi.controller;

import io.nicheblog.dreamdiary.adapter.kasi.model.HldyKasiApiItemDto;
import io.nicheblog.dreamdiary.adapter.kasi.service.HldyKasiApiService;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.util.List;

/**
 * HldyKasiApiController
 * <pre>
 *  한국천문연구원(KASI):: 특일 정보 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyRestControllerAspect
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")   // CORS 에러 해결 위한 조치
@RequiredArgsConstructor
@Log4j2
@Tag(
        name = "한국천문연구원 특일 정보 API",
        description = "한국천문연구원 특일 정보 API입니다."
)
public class HldyKasiApiController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.API_KASI;      // 작업 카테고리 (로그 적재용)

    private final HldyKasiApiService hldyKasiApiService;

    /**
     * 한국천문연구원(KASI):: 휴일 정보 조회 및 DB 저장
     *
     * @param yy - 조회할 연도의 문자열 (nullable, 지정되지 않을 경우 현재 연도를 사용)
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Operation(
            summary = "휴일 정보 조회 및 DB 저장",
            description = "잔디 메신저로부터 웹훅 메세지 수신한다."
    )
    @PostMapping(Url.API_HLDY_GET)
    public ResponseEntity<AjaxResponse> getHldyInfo(
            final @RequestBody(required = false) @Nullable String yy,
            final LogActvtyParam logParam
    ) throws Exception {

        log.info("requestUrl: {}", request.getRequestURL() + "?" + request.getQueryString());

        // 기존 정보 (API로 받아온 휴일) 삭제 후 재등록
        final String yyStr = !StringUtils.isEmpty(yy) ? yy : DateUtils.getCurrYyStr();
        hldyKasiApiService.delHldyList(yyStr);
        final List<HldyKasiApiItemDto> hldyApiList = hldyKasiApiService.getHldyList(yyStr);

        final boolean isSuccess = hldyKasiApiService.regHldyList(hldyApiList);
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(hldyApiList));
    }
}
