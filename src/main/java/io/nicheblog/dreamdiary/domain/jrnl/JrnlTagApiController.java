package io.nicheblog.dreamdiary.domain.jrnl;

import io.nicheblog.dreamdiary.domain.jrnl.day.service.impl.JrnlDayTagCtgrSynchronizer;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.impl.JrnlDiaryTagCtgrSynchronizer;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.impl.JrnlDreamTagCtgrSynchronizer;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.TagSearchParam;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.HttpUtils;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * JrnlTagApiController
 * <pre>
 *  저널 태그 API Controller.
 * </pre>
 *
 * @see LogActvtyRestControllerAspect
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JrnlTagApiController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_DAY_PAGE;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    private final JrnlDreamTagCtgrSynchronizer jrnlDreamTagCtgrSynchronizer;
    private final JrnlDiaryTagCtgrSynchronizer jrnlDiaryTagCtgrSynchronizer;
    private final JrnlDayTagCtgrSynchronizer jrnlDayTagCtgrSynchronizer;

    /**
     * 저널 태그 카테고리 메타 파일 - DB 동기화 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.JRNL_TAG_CTGR_SYNC_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> tagCtgrSyncAjax(
            final @ModelAttribute("searchParam") TagSearchParam searchParam,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        // 저널 일자 태그 동기화
        jrnlDayTagCtgrSynchronizer.tagSync();
        // 저널 일기 태그 동기화
        jrnlDiaryTagCtgrSynchronizer.tagSync();
        // 저널 꿈 태그 동기화
        jrnlDreamTagCtgrSynchronizer.tagSync();

        // 브라우저 캐시 초기화 처리
        HttpUtils.setInvalidateBrowserCacheHeader(response);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }
}
