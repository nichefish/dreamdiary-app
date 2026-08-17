package io.nicheblog.dreamdiary.feature.journal.thread.controller;

import io.nicheblog.dreamdiary.feature.attachable.viewer.handler.ViewerEventListener;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadCandidateDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadSearchParam;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadPeriodSummaryDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDayViewType;
import io.nicheblog.dreamdiary.feature.journal.thread.service.JournalThreadEntryService;
import io.nicheblog.dreamdiary.feature.journal.thread.service.JournalThreadExportService;
import io.nicheblog.dreamdiary.feature.journal.thread.service.JournalThreadService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JournalThreadRestController
 * <pre>
 *  저널 스레드 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class JournalThreadRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JOURNAL_THREAD_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;      // 작업 카테고리 (로그 적재용)

    private final JournalThreadService journalThreadService;
    private final JournalThreadEntryService journalThreadEntryService;
    private final JournalThreadExportService journalThreadExportService;

    /**
     * 엔트리 소속 메뉴용 저널 스레드 후보 조회 (Ajax).
     * <p>
     * 현재 소속 여부, 최근 소속 추가 시각, 활성 소속 수와 스레드 수정 시각을
     * 서버에서 집계해 결정적인 우선순위로 반환한다.
     * 기본은 완료 스레드를 숨기고, {@code includeResolved=true} 일 때만 포함한다.
     * 각 후보에 {@code lifecycleKey} 를 실어 보낸다.
     * </p>
     *
     * @param entryId 후보를 요청한 엔트리 식별자
     * @param keyword 제목 검색어
     * @param prefixId 말머리 ID 필터
     * @param includeResolved 완료({@code RESOLVED}) 스레드 포함 여부
     * @param limit 최대 후보 수 (서버에서 1~20으로 제한)
     * @return {@link ResponseEntity} -- 경량 스레드 후보 목록
     */
    @GetMapping(Url.JOURNAL_THREAD_CANDIDATES)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalThreadCandidateListAjax(
            final @RequestParam("entryId") Integer entryId,
            final @RequestParam(value = "keyword", required = false) String keyword,
            final @RequestParam(value = "prefixId", required = false) Integer prefixId,
            final @RequestParam(value = "includeResolved", defaultValue = "false") Boolean includeResolved,
            final @RequestParam(value = "limit", defaultValue = "7") Integer limit
    ) throws Exception {

        final List<JournalThreadCandidateDto> resultList =
                journalThreadService.getCandidates(entryId, keyword, prefixId, includeResolved, limit);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(resultList));
    }

    /**
     * 월간·주간 저널 화면의 기간별 스레드 요약 조회 (Ajax).
     * <p>
     * LIST는 {@code yy}/{@code mnth}, WEEKLY는 {@code weekStartDt}를 사용한다.
     * 일자 화면의 표시·검색 필터와 무관한 기간 전체 활성 소속을 집계한다.
     *
     * @param viewType LIST 또는 WEEKLY
     * @param searchParam 월간 연·월 또는 주 시작일
     * @return {@link ResponseEntity} -- 기간별 스레드 요약
     */
    @GetMapping(Url.JOURNAL_THREAD_PERIOD_SUMMARY)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalThreadPeriodSummaryAjax(
            final @RequestParam("viewType") JournalDayViewType viewType,
            final JournalDaySearchParam searchParam
    ) throws Exception {

        final List<JournalThreadPeriodSummaryDto> resultList =
                journalThreadEntryService.getPeriodSummary(viewType, searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(resultList));
    }

    /**
     * 저널 스레드 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * 추가(thread-1): Vue SPA 목록 조회용 REST 엔드포인트.
     *
     * @param searchParam 검색 조건 (prefixId, searchKeyword, tagIds 등)
     * @param page 페이지 번호 (0-based, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return {@link ResponseEntity} -- Spring Page 직렬화 (content, totalElements, totalPages, number)
     */
    @GetMapping(Url.JOURNAL_THREAD_API_LIST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalThreadListAjax(
            @ModelAttribute final JournalThreadSearchParam searchParam,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size
    ) throws Exception {

        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        final Page<JournalThreadDto> pageResult = journalThreadService.getPageDto(searchParam, pageRequest);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(pageResult));
    }

    /**
     * 저널 스레드 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param journalThread 등록/수정 처리할 객체
     * @param request - Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.JOURNAL_THREAD_API_LIST, Url.JOURNAL_THREAD_API})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalThreadRegistAjax(
            final @Valid JournalThreadDto journalThread,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isRegist = (journalThread.getKey() == null);
        final ServiceResponse result = isRegist ? journalThreadService.regist(journalThread, request) : journalThreadService.modify(journalThread, request);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 저널 스레드 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see ViewerEventListener
     */
    @GetMapping(Url.JOURNAL_THREAD_API)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalThreadDetailAjax(
            final @PathVariable("id") Integer key
    ) throws Exception {

        final JournalThreadDto retrievedDto = journalThreadService.viewDetailPage(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 스레드 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(Url.JOURNAL_THREAD_API)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalThreadDeleteAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final ServiceResponse result = journalThreadService.delete(id);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 스레드 소속 엔트리 목록 조회 (Ajax) — 상세 화면 카드 표시용 full 엔트리.
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * <p>
     * {@code relatedThreadIds} 가 있으면 해당 연관 스레드(실제 1-hop 연관만)의 엔트리를 합성해 반환한다.
     * 빌려온 엔트리는 {@code sourceThreadId}에 연관 스레드 ID를 실어 프론트가 배지·제거 게이팅에 활용한다.
     * (설계 정본: docs/migration/journal/thread-relation.md §4)
     *
     * @param id 스레드 식별자
     * @param relatedThreadIds 뷰에 합성할 연관 스레드 ID 목록 (미전달/빈 목록이면 base만)
     * @return {@link ResponseEntity} -- 소속(+연관) 엔트리(JournalEntryDto) 목록, 일자순
     */
    @GetMapping(Url.JOURNAL_THREAD_ENTRIES)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalThreadEntryListAjax(
            final @PathVariable("id") Integer id,
            final @RequestParam(value = "relatedThreadIds", required = false) List<Integer> relatedThreadIds
    ) throws Exception {

        final List<JournalEntryDto> resultList = journalThreadEntryService.getEntriesByThread(id, relatedThreadIds);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(resultList));
    }

    /**
     * 엔트리를 스레드에 소속시킨다 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * <p>
     * 멱등하다. 이미 소속돼 있으면 변경 없이 성공으로 응답하고,
     * 해제됐던 소속이면 되살린다.
     *
     * @param id 스레드 식별자
     * @param entryId 엔트리 식별자
     * @param sortOrder 스레드 내 표시 순서 (미지정 시 엔트리 일자순 정렬)
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.JOURNAL_THREAD_ENTRIES)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalThreadEntryRegistAjax(
            final @PathVariable("id") Integer id,
            final @RequestParam("entryId") Integer entryId,
            final @RequestParam(value = "sortOrder", required = false) Integer sortOrder
    ) throws Exception {

        final ServiceResponse result = journalThreadEntryService.regist(id, entryId, sortOrder);
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 엔트리의 스레드 소속을 해제한다 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * <p>
     * 소프트 삭제이며 멱등하다. 이미 해제된 소속이면 변경 없이 성공으로 응답한다.
     *
     * @param id 스레드 식별자
     * @param entryId 엔트리 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(Url.JOURNAL_THREAD_ENTRY)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalThreadEntryDeleteAjax(
            final @PathVariable("id") Integer id,
            final @PathVariable("entryId") Integer entryId
    ) throws Exception {

        final ServiceResponse result = journalThreadEntryService.delete(id, entryId);
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 엔트리가 속한 스레드 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * <p>
     * 한 엔트리가 여러 스레드에 속할 수 있어 목록으로 돌려준다.
     *
     * @param entryId 엔트리 식별자
     * @return {@link ResponseEntity} -- 소속 목록 (등록 순)
     */
    @GetMapping(Url.JOURNAL_ENTRY_THREADS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalEntryThreadListAjax(
            final @PathVariable("entryId") Integer entryId
    ) throws Exception {

        final List<JournalThreadEntryDto> resultList = journalThreadEntryService.getListByEntry(entryId);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(resultList));
    }

    /**
     * 저널 스레드 텍스트 내보내기 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * <p>
     * 스레드 제목과 소속 엔트리를 텍스트 파일로 내보낸다. 상세 모달·독립 상세 화면의 다운로드 액션이 사용한다.
     * 소유권은 {@link JournalThreadEntryService#getEntriesByThread} 가 검증한다.
     *
     * @param id 스레드 식별자
     * @param includeReflection 해석 포함 여부 (기본 true). 포함 시 각 엔트리의 target 리플렉션 본문을 함께 내보낸다.
     * @return {@link ResponseEntity} -- text/plain 첨부 응답
     */
    @GetMapping(Url.JOURNAL_THREAD_EXPORT)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<byte[]> journalThreadExportTxtAjax(
            final @PathVariable("id") Integer id,
            final @RequestParam(value = "includeReflection", defaultValue = "true") boolean includeReflection
    ) throws Exception {

        final JournalThreadDto retrievedDto = journalThreadService.viewDetailPage(id);
        final List<JournalEntryDto> entries = journalThreadEntryService.getEntriesByThread(id);
        final String text = journalThreadExportService.buildTxt(retrievedDto, entries, includeReflection);
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        final String filename = "thread_" + id + "_@" + DateUtils.getCurrDateStr(DatePtn.PDATE) + ".txt";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}
