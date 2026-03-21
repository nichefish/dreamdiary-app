package io.nicheblog.dreamdiary.feature.flsys.controller;

import io.nicheblog.dreamdiary.feature.flsys.model.FlsysMetaDto;
import io.nicheblog.dreamdiary.feature.flsys.service.FlsysMetaService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.handler.LogActvtyEventListener;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * FlsysMetaController
 * <pre>
 *  파일시스템 메타 관리 컨트롤러.
 * </pre>
 * TODO: 보완 예정
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class FlsysMetaRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.FLSYS_HOME;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.FLSYS;        // 작업 카테고리 (로그 적재용)

    private final FlsysMetaService flsysMetaService;

    /**
     * 파일시스템 메타 정보 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param flsysMeta 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see LogActvtyEventListener
     */
    @PostMapping(value = {Url.FLSYS_META_REG_AJAX, Url.FLSYS_META_MDF_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> flsysMetaRegAjax(
            final @Valid FlsysMetaDto flsysMeta
    ) throws Exception {

        final BaseClsfKey key = flsysMeta.getClsfKey();
        final boolean isReg = key.getPostNo() == null;
        final ServiceResponse result = isReg ? flsysMetaService.regist(flsysMeta) : flsysMetaService.modify(flsysMeta);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 파일시스템 메타 정보 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see LogActvtyEventListener
     */
    @GetMapping(Url.FLSYS_META_DTL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> flsysMetaDtlAjax(
            final @RequestParam("postNo") Integer key
    ) throws Exception {

        // 정보 조회 및 응답에 세팅
        final FlsysMetaDto rsDto = flsysMetaService.getDtlDto(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(rsDto));
    }

    /**
     * 파일시스템 메타 정보 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see LogActvtyEventListener
     */
    @PostMapping(Url.FLSYS_META_DEL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> flsysMetaDelAjax(
            final @RequestParam("postNo") Integer postNo
        ) throws Exception {

        final ServiceResponse result = flsysMetaService.delete(postNo);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }
}
