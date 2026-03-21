package io.nicheblog.dreamdiary.feature.clsf.state.controller;

import io.nicheblog.dreamdiary.feature.clsf.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.feature.clsf.state.service.StateService;
import io.nicheblog.dreamdiary.feature.clsf.tag.handler.TagProcEventListener;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.Constant;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * StateRestController
 * <pre>
 *  상태 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class StateRestController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.STATE;           // 작업 카테고리 (로그 적재용)

    private final StateService stateService;

    /**
     * 상태 변경 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see TagProcEventListener
     */
    @PostMapping(value = {Url.STATES})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> stateAjax(
            final @RequestBody StateToggleDto stateToggle
    ) throws Exception {

        final ServiceResponse result = stateService.toggle(stateToggle);
        final String rsltSts = result.getRsltSts();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        final AjaxResponse response = AjaxResponse.fromResponseWithObj(result, rsltMsg);
        response.setRsltSts(rsltSts);
        return ResponseEntity.ok(response);
    }

}
