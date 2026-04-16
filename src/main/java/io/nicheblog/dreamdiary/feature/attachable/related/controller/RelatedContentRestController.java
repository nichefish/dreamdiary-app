package io.nicheblog.dreamdiary.feature.attachable.related.controller;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentPostDto;
import io.nicheblog.dreamdiary.feature.attachable.related.service.my.MyRelatedContentService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * RelatedContentRestController
 * <pre>
 *  관련글 API Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class RelatedContentRestController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.DEFAULT;

    private final MyRelatedContentService myRelatedContentService;

    @GetMapping(value = {Url.RELATEDS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> relatedContentListAjax(
            final @PathVariable("contentType") String contentType,
            final @PathVariable("id") Integer id
    ) throws Exception {
        final ContentType resolvedContentType = ContentType.get(contentType);
        final List<RelatedContentDto> relatedList = myRelatedContentService.getMyListDto(resolvedContentType, id);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withList(relatedList));
    }

    @PostMapping(value = {Url.RELATEDS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> relatedContentRegAjax(
            final @PathVariable("contentType") String contentType,
            final @PathVariable("id") Integer id,
            final @RequestBody @Valid RelatedContentPostDto relatedContent
    ) throws Exception {
        relatedContent.setSrcId(id);
        relatedContent.setSrcContentType(contentType);

        final RelatedContentDto savedDto = myRelatedContentService.saveMyRelation(relatedContent);
        final ServiceResponse result = ServiceResponse.builder()
                .rslt(savedDto.getId() != null)
                .rsltObj(savedDto)
                .build();

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, MessageUtils.RSLT_SUCCESS));
    }

    @DeleteMapping(value = {Url.RELATED})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> relatedContentDeleteAjax(
            final @PathVariable("relatedContentId") Integer relatedContentId
    ) {
        final boolean deleted = myRelatedContentService.deleteMyRelation(relatedContentId);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(deleted, deleted ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE));
    }
}
