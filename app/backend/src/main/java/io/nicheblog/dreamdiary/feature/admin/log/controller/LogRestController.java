package io.nicheblog.dreamdiary.feature.admin.log.controller;

import io.nicheblog.dreamdiary.feature.admin.log.model.LogQueryDto;
import io.nicheblog.dreamdiary.feature.admin.log.service.LogQueryService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogSearchParam;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * LogRestController
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class LogRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.LOG_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LOG;

    private final LogQueryService logQueryService;

    /**
     * 로그 목록 조회 (Ajax)
     */
    @GetMapping(Url.LOGS)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> getLogs(
            @ModelAttribute final LogSearchParam searchParam,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size
    ) throws Exception {
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        final Page<LogQueryDto> pageResult = logQueryService.getPageDto(searchParam, pageRequest);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(pageResult));
    }

    /**
     * 로그 상세 조회 (Ajax)
     */
    @GetMapping(Url.LOG)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> getLog(
            final @PathVariable Integer id
    ) throws Exception {

        final LogQueryDto rsDto = logQueryService.getDtlDto(id);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(rsDto));
    }
}
