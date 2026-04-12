package io.nicheblog.dreamdiary.feature.jrnl.day.controller;

import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.my.MyJrnlDayQueryService;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.my.MyJrnlDayTagService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * JrnlDayTagRestController
 * <pre>
 *  저널 일자 태그 RestController.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class JrnlDayTagRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    private final MyJrnlDayQueryService myJrnlDayQueryService;
    private final MyJrnlDayTagService myJrnlDayTagService;

    /**
     * 저널 일자 태그 카테고리 맵 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JRNL_DAY_TAG_CTGR_MAP)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayTagCtgrMapAjax(
            //
    ) throws Exception {

        final Map<String, List<String>> tagCtgrMap = myJrnlDayTagService.getMyTagCtgrMap();
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withMap(tagCtgrMap));
    }

    /**
     * 저널 일자 태그 전체 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JRNL_DAY_TAGS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayTagListAjax(
            final @ModelAttribute("searchParam") TagSearchParam searchParam
    ) throws Exception {

        final List<TagDto> tagList = searchParam.hasWeekStartDt()
                ? myJrnlDayTagService.getMyWeeklySizedListDto(searchParam.getWeekStartDt())
                : myJrnlDayTagService.getMyYyMnthSizedListDto(searchParam.getYy(), searchParam.getMnth());
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(tagList));
    }

    /**
     * 저널 일자 태그 전체 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JRNL_DAY_TAG_GROUP_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayTagGroupListAjax(
            @ModelAttribute("searchParam") TagSearchParam searchParam
    ) throws Exception {

        final Map<String, List<TagDto>> tagGroupMap = searchParam.hasWeekStartDt()
                ? myJrnlDayTagService.getMyWeeklySizedGroupListDto(searchParam.getWeekStartDt())
                : myJrnlDayTagService.getMyYyMnthSizedGroupListDto(searchParam.getYy(), searchParam.getMnth());
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withMap(tagGroupMap));
    }

    /**
     * 저널 일자 태그가 존재하는 연도 목록 조회 (Ajax)
     *
     * @param tagNo 태그 번호
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JRNL_DAY_TAG_YYS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayTagYyListAjax(
            final @PathVariable("tagNo") Integer tagNo
    ) {

        final List<Integer> yyList = myJrnlDayTagService.getMyYyListByTagNo(tagNo);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(yyList));
    }

    /**
     * 저널 일자 태그 상세 (해당 태그 일자 목록) 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_DAY_TAG})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayListByTagNoAjax(
            final @PathVariable("tagNo") Integer tagNo,
            final JrnlDaySearchParam searchParam
    ) throws Exception {

        searchParam.setTagNo(tagNo);
        final List<JrnlDayDto> jrnlDayList = myJrnlDayQueryService.getMyListDtoByTagNoEnriched(searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(jrnlDayList));
    }
}
