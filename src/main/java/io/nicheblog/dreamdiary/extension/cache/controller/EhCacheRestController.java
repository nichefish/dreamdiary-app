package io.nicheblog.dreamdiary.extension.cache.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.extension.cache.model.CacheParam;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * EhCacheRestController
 * <pre>
 *  ehCache 수동 관리 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@Log4j2
public class EhCacheRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.ADMIN_PAGE;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.CACHE;        // 작업 카테고리 (로그 적재용)

    /**
     * 사이트 캐시 목록 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.CACHE_ACTIVE_MAP_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> cacheActiveListAjax(
            final LogActvtyParam logParam
    ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        // 현재 활성 중인 캐시(name) 목록 조회 :: 성공시 처리완료목록으로 출력
        final Map<String, Object> activeCacheList = EhCacheUtils.getActiveCacheMap();
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setRsltMap(activeCacheList);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 사이트 캐시 상세 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param cacheParam 캐시 조회 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.CACHE_ACTIVE_DTL_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> cacheActiveDtlAjax(
            final CacheParam cacheParam,
            final LogActvtyParam logParam
    ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        // 현재 활성 중인 캐시(name) 목록 조회 :: 성공시 처리완료목록으로 출력
        final Object activeCache = EhCacheUtils.getObjectFromCache(cacheParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(activeCache);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 사이트 캐시 삭제 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param cacheParam 삭제할 캐시의 이름 / 키. (전체 삭제시 "-")
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.CACHE_EVICT_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> cacheEvictAjax(
            final CacheParam cacheParam,
            final LogActvtyParam logParam
    ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        // 캐시 evict
        EhCacheUtils.evictCache(cacheParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 사이트 캐시 삭제 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param cacheParam 삭제할 캐시의 이름 / 키. (전체 삭제시 "-")
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.CACHE_CLEAR_BY_NM_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> cacheClearByNmAjax(
            final CacheParam cacheParam,
            final LogActvtyParam logParam
    ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        // 캐시 evict
        EhCacheUtils.evictCacheAll(cacheParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 사이트 캐시 전체 초기화 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.CACHE_CLEAR_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> cacheClearAjax(
            final LogActvtyParam logParam
    ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final List<String> activeCacheList = EhCacheUtils.chckActiveCacheNm();
        final boolean isSuccess = EhCacheUtils.clearAllCaches();
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setRsltList(activeCacheList);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(ajaxResponse);
    }

}
