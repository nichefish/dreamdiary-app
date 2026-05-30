package io.nicheblog.dreamdiary.feature.admin.web.controller;

import io.nicheblog.dreamdiary.global.*;
import io.nicheblog.dreamdiary.infrastructure.release.model.ReleaseHistoryDto;
import io.nicheblog.dreamdiary.infrastructure.release.service.ReleaseHistoryService;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CmmController
 * <pre>
 *  공통 데이터를 뷰에 전달하기 위한 컨트롤러
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class CmmController {

    private final ActiveProfile activeProfile;
    private final ServerInfo serverInfo;
    private final ReleaseInfo releaseInfo;
    private final ReleaseHistoryService releaseHistoryService;

    /**
     * 인터페이스에서 정의된 Url들을 Map으로 반환
     *
     * @return 상수들을 key-value 형태로 담은 Map
     */
    @GetMapping("/cmm/get-url-map.do")
    public ResponseEntity<AjaxResponse> getUrlMap(
            //
    ) {

        final Map<String, String> urlMap = Url.getUrlMap();
        final String rsltMsg = MessageUtils.getMessage("msg.common.success");
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, rsltMsg).withMap(urlMap));
    }

    /**
     * 인터페이스에서 정의된 상수들을 Map으로 반환
     *
     * @return 상수들을 key-value 형태로 담은 Map
     */
    @GetMapping("/cmm/get-constant-map.do")
    public ResponseEntity<AjaxResponse> getConstantMap(
            //
    ) {

        final Map<String, String> constantMap = Constant.getConstantMap();
        final String rsltMsg = MessageUtils.getMessage("msg.common.success");
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, rsltMsg).withMap(constantMap));
    }

    /**
     * 서버 정보를 반환
     *
     * @return 상수들을 key-value 형태로 담은 Map
     */
    @GetMapping("/cmm/get-server-info.do")
    public ResponseEntity<AjaxResponse> getServerInfo(
            //
    ) {

        // TODO: releaseInfo 등 포함하기
        final String rsltMsg = MessageUtils.getMessage("msg.common.success");
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, rsltMsg).withObj(serverInfo));
    }

    /**
     * release history 정보를 반환
     *
     * @param size 조회 건수 (기본 20)
     * @return release history map
     */
    @GetMapping("/cmm/get-release-history.do")
    public ResponseEntity<AjaxResponse> getReleaseHistory(
            final @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {

        final List<ReleaseHistoryDto> histories = releaseHistoryService.getRecentHistories(size);
        final ReleaseHistoryDto latestDeploy = releaseHistoryService.getLatestDeploy().orElse(null);
        final Map<String, Object> payload = new HashMap<>();
        payload.put("latestDeploy", latestDeploy);
        payload.put("histories", histories);
        final String rsltMsg = MessageUtils.getMessage("msg.common.success");
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, rsltMsg).withMap(payload));
    }
}
