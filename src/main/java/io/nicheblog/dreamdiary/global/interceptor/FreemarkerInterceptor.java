package io.nicheblog.dreamdiary.global.interceptor;

import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.exception.MenuNotExistsException;
import io.nicheblog.dreamdiary.domain.admin.menu.model.MenuDto;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.domain.admin.menu.service.MenuService;
import io.nicheblog.dreamdiary.domain.board.def.service.BoardDefService;
import io.nicheblog.dreamdiary.domain.notice.service.NoticeService;
import io.nicheblog.dreamdiary.global.ActiveProfile;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.config.WebMvcContextConfig;
import io.nicheblog.dreamdiary.global.model.SiteAcsInfo;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * FreemarkerInterceptor
 * <pre>
 *  controller -> view로 가는 중간에 작용하는 인터셉터.
 *  (프리마커 관련 외에도 분류가 애매한 기타 로직 뭉뚱그려 수행)
 * </pre>
 *
 * @author nichefish
 * @see WebMvcContextConfig
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class FreemarkerInterceptor
        implements HandlerInterceptor {

    private final HttpSession session;

    private final ActiveProfile activeProfile;
    private final NoticeService noticeService;
    private final MenuService menuService;
    private final BoardDefService boardDefService;

    @Value("${release-date:20000101}")
    private String releaseDate;

    /**
     * postHandle : controller 요청 처리 후 view를 렌더링하기 전에 동작한다.
     */
    @Override
    public void postHandle(
            final @NotNull HttpServletRequest request,
            final @NotNull HttpServletResponse response,
            final @NotNull Object handler,
            final ModelAndView mav
    ) throws Exception {

        /* mav 정보 없을시 처리하지 않음 */
        if (mav == null) return;

        // 모든 페이지에 activeProfile, releaseDate, urlMap, messageMap 추가
        // @see "/templates/layout/head.ftlh"
        mav.addObject("activeProfile", activeProfile.getActive());
        mav.addObject("releaseDate", releaseDate);
        mav.addObject("urlMap", Url.getUrlMap());
        mav.addObject("messageMap", MessageUtils.getMessageMap());
        mav.addObject("constantMap", Constant.getConstantMap());

        /* 모바일 여부 체크 추가 (TODO: 현재 미사용중) */
        final Boolean isMobile = DeviceUtils.getCurrentDevice(request).isMobile();
        request.setAttribute(Constant.IS_MBL, isMobile);

        /* 이후는 로그인 상태에서만 진행 */
        if (!AuthUtils.isAuthenticated()) return;

        /* 사용자 권한 정보 모델에 추가 */
        final AuthInfo authInfo = (AuthInfo) session.getAttribute("authInfo");
        final Boolean isMngr = authInfo.getIsMngr();
        mav.addObject("isMngr", isMngr);
        mav.addObject("isDev", authInfo.getIsDev());

        /* 메뉴접근 정보 읽어서 관리자/사용자 모드 세션에 세팅 */
        final SiteMenu menuLabel = (SiteMenu) mav.getModel().get("menuLabel");
        final PageNm pageNm = (PageNm) mav.getModel().get("pageNm");
        if (menuLabel != null) {
            try {
                final MenuDto menu = menuService.getMenuByLabel(menuLabel);
                SiteAcsInfo acsInfo;
                if (menuLabel == SiteMenu.BOARD) {
                    final String boardDef = (String) mav.getModel().get("boardDef");
                    acsInfo = boardDefService.getMenuByBoardDef(boardDef);
                } else {
                    acsInfo = menuService.getSiteAceInfoFromMenu(menu);
                }
                if (pageNm != null) acsInfo.setAcsPageInfo(pageNm);
                mav.addObject("siteAcsInfo", acsInfo);
                final boolean isMngrMenu = menuService.getIsMngrMenu(menu.getMenuNo());
                final String userMode = isMngrMenu ? Constant.AUTH_MNGR : Constant.AUTH_USER;
                session.setAttribute("userMode", userMode);
                mav.addObject("isMngrMode", Constant.AUTH_MNGR.equals(userMode));
            } catch (final MenuNotExistsException e) {
                log.error(MessageUtils.getExceptionMsg(e));
            }
        }

        /* 메뉴 정보 조회 */
        final List<MenuDto> userMenuList = menuService.getUserMenuList();
        mav.addObject("userMenuList", userMenuList);
        final List<MenuDto> mngrMenuList = menuService.getMngrMenuList();
        mav.addObject("mngrMenuList", mngrMenuList);
        final List<SiteAcsInfo> boardDefList = boardDefService.boardDefMenuList();
        mav.addObject("boardDefList", boardDefList);

        /* 내가 읽지 않은 공지사항 추가 */
        // 최종수정일이 조회기준일자 이내이고, 최종수정자(또는 작성자)가 내가 아니고, 내가 (수정 이후로) 조회하지 않은 글 갯수를 조회한다.
        final Integer noticeUnreadCnt = noticeService.getUnreadCnt(authInfo.getUserId(), DateUtils.getCurrDateAddDay(-7));
        mav.addObject("noticeUnreadCnt", noticeUnreadCnt);
    }
}
