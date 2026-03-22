package io.nicheblog.dreamdiary.feature.clsf.tag.controller;

import io.nicheblog.dreamdiary.auth.AuthConstant;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagPropertyDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagPropertyService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * TagPropRestController
 * <pre>
 *  태그 속성 관리 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class TagPropRestController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.TAG;           // 작업 카테고리 (로그 적재용)

    private final TagPropertyService tagPropertyService;

    /**
     * 태그 속성 등록/수정 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param tagProperty 등록/수정 처리할 객체
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.TAG_PROPERTY_REG_AJAX, Url.TAG_PROPERTY_MDF_AJAX})
    @Secured({AuthConstant.ROLE_USER, AuthConstant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> TagPropertyRegAjax(
            final @Valid TagPropertyDto tagProperty,
            final @RequestParam("tagPropertyNo") Integer key
    ) throws Exception {

        final boolean isReg = key == null;
        final ServiceResponse result = isReg ? tagPropertyService.regist(tagProperty) : tagPropertyService.modify(tagProperty);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 태그 속성 상세 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.TAG_PROPERTY_DTL_AJAX)
    @Secured({AuthConstant.ROLE_USER, AuthConstant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> TagDtlAjax(
            final @RequestParam("tagPropertyNo") Integer key
    ) throws Exception {

        final TagPropertyDto tagDto = tagPropertyService.getDtlDto(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(tagDto));
    }

    /**
     * 태그 속성 삭제 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.TAG_PROPERTY_DEL_AJAX)
    @Secured({AuthConstant.ROLE_USER, AuthConstant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> TagPropertyDelAjax(
            final @RequestParam("tagPropertyNo") Integer key

    ) throws Exception {

        final TagPropertyDto tagDto = tagPropertyService.getDtlDto(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(tagDto));
    }

}
