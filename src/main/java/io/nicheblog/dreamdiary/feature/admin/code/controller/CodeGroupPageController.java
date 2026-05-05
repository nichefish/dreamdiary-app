package io.nicheblog.dreamdiary.feature.admin.code.controller;

import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupDto;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupSearchParam;
import io.nicheblog.dreamdiary.feature.admin.code.service.CodeGroupService;
import io.nicheblog.dreamdiary.feature.admin.menu.type.PageName;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.PaginationInfo;
import io.nicheblog.dreamdiary.infrastructure.web.util.ParamUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
@Log4j2
public class CodeGroupPageController extends BaseControllerImpl {
    @Getter
    private final String baseUrl = Url.CODE_ADMIN_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.CODE;

    private final CodeGroupService codeGroupService;
    private final CodeLookupService codeLookupService;

    @GetMapping(Url.CODE_ADMIN_PAGE)
    @Secured({Constant.ROLE_MNGR})
    public String codeGroupList(
            @ModelAttribute("searchParam") CodeGroupSearchParam searchParam,
            final ModelMap model
    ) throws Exception {
        model.addAttribute("menuLabel", SiteMenu.CODE);
        model.addAttribute("pageName", PageName.LIST);

        searchParam = (CodeGroupSearchParam) ParamUtils.checkPrevSearchParam(baseUrl, searchParam);
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        final PageRequest pageRequest = ParamUtils.getPageRequest(searchParam, sort, model);
        final Page<CodeGroupDto> codeGroupList = codeGroupService.getPageDto(searchParam, pageRequest);
        model.addAttribute("codeGroupList", codeGroupList.getContent());
        model.addAttribute(Constant.PAGINATION_INFO, new PaginationInfo(codeGroupList));
        ParamUtils.setModelAttrMap(searchParam, baseUrl, model);
        codeLookupService.setCdListToModel(Code.CL_CTGR_CD, model);

        return "/view/feature/admin/code/code_admin_page";
    }
}
