package io.nicheblog.dreamdiary.feature.attachable.tag.controller;

import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagProfileDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.service.TagProfileService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * TagProfileRestController
 * <pre>
 *  Tag profile CRUD API controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class TagProfileRestController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.TAG;

    private final TagProfileService tagProfileService;

    @GetMapping(Url.TAG_PROFILE)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> tagProfileDtlAjax(
            final @PathVariable Integer tagId,
            final @RequestParam("contentType") String contentType
    ) throws Exception {
        final TagProfileDto tagProfile = tagProfileService.getDtoByRefOrNew(tagId, contentType);
        return ResponseEntity.ok(
                AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS)
                        .withObj(tagProfile)
        );
    }

    @PostMapping(Url.TAG_PROFILE)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> tagProfileRegAjax(
            final @PathVariable Integer tagId,
            final @Valid TagProfileDto tagProfile
    ) throws Exception {
        // 변경 전: tagId를 body/form 데이터에서만 수신
        // 변경 후: /tags/{tagId}/profile 경로 변수 기준으로 tagId를 고정
        tagProfile.setTagId(tagId);

        final ServiceResponse result = tagProfileService.upsert(tagProfile);
        if (Boolean.TRUE.equals(result.getRslt())) {
            tagProfileService.evictTagCloudCaches(tagProfile.getContentType());
        }
        final String rsltMsg = result.getRslt() ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @DeleteMapping(Url.TAG_PROFILE)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> tagProfileDelByTagAjax(
            final @PathVariable Integer tagId,
            final @RequestParam("contentType") String contentType
    ) throws Exception {

        // 변경 전: /tag-profile/{id}로만 삭제 가능
        // 변경 후: /tags/{tagId}/profile?contentType=... 형태로 삭제 가능
        final TagProfileDto existing = tagProfileService.getDtoByTagIdAndContentType(tagId, contentType).orElse(null);
        if (existing == null || existing.getId() == null) {
            final ServiceResponse emptyResult = new ServiceResponse(false, MessageUtils.RSLT_EMPTY);
            return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(emptyResult, MessageUtils.RSLT_EMPTY));
        }

        final ServiceResponse result = tagProfileService.delete(existing.getId());
        if (Boolean.TRUE.equals(result.getRslt())) {
            tagProfileService.evictTagCloudCaches(contentType);
        }
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, MessageUtils.RSLT_SUCCESS));
    }
}
