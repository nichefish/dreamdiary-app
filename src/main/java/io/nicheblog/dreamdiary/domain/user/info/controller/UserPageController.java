 package io.nicheblog.dreamdiary.domain.user.info.controller;

 import io.nicheblog.dreamdiary.auth.security.service.AuthRoleService;
 import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
 import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
 import io.nicheblog.dreamdiary.domain.user.info.model.UserDto;
 import io.nicheblog.dreamdiary.domain.user.info.model.UserSearchParam;
 import io.nicheblog.dreamdiary.domain.user.info.service.UserService;
 import io.nicheblog.dreamdiary.global.Constant;
 import io.nicheblog.dreamdiary.global.Url;
 import io.nicheblog.dreamdiary.global._common.cd.service.DtlCdService;
 import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
 import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
 import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyPageControllerAspect;
 import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
 import io.nicheblog.dreamdiary.global.model.PaginationInfo;
 import io.nicheblog.dreamdiary.global.util.MessageUtils;
 import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
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
 import org.springframework.web.bind.annotation.RequestParam;

 import java.util.HashMap;
 import java.util.Map;

/**
 * UserPageController
 * <pre>
 *  사용자 관리 > 계정 및 권한 관리 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyPageControllerAspect
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class UserPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER;      // 작업 카테고리 (로그 적재용)

    private final UserService userService;
    private final AuthRoleService authRoleService;
    private final DtlCdService dtlCdService;

    /**
     * 사용자 관리 > 계정 및 권한 관리 > 사용자 목록 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.USER_LIST)
    @Secured(Constant.ROLE_MNGR)
    public String userList(
            @ModelAttribute("searchParam") UserSearchParam searchParam,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.USER_INFO);
        model.addAttribute("pageNm", PageNm.LIST);

        // 상세/수정 화면에서 목록 화면 복귀시 세션에 목록 검색 인자 저장해둔 거 있는지 체크
        searchParam = (UserSearchParam) CmmUtils.Param.checkPrevSearchParam(baseUrl, searchParam);
        // 페이징 정보 생성:: 공백시 pageSize=10, pageNo=1
        final Sort sort = Sort.by(Sort.Direction.ASC, "acntStus.cfYn")
                .and(Sort.by(Sort.Direction.ASC, "acntStus.lockedYn"))
                .and(Sort.by(Sort.Direction.DESC, "regDt"));
        final PageRequest pageRequest = CmmUtils.Param.getPageRequest(searchParam, sort, model);
        // 목록 조회
        final Page<UserDto.LIST> userList = userService.getPageDto(searchParam, pageRequest);
        model.addAttribute("userList", userList.getContent());
        model.addAttribute(Constant.PAGINATION_INFO, new PaginationInfo(userList));
        // 코드 정보 모델에 추가
        dtlCdService.setCdListToModel(Constant.AUTH_CD, model);
        dtlCdService.setCdListToModel(Constant.TEAM_CD, model);
        dtlCdService.setCdListToModel(Constant.EMPLYM_CD, model);
        dtlCdService.setCdListToModel(Constant.RANK_CD, model);
        // 목록 검색 URL + 파라미터 모델에 추가
        CmmUtils.Param.setModelAttrMap(searchParam, baseUrl, model);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/user/user_list";
    }

    /**
     * 사용자 관리 > 계정 및 권한 관리 > 사용자 등록 화면 조회
     * (관리자MNGR만 접근 가능.)
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.USER_REG_FORM)
    @Secured(Constant.ROLE_MNGR)
    public String userRegForm(
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.USER_INFO);
        model.addAttribute("pageNm", PageNm.REG);

        // 빈 객체 주입 (freemarker error prevention)
        model.addAttribute("user", new UserDto.DTL());
        // 등록/수정 화면 플래그 세팅
        model.addAttribute(Constant.FORM_MODE, "regist");
        // 권한 정보 모델에 추가
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("useYn", "Y");
        }};
        model.addAttribute("authRoleList", authRoleService.getListDto(searchParamMap));
        // 코드 정보 모델에 추가
        dtlCdService.setCdListToModel(Constant.AUTH_CD, model);
        dtlCdService.setCdListToModel(Constant.TEAM_CD, model);
        dtlCdService.setCdListToModel(Constant.EMPLYM_CD, model);
        dtlCdService.setCdListToModel(Constant.RANK_CD, model);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/user/user_reg_form";
    }

    /**
     * 사용자 관리 > 계정 및 권한 관리 > 사용자 상세 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.USER_DTL)
    @Secured(Constant.ROLE_MNGR)
    public String userDtl(
            final @RequestParam("userNo") Integer key,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.USER_INFO);
        model.addAttribute("pageNm", PageNm.DTL);

        // 상세 조회 및 모델에 추가
        final UserDto retrievedDto = userService.getDtlDto(key);
        model.addAttribute("user", retrievedDto);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/user/user_dtl";
    }

    /**
     * 사용자 관리 > 계정 및 권한 관리 > 사용자 수정 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.USER_MDF_FORM)
    @Secured(Constant.ROLE_MNGR)
    public String userMdfForm(
            final @RequestParam("userNo") Integer key,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.USER_INFO);
        model.addAttribute("pageNm", PageNm.MDF);

        // 상세 조회 및 모델에 추가
        final UserDto rsDto = userService.getDtlDto(key);
        model.addAttribute("user", rsDto);
        // 등록/수정 화면 플래그
        model.addAttribute(Constant.FORM_MODE, "regist");
        // 권한 정보 모델에 추가
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("useYn", "Y");
        }};
        model.addAttribute("authRoleList", authRoleService.getListDto(searchParamMap));
        // 코드 정보 모델에 추가
        dtlCdService.setCdListToModel(Constant.AUTH_CD, model);
        dtlCdService.setCdListToModel(Constant.TEAM_CD, model);
        dtlCdService.setCdListToModel(Constant.EMPLYM_CD, model);
        dtlCdService.setCdListToModel(Constant.RANK_CD, model);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return "/view/domain/user/user_reg_form";
    }
}
