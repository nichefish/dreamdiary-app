package io.nicheblog.dreamdiary.feature.admin.menu.adapter;

import io.nicheblog.dreamdiary.feature.admin.menu.exception.MenuNotExistsException;
import io.nicheblog.dreamdiary.feature.admin.menu.model.MenuDto;
import io.nicheblog.dreamdiary.feature.admin.menu.model.SiteAcsInfo;
import io.nicheblog.dreamdiary.feature.admin.menu.service.MenuService;
import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import io.nicheblog.dreamdiary.infrastructure.freemarker.interceptor.FreemarkerInterceptor;
import io.nicheblog.dreamdiary.infrastructure.freemarker.model.FreemarkerModelContext;
import io.nicheblog.dreamdiary.infrastructure.freemarker.port.FreemarkerModelContributor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * FreemarkerMenuModelContributor
 * <pre>
 *  메뉴 관련 Freemarker 모델 기여자(Contributor) 구현체.
 * </pre>
 *
 * @author nichefish
 * @see FreemarkerInterceptor
 */
@Component
@Order(10)
@RequiredArgsConstructor
@Log4j2
public class MenuFreemarkerModelContributor
        implements FreemarkerModelContributor {

    private final MenuService menuService;

    /**
     * Freemarker 모델에 데이터를 추가한다.
     *
     * @param context 요청 단위 Freemarker 모델 컨텍스트
     * @throws Exception 처리 중 예외 발생 시
     */
    @Override
    public void contribute(final FreemarkerModelContext context) throws Exception {
        final Object menuLabel = context.getModelAttr("menuLabel");
        final Object pageNm = context.getModelAttr("pageNm");
        if (menuLabel instanceof SiteMenu menu) {
            try {
                final MenuDto menuDto = menuService.getMenuByLabel(menu);
                final SiteAcsInfo acsInfo = menuService.getSiteAceInfoFromMenu(menuDto);
                if (pageNm instanceof PageNm page) {
                    acsInfo.setAcsPageInfo(page);
                }
                context.addObject("siteAcsInfo", acsInfo);

                final String userMode = menuService.getIsMngrMenu(menuDto.getMenuNo()) ? Code.AUTH_MNGR : Code.AUTH_USER;
                context.getSession().setAttribute("userMode", userMode);
                context.addObject("isMngrMode", Code.AUTH_MNGR.equals(userMode));
            } catch (final MenuNotExistsException e) {
                log.error(MessageUtils.getExceptionMsg(e));
            }
        }

        context.addObject("userMenuList", menuService.getUserMenuList());
        context.addObject("mngrMenuList", menuService.getMngrMenuList());
    }
}
