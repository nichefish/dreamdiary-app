package io.nicheblog.dreamdiary.feature.journal.diary.controller;

import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagSearchParam;
import io.nicheblog.dreamdiary.feature.journal.diary.service.my.MyJournalDiaryTagService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * JournalDiaryTagRestController
 * <pre>
 *  저널 일기 태그 RestController.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JournalDiaryTagRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    private final MyJournalDiaryTagService myJournalDiaryTagService;

    /**
     * 저널 일기 태그 카테고리 맵 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JOURNAL_DIARY_TAG_CTGR_MAP)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDiaryTagCtgrMapAjax(
            //
    ) throws Exception {

        final Map<String, List<String>> tagCtgrMap = myJournalDiaryTagService.getMyTagCtgrMap();
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withMap(tagCtgrMap));
    }

    /**
     * 저널 일기 태그 전체 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JOURNAL_DIARY_TAGS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> tagListAjax(
            final @ModelAttribute("searchParam") TagSearchParam searchParam
    ) throws Exception {

        List<TagDto> tagList;
        if (searchParam.hasWeekStartDt()) {
            tagList = myJournalDiaryTagService.getMyWeeklySizedListDto(searchParam.getWeekStartDt());
        } else if (searchParam.hasYyMnth()) {
            tagList = myJournalDiaryTagService.getMyDiarySizedListDto(searchParam.getYy(), searchParam.getMnth());
        } else {
            tagList = myJournalDiaryTagService.getMyTagList();
        }

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(tagList));
    }

    /**
     * 저널 일기 태그 전체 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JOURNAL_DIARY_TAG_GROUP_LIST_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> tagGroupListAjax(
            final @ModelAttribute("searchParam") TagSearchParam searchParam
    ) throws Exception {

        final Map<String, List<TagDto>> tagGroupMap = searchParam.hasWeekStartDt()
                ? myJournalDiaryTagService.getMyWeeklySizedGroupListDto(searchParam.getWeekStartDt())
                : myJournalDiaryTagService.getMyDiarySizedGroupListDto(searchParam.getYy(), searchParam.getMnth());
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withMap(tagGroupMap));
    }
}

