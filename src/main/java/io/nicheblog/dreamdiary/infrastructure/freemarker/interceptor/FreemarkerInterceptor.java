package io.nicheblog.dreamdiary.infrastructure.freemarker.interceptor;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.ActiveProfile;
import io.nicheblog.dreamdiary.global.ReleaseInfo;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.Constant;
import io.nicheblog.dreamdiary.infrastructure.freemarker.model.FreemarkerModelContext;
import io.nicheblog.dreamdiary.infrastructure.freemarker.port.FreemarkerModelContributor;
import io.nicheblog.dreamdiary.infrastructure.web.config.WebMvcContextConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

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
    private final ReleaseInfo releaseInfo;
    private final List<FreemarkerModelContributor> modelContributors;

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

        if (!(handler instanceof HandlerMethod)) return;
        if (mav == null) return;

        final String viewName = mav.getViewName();
        if (viewName == null) return;

        mav.addObject("profile", activeProfile.getActive());
        mav.addObject("releaseDate", releaseInfo.getReleaseDateStr());
        mav.addObject("urlMap", Url.getUrlMap());
        mav.addObject("messageMap", MessageUtils.getMessageMap());
        mav.addObject("constantMap", Constant.getConstantMap());

        final Boolean isMobile = DeviceUtils.getCurrentDevice(request).isMobile();
        request.setAttribute(Constant.IS_MBL, isMobile);

        if (!AuthUtils.isAuthenticated()) return;

        final AuthInfo authInfo = (AuthInfo) session.getAttribute("authInfo");
        mav.addObject("isMngr", authInfo.getIsMngr());
        mav.addObject("isDev", authInfo.getIsDev());

        final FreemarkerModelContext context = FreemarkerModelContext.builder()
                .request(request)
                .session(session)
                .mav(mav)
                .userId(authInfo.getUserId())
                .build();

        for (final FreemarkerModelContributor contributor : modelContributors) {
            contributor.contribute(context);
        }
    }
}
