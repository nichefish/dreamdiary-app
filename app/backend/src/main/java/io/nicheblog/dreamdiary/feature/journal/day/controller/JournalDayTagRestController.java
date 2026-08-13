package io.nicheblog.dreamdiary.feature.journal.day.controller;

import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagSearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayTagQuery;
import io.nicheblog.dreamdiary.feature.journal.day.service.my.MyJournalDayQueryService;
import io.nicheblog.dreamdiary.feature.journal.day.service.my.MyJournalDayTagService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
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

import java.util.List;
import java.util.Map;

/**
 * JournalDayTagRestController
 * <pre>
 *  저널 일자 태그 RestController.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class JournalDayTagRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    private final MyJournalDayQueryService myJournalDayQueryService;
    private final MyJournalDayTagService myJournalDayTagService;

    /**
     * 저널 일자 태그 카테고리 맵 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JOURNAL_DAY_TAG_CATEGORIES)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayTagCategoryMapAjax(
            //
    ) throws Exception {

        final Map<String, List<String>> tagCategoryMap = myJournalDayTagService.getMyTagCategoryMap();
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withMap(tagCategoryMap));
    }

    /**
     * 저널 일자 태그 전체 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JOURNAL_DAY_TAGS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayTagListAjax(
            final @ModelAttribute("searchParam") TagSearchParam searchParam
    ) throws Exception {

        final List<TagDto> tagList = myJournalDayTagService.getMySizedTagList(toTagQuery(searchParam));
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(tagList));
    }

    /**
     * 저널 일자 태그 전체 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JOURNAL_DAY_TAG_GROUP_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayTagGroupListAjax(
            @ModelAttribute("searchParam") TagSearchParam searchParam
    ) throws Exception {

        final Map<String, List<TagDto>> tagGroupMap = myJournalDayTagService.getMySizedTagGroupMap(toTagQuery(searchParam));
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withMap(tagGroupMap));
    }

    /**
     * 저널 일자 태그가 존재하는 연도 목록 조회 (Ajax)
     *
     * @param tagId 태그 ID
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JOURNAL_DAY_TAG_YYS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayTagYyListAjax(
            final @PathVariable Integer tagId
    ) {

        final List<Integer> yyList = myJournalDayTagService.getMyYyListByTagId(tagId);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(yyList));
    }

    /**
     * 저널 일자 태그 상세 (해당 태그 일자 목록) 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_DAY_TAG})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayListByTagIdAjax(
            final @PathVariable Integer tagId,
            final JournalDaySearchParam searchParam
    ) throws Exception {

        searchParam.setTagId(tagId);
        final List<JournalDayDto> journalDayList = myJournalDayQueryService.getMyListDtoByTagIdEnriched(searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(journalDayList));
    }

    /**
     * 일간·주간·월간 검색 파라미터를 태그 기간 질의로 변환한다.
     * 더 좁은 기준일자 조건을 주간·연월 조건보다 우선한다.
     *
     * @param searchParam 태그 검색 조건
     * @return 태그 기간 질의
     */
    private JournalDayTagQuery toTagQuery(final TagSearchParam searchParam) {
        if (searchParam.hasStdrdDt()) {
            return JournalDayTagQuery.daily(searchParam.getStdrdDt());
        }
        if (searchParam.hasWeekStartDt()) {
            return JournalDayTagQuery.weekly(searchParam.getWeekStartDt());
        }
        return JournalDayTagQuery.of(searchParam.getYy(), searchParam.getMnth());
    }
}
