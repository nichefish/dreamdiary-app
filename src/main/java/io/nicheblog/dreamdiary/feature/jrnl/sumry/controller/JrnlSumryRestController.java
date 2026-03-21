package io.nicheblog.dreamdiary.feature.jrnl.sumry.controller;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.JrnlDayTagService;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiarySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.JrnlDiaryService;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.JrnlDiaryTagService;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.JrnlDreamService;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.JrnlDreamTagService;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.JrnlSumryTagType;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.model.JrnlSumryDto;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.model.JrnlSumrySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.service.JrnlSumryService;
import io.nicheblog.dreamdiary.feature.clsf.tag.handler.TagProcEventListener;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * JrnlSumryRestController
 * <pre>
 *  저널 결산 API Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JrnlSumryRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_SUMRY_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    private final JrnlSumryService jrnlSumryService;
    private final JrnlDiaryService jrnlDiaryService;
    private final JrnlDreamService jrnlDreamService;
    private final JrnlDayTagService jrnlDayTagService;
    private final JrnlDiaryTagService jrnlDiaryTagService;
    private final JrnlDreamTagService jrnlDreamTagService;

    /**
     * 저널 결산 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_SUMRIES})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryListAjax(
            final JrnlSumrySearchParam searchParam
    ) throws Exception {

        final List<JrnlSumryDto> jrnlSumryList = jrnlSumryService.getMyListDto(searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(jrnlSumryList));
    }

    /**
     * 저널 결산 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 년도
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_SUMRY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryDtlAjax(
            final @PathVariable("yy") Integer yy
    ) throws Exception {

        // 객체 조회 및 모델에 추가
        final JrnlSumryDto retrievedDto = jrnlSumryService.getDtlDtoByYy(yy);
        final boolean isSuccess = (retrievedDto.getPostNo() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 결산 중요 일기 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 년도
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_SUMRY_DIARIES})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryImprtcDiaryListAjax(
            final @PathVariable("yy") Integer yy,
            final @RequestParam(defaultValue = "true") boolean showImprtc,
            final @RequestParam(defaultValue = "false") boolean showRefrnc,
            final JrnlDiarySearchParam searchParam
    ) throws Exception {

        // 중요 일기 목록 조회
        searchParam.setYy(yy);
        searchParam.resolveStates(showImprtc, showRefrnc);
        final List<JrnlDiaryDto> mySumryDiaryList = jrnlDiaryService.getMySumryDiaryList(AuthUtils.getLgnUserId(), searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(mySumryDiaryList));
    }

    /**
     * 저널 결산 중요 꿈 목록  조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 년도
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_SUMRY_DREAMS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryImprtcDreamListAjax(
            final @PathVariable("yy") Integer yy,
            final @RequestParam(defaultValue = "true") boolean showImprtc,
            final @RequestParam(defaultValue = "false") boolean showRefrnc,
            final JrnlDreamSearchParam searchParam
    ) throws Exception {

        // 중요 꿈 목록 조회
        searchParam.setYy(yy);
        searchParam.resolveStates(showImprtc, showRefrnc);
        final List<JrnlDreamDto> imprtcDreamList = jrnlDreamService.getMySumryDreamList(AuthUtils.getLgnUserId(), searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(imprtcDreamList));
    }

    /**
     * 저널 결산 태그 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 년도
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_SUMRY_TAGS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryTagListAjax(
            final @PathVariable("yy") Integer yy,
            final @RequestParam("type") JrnlSumryTagType type
    ) throws Exception {

        // 태그 목록 조회
        List <TagDto> tagList = new ArrayList<>();
        switch(type) {
            case DAY -> tagList = jrnlDayTagService.getDaySizedListDto(yy, 99);
            case DIARY -> tagList = jrnlDiaryTagService.getDiarySizedListDto(AuthUtils.getLgnUserId(), yy, 99);
            case DREAM -> tagList = jrnlDreamTagService.getDreamSizedListDto(yy, 99);
        }
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(tagList));
    }

    /**
     * 특정 년도에 대해 저널 결산 생성 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param yy 년도
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.JRNL_SUMRY_MAKE_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryMakeAjax(
            final @RequestParam("yy") Integer yy
    ) throws Exception {

        final boolean isSuccess = jrnlSumryService.makeYySumry(yy);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 전체 년도에 대해 저널 결산 생성 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.JRNL_SUMRY_MAKE_TOTAL_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryMakeTotalAjax(
            //
    ) throws Exception {

        final boolean isSuccess = jrnlSumryService.makeTotalYySumry();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 저널 결산 꿈 기록 완료 처리 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.JRNL_SUMRY_DREAM_COMPT_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryDreamComptAjax(
            final @RequestParam("postNo") Integer postNo
    ) throws Exception {

        final boolean isSuccess = jrnlSumryService.dreamCompt(postNo);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 저널 결산 내용 수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param jrnlSumry 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see TagProcEventListener
     */
    @PostMapping(value = {Url.JRNL_SUMRY_REG_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryRegAjax(
            final @Valid JrnlSumryDto jrnlSumry
    ) throws Exception {

        final ServiceResponse result = jrnlSumryService.modify(jrnlSumry);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
