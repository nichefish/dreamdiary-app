package io.nicheblog.dreamdiary.feature.clsf.history.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.clsf.history.service.HistoryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyHistoryFacade
 * <pre>
 *  로그인 사용자 기준의 공통 history facade.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class MyHistoryFacade {

    private final HistoryFacade historyFacade;

    public <Dto extends BaseClsfDto> Dto getMyHistoryTarget(
            final ContentType contentType,
            final Integer key
    ) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return historyFacade.getHistoryTargetByUser(contentType, userId, key);
    }

    public List<HistoryDto> getMyHistoryList(final ContentType contentType, final Integer key) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return historyFacade.getHistoryListByUser(contentType, userId, key);
    }

    public <Dto extends BaseClsfDto> Dto restoreMyHistory(
            final ContentType contentType,
            final Integer key,
            final Integer historyNo
    ) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return historyFacade.restoreHistoryByUser(contentType, userId, key, historyNo);
    }

    public boolean deleteMyHistory(
            final ContentType contentType,
            final Integer key,
            final Integer historyNo
    ) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return historyFacade.deleteHistoryByUser(contentType, userId, key, historyNo);
    }

    public boolean deleteAllMyHistory(
            final ContentType contentType,
            final Integer key
    ) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return historyFacade.deleteAllHistoryByUser(contentType, userId, key);
    }
}
