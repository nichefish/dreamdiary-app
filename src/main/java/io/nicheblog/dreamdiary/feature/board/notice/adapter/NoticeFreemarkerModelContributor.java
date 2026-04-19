package io.nicheblog.dreamdiary.feature.board.notice.adapter;

import io.nicheblog.dreamdiary.feature.board.notice.service.NoticeService;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.freemarker.interceptor.FreemarkerInterceptor;
import io.nicheblog.dreamdiary.infrastructure.freemarker.model.FreemarkerModelContext;
import io.nicheblog.dreamdiary.infrastructure.freemarker.port.FreemarkerModelContributor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * FreemarkerNoticeModelContributor
 * <pre>
 *  공지 관련 Freemarker 모델 기여자 구현체.
 * </pre>
 *
 * @author nichefish
 * @see FreemarkerInterceptor
 */
@Component
@Order(30)
@RequiredArgsConstructor
public class NoticeFreemarkerModelContributor
        implements FreemarkerModelContributor {

    private final NoticeService noticeService;

    /**
     * Freemarker 모델에 데이터를 추가한다.
     *
     * @param context 요청 단위 Freemarker 모델 컨텍스트
     * @throws Exception 처리 중 예외 발생 시
     */
    @Override
    public void contribute(final FreemarkerModelContext context) throws Exception {
        final Integer noticeUnreadCnt = noticeService.getUnreadCnt(context.getUsername(), DateUtils.getCurrDateAddDay(-7));
        context.addObject("noticeUnreadCnt", noticeUnreadCnt);
    }
}
