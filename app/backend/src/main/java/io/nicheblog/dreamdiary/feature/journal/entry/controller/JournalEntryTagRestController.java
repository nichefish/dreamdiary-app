package io.nicheblog.dreamdiary.feature.journal.entry.controller;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagSearchParam;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryTagQuery;
import io.nicheblog.dreamdiary.feature.journal.entry.service.my.JournalEntryMyTagService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypeResolver;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 로그인 사용자의 엔트리 태그 목록·카테고리·기간별 태그클라우드를 제공한다.
 */
@RestController
@RequiredArgsConstructor
public class JournalEntryTagRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DAY_MONTHLY;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;

    private final JournalEntryMyTagService journalEntryMyTagService;
    private final JournalEntryTypeResolver typeResolver;

    /**
     * 엔트리 태그 카테고리 맵을 조회한다.
     *
     * @param type 엔트리 타입(DIARY/DREAM 또는 JOURNAL_DIARY/JOURNAL_DREAM)
     * @return Ajax 응답
     * @throws Exception 조회 중 예외
     */
    @GetMapping(Url.JOURNAL_ENTRY_TAG_CATEGORIES)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalEntryTagCategoryMapAjax(
            final @RequestParam("type") String type
    ) throws Exception {
        return tagCategoryMapAjax(typeResolver.resolveByRawType(type));
    }

    /**
     * 엔트리 태그 목록을 조회한다.
     *
     * @param searchParam 검색 조건
     * @param type 엔트리 타입(DIARY/DREAM 또는 JOURNAL_DIARY/JOURNAL_DREAM)
     * @return Ajax 응답
     * @throws Exception 조회 중 예외
     */
    @GetMapping(Url.JOURNAL_ENTRY_TAGS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> tagListAjax(
            final @ModelAttribute("searchParam") TagSearchParam searchParam,
            final @RequestParam("type") String type
    ) throws Exception {
        return doTagListAjax(searchParam, typeResolver.resolveByRawType(type));
    }

    /**
     * 엔트리 태그 그룹 목록을 조회한다.
     *
     * @param searchParam 검색 조건
     * @param type 엔트리 타입(DIARY/DREAM 또는 JOURNAL_DIARY/JOURNAL_DREAM)
     * @return Ajax 응답
     * @throws Exception 조회 중 예외
     */
    @GetMapping(Url.JOURNAL_ENTRY_TAG_GROUP_LIST_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> tagGroupListAjax(
            final @ModelAttribute("searchParam") TagSearchParam searchParam,
            final @RequestParam("type") String type
    ) throws Exception {
        return doTagGroupListAjax(searchParam, typeResolver.resolveByRawType(type));
    }

    /**
     * 콘텐츠 타입별 태그 카테고리 맵 응답을 만든다.
     *
     * @param contentType 콘텐츠 타입
     * @return Ajax 응답
     * @throws Exception 조회 중 예외
     */
    private ResponseEntity<AjaxResponse> tagCategoryMapAjax(final ContentType contentType) throws Exception {
        final Map<String, List<String>> tagCategoryMap = journalEntryMyTagService.getMyTagCategoryMap(contentType);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withMap(tagCategoryMap));
    }

    /**
     * 기간 조건 여부에 따라 태그 목록 조회 방식을 선택한다.
     *
     * @param searchParam 검색 조건
     * @param contentType 콘텐츠 타입
     * @return Ajax 응답
     * @throws Exception 조회 중 예외
     */
    private ResponseEntity<AjaxResponse> doTagListAjax(
            final TagSearchParam searchParam,
            final ContentType contentType
    ) throws Exception {
        final List<TagDto> tagList = searchParam.hasStdrdDt() || searchParam.hasWeekStartDt() || searchParam.hasYyMnth()
                ? journalEntryMyTagService.getMySizedTagList(toTagQuery(searchParam, contentType))
                : journalEntryMyTagService.getMyTagList(contentType);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withList(tagList));
    }

    /**
     * 태그를 카테고리별 그룹으로 묶어 응답한다.
     *
     * @param searchParam 검색 조건
     * @param contentType 콘텐츠 타입
     * @return Ajax 응답
     * @throws Exception 조회 중 예외
     */
    private ResponseEntity<AjaxResponse> doTagGroupListAjax(
            final TagSearchParam searchParam,
            final ContentType contentType
    ) throws Exception {
        final Map<String, List<TagDto>> tagGroupMap = journalEntryMyTagService.getMySizedTagGroupMap(toTagQuery(searchParam, contentType));
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withMap(tagGroupMap));
    }

    /**
     * 검색 파라미터를 태그 질의 객체로 변환한다.
     *
     * @param searchParam 검색 조건
     * @param contentType 콘텐츠 타입
     * @return 태그 질의 객체
     */
    private JournalEntryTagQuery toTagQuery(final TagSearchParam searchParam, final ContentType contentType) {
        if (searchParam.hasStdrdDt()) {
            return JournalEntryTagQuery.daily(contentType, searchParam.getStdrdDt());
        }
        if (searchParam.hasWeekStartDt()) {
            return JournalEntryTagQuery.weekly(contentType, searchParam.getWeekStartDt());
        }
        return JournalEntryTagQuery.of(contentType, searchParam.getYy(), searchParam.getMnth());
    }

}
