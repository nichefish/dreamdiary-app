package io.nicheblog.dreamdiary.feature.board.def.adapter;

import io.nicheblog.dreamdiary.feature.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.feature.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.model.SiteAcsInfo;
import io.nicheblog.dreamdiary.feature.board.def.service.BoardDefService;
import io.nicheblog.dreamdiary.infrastructure.freemarker.interceptor.FreemarkerInterceptor;
import io.nicheblog.dreamdiary.infrastructure.freemarker.model.FreemarkerModelContext;
import io.nicheblog.dreamdiary.infrastructure.freemarker.port.FreemarkerModelContributor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * BoardDefFreemarkerModelContributor
 * <pre>
 *  게시판 정의 관련 Freemarker 모델 기여자 구현체.
 * </pre>
 *
 * @author nichefish
 * @see FreemarkerInterceptor
 */
@Component
@Order(20)
@RequiredArgsConstructor
public class BoardDefFreemarkerModelContributor
        implements FreemarkerModelContributor {

    private final BoardDefService boardDefService;

    /**
     * Freemarker 모델에 데이터를 추가한다.
     *
     * @param context 요청 단위 Freemarker 모델 컨텍스트
     * @throws Exception 처리 중 예외 발생 시
     */
    @Override
    public void contribute(final FreemarkerModelContext context) throws Exception {
        final Object menuLabel = context.getModelAttr("menuLabel");
        if (menuLabel == SiteMenu.BOARD) {
            final String boardDef = (String) context.getModelAttr("boardDef");
            final SiteAcsInfo acsInfo = boardDefService.getMenuByBoardDef(boardDef);
            final Object pageNm = context.getModelAttr("pageNm");
            if (pageNm instanceof PageNm page) {
                acsInfo.setAcsPageInfo(page);
            }
            context.addObject("siteAcsInfo", acsInfo);
        }

        context.addObject("boardDefList", boardDefService.boardDefMenuList());
    }
}
