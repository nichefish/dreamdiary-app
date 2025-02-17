package io.nicheblog.dreamdiary.domain.admin.web.controller;

import io.nicheblog.dreamdiary.global.*;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 인터페이스에서 정의된 Url들을 Map으로 반환
     *
     * @return Map<String, String> - 상수들을 key-value 형태로 담은 Map
     */
    @GetMapping("/cmm/getUrlMap.do")
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
     * @return Map<String, String> - 상수들을 key-value 형태로 담은 Map
     */
    @GetMapping("/cmm/getConstantMap.do")
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
     * @return Map<String, String> - 상수들을 key-value 형태로 담은 Map
     */
    @GetMapping("/cmm/getServerInfo.do")
    public ResponseEntity<AjaxResponse> getServerInfo(
            //
    ) {

        // TODO: releaseInfo 등 포함하기
        final String rsltMsg = MessageUtils.getMessage("msg.common.success");
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, rsltMsg).withObj(serverInfo));
    }
}
