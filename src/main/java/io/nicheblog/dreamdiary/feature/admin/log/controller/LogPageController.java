package io.nicheblog.dreamdiary.feature.admin.log.controller;

import io.nicheblog.dreamdiary.feature.admin.log.model.LogQueryDto;
import io.nicheblog.dreamdiary.feature.admin.log.service.LogQueryService;
import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogSearchParam;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.PaginationInfo;
import io.nicheblog.dreamdiary.infrastructure.web.util.ParamUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * LogPageController
 * <pre>
 *  로그 관리 > 활동 로그 관리 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class LogPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.LOG_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LOG;        // 작업 카테고리 (로그 적재용)

    private final LogQueryService logQueryService;

    /**
     * 활동 로그 (전체) 목록 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.LOG_LIST)
    @Secured(Constant.ROLE_MNGR)
    public String logList(
            @ModelAttribute("searchParam") LogSearchParam searchParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.LOG_LIST);
        model.addAttribute("pageNm", PageNm.LIST);

        // 활동 로그 목록 조회
        // 상세/수정 화면에서 목록 화면 복귀시 세션에 목록 검색 인자 저장해둔 거 있는지 체크
        searchParam = (LogSearchParam) ParamUtils.checkPrevSearchParam(baseUrl, searchParam);
        // 페이징 정보 생성:: 공백시 pageSize=10, pageNo=1
        final PageRequest pageRequest = ParamUtils.getPageRequest(searchParam, "createdAt", model);
        // 목록 조회
        final Page<LogQueryDto> logList = logQueryService.getPageDto(searchParam, pageRequest);
        model.addAttribute("logList", logList.getContent());
        model.addAttribute(Constant.PAGINATION_INFO, new PaginationInfo(logList));
        // 목록 검색 URL + 파라미터 모델에 추가
        ParamUtils.setModelAttrMap(searchParam, baseUrl, model);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return "/view/feature/admin/log/log_list";
    }
}
