package io.nicheblog.dreamdiary.feature.admin.code.controller;

import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupDto;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupPatchDto;
import io.nicheblog.dreamdiary.feature.admin.code.service.CodeGroupService;
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

@RestController
@RequiredArgsConstructor
@Log4j2
public class CodeGroupRestController extends BaseControllerImpl {
    @Getter
    private final String baseUrl = Url.CODE_GROUP_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.CD;

    private final CodeGroupService codeGroupService;

    @PostMapping(value = {Url.CODE_GROUPS})
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> codeGroupRegAjax(final @Valid CodeGroupDto codeGroupDto) throws Exception {
        final ServiceResponse result = codeGroupService.regist(codeGroupDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @PostMapping(value = {Url.CODE_GROUP})
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> codeGroupMdfAjax(final @PathVariable("id") Integer id, final @Valid CodeGroupDto codeGroupDto) throws Exception {
        codeGroupDto.setId(id);
        final ServiceResponse result = codeGroupService.modify(codeGroupDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @GetMapping(Url.CODE_GROUP)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> codeGroupDtlAjax(final @PathVariable("id") Integer id) throws Exception {
        final CodeGroupDto codeGroup = codeGroupService.getDtlDto(id);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(codeGroup));
    }

    @PatchMapping(Url.CODE_GROUP)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> codeGroupPatchAjax(final @PathVariable("id") Integer id, final @RequestBody CodeGroupPatchDto patchDto) throws Exception {
        final ServiceResponse result = codeGroupService.patch(id, patchDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @DeleteMapping(Url.CODE_GROUP)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> codeGroupDelAjax(final @PathVariable("id") Integer id) throws Exception {
        final ServiceResponse result = codeGroupService.delete(id);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;
        return ResponseEntity.ok(AjaxResponse.fromResponse(result, rsltMsg));
    }
}
