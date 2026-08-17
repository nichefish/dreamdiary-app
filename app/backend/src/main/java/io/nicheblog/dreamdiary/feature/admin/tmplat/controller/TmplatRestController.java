package io.nicheblog.dreamdiary.feature.admin.tmplat.controller;

import io.nicheblog.dreamdiary.feature.admin.tmplat.model.TmplatDto;
import io.nicheblog.dreamdiary.feature.admin.tmplat.model.TmplatSearchParam;
import io.nicheblog.dreamdiary.feature.admin.tmplat.service.TmplatService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * TmplatRestController
 * <pre>
 *  템플릿 관리 (전역 공용) REST 컨트롤러.
 *  관리 CRUD 는 {@code menu.admin.tmplat} 권한으로 보호하고,
 *  에디터 소비용 활성 목록({@code active})은 인증 사용자면 조회할 수 있게 분리한다.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class TmplatRestController extends BaseControllerImpl {
    @Getter
    private final String baseUrl = Url.TMPLAT_ADMIN_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.TMPLAT;

    private final TmplatService tmplatService;

    /**
     * 템플릿 목록 조회 (관리)
     *
     * @param searchParam 검색 조건 파라미터
     * @return {@link ResponseEntity} -- 정렬순서 오름차순 템플릿 목록
     */
    @GetMapping(Url.TMPLATS)
    @PreAuthorize("hasAuthority('menu.admin.tmplat')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> tmplatListAjax(
            final @ModelAttribute TmplatSearchParam searchParam
    ) throws Exception {
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.ASC, "id"));
        final List<TmplatDto> tmplatList = tmplatService.getListDto(searchParam, sort);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withList(tmplatList));
    }

    /**
     * 에디터 소비용 활성 템플릿 목록 조회 (인증 사용자)
     *
     * @return {@link ResponseEntity} -- useYn=Y 인 템플릿을 정렬순서 오름차순으로 반환
     */
    @GetMapping(Url.TMPLATS_ACTIVE)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<AjaxResponse> tmplatActiveListAjax() throws Exception {
        final TmplatSearchParam searchParam = TmplatSearchParam.builder().useYn("Y").build();
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.ASC, "id"));
        final List<TmplatDto> tmplatList = tmplatService.getListDto(searchParam, sort);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withList(tmplatList));
    }

    /**
     * 템플릿 상세 조회 (관리)
     *
     * @param id 템플릿 ID
     * @return {@link ResponseEntity} -- 템플릿 상세 정보
     */
    @GetMapping(Url.TMPLAT)
    @PreAuthorize("hasAuthority('menu.admin.tmplat')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> tmplatDtlAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {
        final TmplatDto tmplat = tmplatService.getDtlDto(id);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(tmplat));
    }

    /**
     * 템플릿 등록 (관리)
     *
     * @param tmplatDto 등록할 템플릿 정보
     * @return {@link ResponseEntity} -- 등록 결과
     */
    @PostMapping(Url.TMPLATS)
    @PreAuthorize("hasAuthority('menu.admin.tmplat')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> tmplatRegAjax(
            final @Valid TmplatDto tmplatDto
    ) throws Exception {
        final ServiceResponse result = tmplatService.regist(tmplatDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 템플릿 수정 (관리)
     *
     * @param id 템플릿 ID
     * @param tmplatDto 수정할 템플릿 정보
     * @return {@link ResponseEntity} -- 수정 결과
     */
    @PostMapping(Url.TMPLAT)
    @PreAuthorize("hasAuthority('menu.admin.tmplat')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> tmplatMdfAjax(
            final @PathVariable("id") Integer id,
            final @Valid TmplatDto tmplatDto
    ) throws Exception {
        tmplatDto.setId(id);
        final ServiceResponse result = tmplatService.modify(tmplatDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");
        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 템플릿 삭제 (관리) :: soft-delete
     *
     * @param id 템플릿 ID
     * @return {@link ResponseEntity} -- 삭제 결과
     */
    @DeleteMapping(Url.TMPLAT)
    @PreAuthorize("hasAuthority('menu.admin.tmplat')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> tmplatDelAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {
        final ServiceResponse result = tmplatService.delete(id);
        final String rsltMsg = MessageUtils.getMessage("common.result.success");
        return ResponseEntity.ok(AjaxResponse.fromResponse(result, rsltMsg));
    }
}