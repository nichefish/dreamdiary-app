package io.nicheblog.dreamdiary.feature.attachable.prefix.controller;

import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 로그인 사용자의 말머리 관리·선택지 API.
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my/prefixes")
@Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
public class PrefixRestController {

    private final PrefixService service;

    /** 비활성 항목을 포함한 내 말머리 목록을 content_type별로 조회한다. */
    @GetMapping
    public ResponseEntity<AjaxResponse> getMine(@RequestParam final String contentType) {
        return okList(service.getMine(contentType));
    }

    /** 콘텐츠 편집·검색에 사용할 활성 말머리 목록을 content_type별로 조회한다. */
    @GetMapping("/options")
    public ResponseEntity<AjaxResponse> getOptions(@RequestParam final String contentType) {
        return okList(service.getActiveMine(contentType));
    }

    /** 지정 content_type 목록에 말머리를 생성한다. */
    @PostMapping
    public ResponseEntity<AjaxResponse> create(
            @RequestParam final String contentType,
            @Valid @RequestBody final PrefixDto request
    ) {
        return okObj(service.create(contentType, request));
    }

    /** 말머리를 수정한다. */
    @PutMapping("/{prefixId}")
    public ResponseEntity<AjaxResponse> update(
            @RequestParam final String contentType,
            @PathVariable final Integer prefixId,
            @Valid @RequestBody final PrefixDto request
    ) {
        return okObj(service.update(contentType, prefixId, request));
    }

    /** 기존 콘텐츠 참조를 보존한 채 활성 상태를 변경한다. */
    @PatchMapping("/{prefixId}/active")
    public ResponseEntity<AjaxResponse> setActive(
            @RequestParam final String contentType,
            @PathVariable final Integer prefixId,
            @RequestParam final boolean active
    ) {
        service.setActive(contentType, prefixId, active);
        return okObj(null);
    }

    private ResponseEntity<AjaxResponse> okObj(final Object object) {
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(
                true, MessageUtils.getMessage("common.result.success")).withObj(object));
    }

    private ResponseEntity<AjaxResponse> okList(final Object list) {
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(
                true, MessageUtils.getMessage("common.result.success")).withList((java.util.List<?>) list));
    }
}
