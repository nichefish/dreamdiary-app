package io.nicheblog.dreamdiary.feature.attachable.lifecycle.controller;

import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.LifecycleSetDto;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 부착 가능 컨텐츠 라이프사이클 설정 REST 컨트롤러.
 *
 * <p>클라이언트가 {@code OPEN/PENDING/RESOLVED}를 state checkbox처럼 다루지 않도록
 * state toggle API와 별도의 endpoint로 분리한다.</p>
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class LifecycleRestController {

    private final LifecycleService lifecycleService;

    /**
     * 컨텐츠 하나의 라이프사이클 키를 설정한다.
     *
     * @param lifecycleSet 컨텐츠 ID, 컨텐츠 타입, 라이프사이클 키, 캐시 컨텍스트
     * @return 성공 시 이전/현재 라이프사이클 키를 담은 ajax 응답
     * @throws Exception 서비스 처리 중 예외가 발생한 경우
     */
    @PutMapping(value = {Url.LIFECYCLES})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> lifecycleAjax(
            final @RequestBody LifecycleSetDto lifecycleSet
    ) throws Exception {

        final ServiceResponse result = lifecycleService.set(lifecycleSet);
        if (!Boolean.TRUE.equals(result.getRslt())) {
            final String failMsg = result.getMessage() != null ? result.getMessage() : MessageUtils.getMessage("common.result.failure");
            return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, failMsg));
        }

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, MessageUtils.getMessage("common.result.success")));
    }
}
