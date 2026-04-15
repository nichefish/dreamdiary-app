package io.nicheblog.dreamdiary.feature.admin.code.controller;

import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupDto;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupSearchParam;
import io.nicheblog.dreamdiary.feature.admin.code.service.CodeGroupService;
import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CdLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
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
    private final String baseUrl = Url.CODE_GROUP_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.CD;

    private final CodeGroupService codeGroupService;
    private final CdLookupService cdLookupService;

    @GetMapping(Url.CODE_GROUP_LIST)
    @Secured({Constant.ROLE_MNGR})
    public String clCdList(
            @ModelAttribute("searchParam") CodeGroupSearchParam searchParam,
            final ModelMap model
    ) throws Exception {
        model.addAttribute("menuLabel", SiteMenu.CD);
        model.addAttribute("pageNm", PageNm.LIST);

        searchParam = (CodeGroupSearchParam) ParamUtils.checkPrevSearchParam(baseUrl, searchParam);
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        final PageRequest pageRequest = ParamUtils.getPageRequest(searchParam, sort, model);
        final Page<CodeGroupDto> codeGroupList = codeGroupService.getPageDto(searchParam, pageRequest);
        model.addAttribute("clCdList", codeGroupList.getContent());
        model.addAttribute(Constant.PAGINATION_INFO, new PaginationInfo(codeGroupList));
        ParamUtils.setModelAttrMap(searchParam, baseUrl, model);
        cdLookupService.setCdListToModel(Code.CL_CTGR_CD, model);

        return "/view/feature/admin/code/code_group_list";
    }
}
