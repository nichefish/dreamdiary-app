package io.nicheblog.dreamdiary.feature.journal.day.controller;

import io.nicheblog.dreamdiary.feature.clsf.meta.model.MetaDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagSearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayMetaService;
import io.nicheblog.dreamdiary.feature.journal.day.service.my.MyJournalDayMetaService;
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
 * JournalDayMetaRestController
 * <pre>
 *  저널 일자 메타 RestController.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class JournalDayMetaRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    private final JournalDayMetaService journalDayMetaService;
    private final MyJournalDayMetaService myJournalDayMetaService;

    /**
     * 저널 일자 메타 전체 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JOURNAL_DAY_METAS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayMetaListAjax(
            final @ModelAttribute("searchParam") TagSearchParam searchParam
    ) throws Exception {

        final List<MetaDto> tagList = journalDayMetaService.getListDto(searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(tagList));
    }

    /**
     * 저널 일자 메타 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JOURNAL_DAY_META)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayMetaDtlAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final MetaDto retrievedDto = journalDayMetaService.getDtlDto(id);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 일자 메타가 존재하는 연도 목록 조회 (Ajax)
     *
     * @param id 메타 ID
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JOURNAL_DAY_META_YYS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayMetaYyListAjax(
            final @PathVariable("id") Integer id
    ) {

        final List<Integer> yyList = myJournalDayMetaService.getMyYyListByMetaId(id);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(yyList));
    }


    /**
     * 저널 일자 메타 카테고리 맵 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.JOURNAL_DAY_META_CTGR_MAP)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalDayMetaCtgrMapAjax(
            //
    ) throws Exception {

        final Map<String, List<String>> metaCtgrMap = myJournalDayMetaService.getMyMetaCtgrMap();
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withMap(metaCtgrMap));
    }
}

