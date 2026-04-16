package io.nicheblog.dreamdiary.feature.admin.code.controller;

import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupDto;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupPatchDto;
import io.nicheblog.dreamdiary.feature.admin.code.service.CodeGroupService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.model.LogActvtyParam;
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
    public ResponseEntity<AjaxResponse> clCdRegAjax(final @Valid CodeGroupDto clCd, final LogActvtyParam logParam) throws Exception {
        final ServiceResponse result = codeGroupService.regist(clCd);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @PostMapping(value = {Url.CODE_GROUP})
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clCdMdfAjax(final @PathVariable("id") Integer id, final @Valid CodeGroupDto clCdDto, final LogActvtyParam logParam) throws Exception {
        clCdDto.setId(id);
        final ServiceResponse result = codeGroupService.modify(clCdDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @GetMapping(Url.CODE_GROUP)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clCdDtlAjax(final @PathVariable("id") Integer id, final LogActvtyParam logParam) throws Exception {
        final CodeGroupDto codeGroup = codeGroupService.getDtlDto(id);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(codeGroup));
    }

    @PatchMapping(Url.CODE_GROUP)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clCdPatchAjax(final @PathVariable("id") Integer id, final @RequestBody CodeGroupPatchDto patchDto, final LogActvtyParam logParam) throws Exception {
        final ServiceResponse result = codeGroupService.patch(id, patchDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @DeleteMapping(Url.CODE_GROUP)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clCdDelAjax(final @PathVariable("id") Integer id, final LogActvtyParam logParam) throws Exception {
        final ServiceResponse result = codeGroupService.delete(id);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
        return ResponseEntity.ok(AjaxResponse.fromResponse(result, rsltMsg));
    }
}
