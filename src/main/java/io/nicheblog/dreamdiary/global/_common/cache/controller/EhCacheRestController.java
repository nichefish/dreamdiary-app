package io.nicheblog.dreamdiary.global._common.cache.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.cache.model.CacheParam;
import io.nicheblog.dreamdiary.global._common.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

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
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.ADMIN;        // 작업 카테고리 (로그 적재용)

    /**
     * 사이트 캐시 목록 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     * 
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.CACHE_ACTIVE_LIST_AJAX)
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
        final String key = cacheParam.getKey();
        final String cacheName = cacheParam.getCacheName();
        if ("-".equals(key)) {
            EhCacheUtils.evictCacheAll(cacheName);
        } else {
            // 숫자 형태의 키인 경우 Integer로 변환하여 처리할 수 있음
            if (key.matches("\\d+")) {
                EhCacheUtils.evictCache(cacheName, Integer.valueOf(key));
            } else {
                // 문자열 키 처리
                EhCacheUtils.evictCache(cacheName, key);
            }
        }
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
