package io.nicheblog.dreamdiary.feature.clsf.tag.controller;

import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagProfileDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagProfileService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping(Url.TAG_PROFILES)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> tagProfileDtlAjax(
            final @RequestParam("tagNo") Integer tagNo,
            final @RequestParam("contentType") String contentType
    ) throws Exception {

        final TagProfileDto tagProfile = tagProfileService.getDtoByRefOrNew(tagNo, contentType);
        return ResponseEntity.ok(
                AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS)
                        .withObj(tagProfile)
        );
    }

    @PostMapping(Url.TAG_PROFILES)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> tagProfileRegAjax(
            final @Valid TagProfileDto tagProfile
    ) throws Exception {

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
    public ResponseEntity<AjaxResponse> tagProfileDelAjax(
            final @PathVariable("tagProfileNo") Integer tagProfileNo
    ) throws Exception {

        final TagProfileDto existing = tagProfileService.getDtlDto(tagProfileNo);
        final ServiceResponse result = tagProfileService.delete(tagProfileNo);
        if (Boolean.TRUE.equals(result.getRslt()) && existing != null) {
            tagProfileService.evictTagCloudCaches(existing.getContentType());
        }
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, MessageUtils.RSLT_SUCCESS));
    }
}
