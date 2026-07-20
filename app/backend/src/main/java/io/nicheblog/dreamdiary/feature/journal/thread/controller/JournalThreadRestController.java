package io.nicheblog.dreamdiary.feature.journal.thread.controller;

import io.nicheblog.dreamdiary.feature.attachable.viewer.handler.ViewerEventListener;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeItemDto;
import io.nicheblog.dreamdiary.feature.admin.code.service.CodeItemService;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadSearchParam;
import io.nicheblog.dreamdiary.feature.journal.thread.service.JournalThreadService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;
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
    private final CodeItemService codeItemService;

    /**
     * 저널 스레드 분류 목록 조회 (Ajax).
     * 관리 화면 API를 우회하지 않고 사용자 화면이 읽을 수 있는 전용 읽기 계약을 제공한다.
     *
     * @return {@link ResponseEntity} -- 현재 locale이 적용된 분류 코드 목록
     */
    @GetMapping(Url.JOURNAL_THREAD_CATEGORIES)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalThreadCategoryListAjax() {
        final List<CodeItemDto> categoryList = codeItemService.getCdDtoListByGroupCode("JOURNAL_THREAD_CTGR_CD");
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(categoryList));
    }

    /**
     * 저널 스레드 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * 추가(thread-1): Vue SPA 목록 조회용 REST 엔드포인트.
     *
     * @param searchParam 검색 조건 (categoryCode, searchKeyword 등)
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
}
