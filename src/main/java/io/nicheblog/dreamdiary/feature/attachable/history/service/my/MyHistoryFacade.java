package io.nicheblog.dreamdiary.feature.attachable.history.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.service.HistoryFacade;
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

    public <Dto extends BaseAttachableDto> Dto getMyHistoryTarget(
            final ContentType contentType,
            final Integer key
    ) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return historyFacade.getHistoryTargetByUser(contentType, username, key);
    }

    public List<HistoryDto> getMyHistoryList(final ContentType contentType, final Integer key) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return historyFacade.getHistoryListByUser(contentType, username, key);
    }

    public <Dto extends BaseAttachableDto> Dto restoreMyHistory(
            final ContentType contentType,
            final Integer key,
            final Integer historyId
    ) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return historyFacade.restoreHistoryByUser(contentType, username, key, historyId);
    }

    public boolean deleteMyHistory(
            final ContentType contentType,
            final Integer key,
            final Integer historyId
    ) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return historyFacade.deleteHistoryByUser(contentType, username, key, historyId);
    }

    public boolean deleteAllMyHistory(
            final ContentType contentType,
            final Integer key
    ) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return historyFacade.deleteAllHistoryByUser(contentType, username, key);
    }
}
