package io.nicheblog.dreamdiary.feature.attachable.history.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.service.strategy.HistoryStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * AttachableHistoryFacade
 * <pre>
 *  contentType 별 history 전략을 선택해 공통 history 흐름을 처리한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class HistoryFacade {

    private final HistoryService historyService;
    private final List<HistoryStrategy<? extends BaseAttachableDto>> strategies;

    private final Map<ContentType, HistoryStrategy<? extends BaseAttachableDto>> strategyMap = new EnumMap<>(ContentType.class);

    @PostConstruct
    private void initStrategyMap() {
        for (final HistoryStrategy<? extends BaseAttachableDto> strategy : strategies) {
            final Set<ContentType> contentTypes = strategy.getContentTypes();
            if (contentTypes == null || contentTypes.isEmpty()) {
                throw new IllegalStateException("Invalid HistoryStrategy contentTypes: " + strategy.getClass().getName());
            }
            for (final ContentType contentType : contentTypes) {
                if (contentType == null || ContentType.DEFAULT.equals(contentType)) {
                    throw new IllegalStateException("Invalid HistoryStrategy contentType: " + strategy.getClass().getName());
                }
                if (strategyMap.containsKey(contentType)) {
                    throw new IllegalStateException("Duplicate HistoryStrategy mapping for ContentType: " + contentType);
                }
                strategyMap.put(contentType, strategy);
            }
        }
    }

    public <Dto extends BaseAttachableDto> List<HistoryDto> getHistoryListByUser(
            final ContentType contentType,
            final String username,
            final Integer key
    ) throws Exception {
        final HistoryStrategy<Dto> strategy = this.getRequiredStrategy(contentType);
        final Dto currentDto = strategy.getOwnedDto(username, key, contentType);
        return historyService.getHistoryList(currentDto.getAttachableKey());
    }

    public <Dto extends BaseAttachableDto> Dto getHistoryTargetByUser(
            final ContentType contentType,
            final String username,
            final Integer key
    ) throws Exception {
        final HistoryStrategy<Dto> strategy = this.getRequiredStrategy(contentType);
        final Dto currentDto = strategy.getOwnedDto(username, key, contentType);
        final List<HistoryDto> historyList = historyService.getHistoryList(currentDto.getAttachableKey());
        return strategy.applyHistoryList(currentDto, historyList);
    }

    public <Dto extends BaseAttachableDto> Dto restoreHistoryByUser(
            final ContentType contentType,
            final String username,
            final Integer key,
            final Integer historyId
    ) throws Exception {
        final HistoryStrategy<Dto> strategy = this.getRequiredStrategy(contentType);
        final Dto currentDto = strategy.getOwnedDto(username, key, contentType);
        final Optional<HistoryDto> history = historyService.getHistory(currentDto.getAttachableKey(), historyId);
        if (history.isEmpty()) {
            throw new IllegalArgumentException("복구할 이력이 없습니다.");
        }

        return strategy.updtContent(key, history.get().getContent(), HistoryType.RESTORE, historyId, contentType);
    }

    public <Dto extends BaseAttachableDto> boolean deleteHistoryByUser(
            final ContentType contentType,
            final String username,
            final Integer key,
            final Integer historyId
    ) throws Exception {
        final HistoryStrategy<Dto> strategy = this.getRequiredStrategy(contentType);
        final Dto currentDto = strategy.getOwnedDto(username, key, contentType);
        return historyService.deleteHistory(currentDto.getAttachableKey(), historyId);
    }

    public <Dto extends BaseAttachableDto> boolean deleteAllHistoryByUser(
            final ContentType contentType,
            final String username,
            final Integer key
    ) throws Exception {
        final HistoryStrategy<Dto> strategy = this.getRequiredStrategy(contentType);
        final Dto currentDto = strategy.getOwnedDto(username, key, contentType);
        return historyService.deleteAllHistory(currentDto.getAttachableKey());
    }

    @SuppressWarnings("unchecked")
    private <Dto extends BaseAttachableDto> HistoryStrategy<Dto> getRequiredStrategy(final ContentType contentType) {
        final HistoryStrategy<? extends BaseAttachableDto> strategy = strategyMap.get(contentType);
        if (strategy == null) {
            throw new IllegalStateException("Missing HistoryStrategy for ContentType: " + contentType);
        }
        return (HistoryStrategy<Dto>) strategy;
    }
}
