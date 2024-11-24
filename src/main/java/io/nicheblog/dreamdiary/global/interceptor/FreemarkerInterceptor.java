package io.nicheblog.dreamdiary.global.interceptor;

import io.nicheblog.dreamdiary.global.ActiveProfile;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.global.model.SiteAcsInfo;
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

/**
 * FreemarkerInterceptor
 * <pre>
 *  controller -> view로 가는 중간에 작용하는 인터셉터.
 *  (프리마커 관련 외에도 분류가 애매한 기타 로직 뭉뚱그려 수행)
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class FreemarkerInterceptor
        implements HandlerInterceptor {

    private final HttpSession session;

    private final ActiveProfile activeProfile;

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
    ) {

        /* model 정보 없을시 처리하지 않음 */
        if (mav == null) return;

        // 모든 페이지에 activeProfile 추가
        mav.addObject("activeProfile", activeProfile.getActive());
        // static 자원들에 releaseDate 세팅
        mav.addObject("releaseDate", releaseDate);

        final String requestURI = request.getRequestURI();
        if (Url.AUTH_LGN_FORM.equals(requestURI) || Url.AUTH_LGN_PROC.equals(requestURI)) return;

        /* 모바일 여부 체크 추가 (TODO: 현재 미사용중) */
        final Boolean isMobile = DeviceUtils.getCurrentDevice(request).isMobile();
        request.setAttribute(Constant.IS_MBL, isMobile);

        /* 사용자 권한 정보 모델에 추가 */
        if (AuthUtils.isAuthenticated()) {
            final AuthInfo authInfo = (AuthInfo) session.getAttribute("authInfo");
            final Boolean isMngr = authInfo.getIsMngr();
            mav.addObject("isMngr", isMngr);
            mav.addObject("isDev", authInfo.getIsDev());

            /* 메뉴접근 정보 읽어서 관리자/사용자 모드 세션에 세팅 */
            final SiteAcsInfo acsInfo = (SiteAcsInfo) mav.getModel().get(Constant.SITE_MENU);
            final String userMode = acsInfo != null && acsInfo.getIsMngrMenu() ? Constant.AUTH_MNGR : Constant.AUTH_USER;
            session.setAttribute("userMode", userMode);
            mav.addObject("isMngrMode", Constant.AUTH_MNGR.equals(userMode));
        }
    }
}
