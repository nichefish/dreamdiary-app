package io.nicheblog.dreamdiary.feature.admin.code.controller;

import io.nicheblog.dreamdiary.feature.admin.code.model.CodeItemDto;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeItemParam;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeItemSearchParam;
import io.nicheblog.dreamdiary.feature.admin.code.service.CodeItemService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
public class CodeItemRestController extends BaseControllerImpl {
    @Getter
    private final String baseUrl = Url.CODE_GROUP_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.CD;

    private final CodeItemService codeItemService;

    @GetMapping(Url.CODE_ITEMS)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> codeItemListAjax(final @RequestParam("groupCode") String groupCode, final LogActvtyParam logParam) throws Exception {
        final CodeItemSearchParam searchParam = CodeItemSearchParam.builder().groupCode(groupCode).build();
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.ASC, "code"));
        final List<CodeItemDto> codeItemList = codeItemService.getListDto(searchParam, sort);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(codeItemList));
    }

    @PostMapping(value = {Url.CODE_ITEMS})
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> codeItemRegAjax(final @Valid CodeItemDto codeItemDto, final LogActvtyParam logParam) throws Exception {
        final ServiceResponse result = codeItemService.regist(codeItemDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @PostMapping(value = {Url.CODE_ITEM})
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> codeItemMdfAjax(final @Valid CodeItemDto codeItemDto, final LogActvtyParam logParam) throws Exception {
        final ServiceResponse result = codeItemService.modify(codeItemDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @GetMapping(Url.CODE_ITEM)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> codeItemDtlAjax(final @RequestParam("id") Integer id, final LogActvtyParam logParam) throws Exception {
        final CodeItemDto codeItemDto = codeItemService.getDtlDto(id);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(codeItemDto));
    }

    @DeleteMapping(Url.CODE_ITEM)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> codeItemDelAjax(final @RequestParam("id") Integer id, final LogActvtyParam logParam) throws Exception {
        final ServiceResponse result = codeItemService.delete(id);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
        return ResponseEntity.ok(AjaxResponse.fromResponse(result, rsltMsg));
    }

    @PutMapping(Url.CODE_ITEMS_SORT_ORDERS)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> codeItemSortOrdrAjax(final @RequestBody CodeItemParam codeItemParam, final LogActvtyParam logParam) throws Exception {
        final ServiceResponse result = codeItemService.sortOrder(codeItemParam.getSortOrders());
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }
}
