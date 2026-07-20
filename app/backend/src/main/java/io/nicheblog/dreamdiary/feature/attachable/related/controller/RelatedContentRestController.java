package io.nicheblog.dreamdiary.feature.attachable.related.controller;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentFlowDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentPostDto;
import io.nicheblog.dreamdiary.feature.attachable.related.service.my.MyRelatedContentService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
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
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withList(relatedList));
    }

    /**
     * 앵커 엔트리의 FLOW 연결 컴포넌트를 시간순으로 조회한다.
     *
     * @param contentType 앵커 콘텐츠 타입
     * @param id 앵커 엔트리 ID
     * @return FLOW 조회 결과
     * @throws Exception 조회 중 예외
     */
    @GetMapping(value = {Url.RELATED_FLOW})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> relatedContentFlowAjax(
            final @PathVariable("contentType") String contentType,
            final @PathVariable("id") Integer id
    ) throws Exception {
        final ContentType resolvedContentType = ContentType.get(contentType);
        final RelatedContentFlowDto flow = myRelatedContentService.getMyFlowDto(resolvedContentType, id);
        return ResponseEntity.ok(
                AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(flow)
        );
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

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, MessageUtils.getMessage("common.result.success")));
    }

    @DeleteMapping(value = {Url.RELATED})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> relatedContentDeleteAjax(
            final @PathVariable("relatedContentId") Integer relatedContentId
    ) {
        final boolean deleted = myRelatedContentService.deleteMyRelation(relatedContentId);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(deleted, deleted ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure")));
    }
}
