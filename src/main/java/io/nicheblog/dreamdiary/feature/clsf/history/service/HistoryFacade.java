package io.nicheblog.dreamdiary.feature.clsf.history.service;

import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.clsf.history.service.strategy.HistoryStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ClsfHistoryFacade
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
    private final List<HistoryStrategy<? extends BaseClsfDto>> strategies;

    private final Map<ContentType, HistoryStrategy<? extends BaseClsfDto>> strategyMap = new EnumMap<>(ContentType.class);

    @PostConstruct
    private void initStrategyMap() {
        for (final HistoryStrategy<? extends BaseClsfDto> strategy : strategies) {
            final ContentType contentType = strategy.getContentType();
            if (contentType == null || ContentType.DEFAULT.equals(contentType)) {
                throw new IllegalStateException("Invalid HistoryStrategy contentType: " + strategy.getClass().getName());
            }
            if (strategyMap.containsKey(contentType)) {
                throw new IllegalStateException("Duplicate HistoryStrategy mapping for ContentType: " + contentType);
            }
            strategyMap.put(contentType, strategy);
        }
    }

    public <Dto extends BaseClsfDto> List<HistoryDto> getHistoryListByUser(
            final ContentType contentType,
            final String userId,
            final Integer key
    ) throws Exception {
        final HistoryStrategy<Dto> strategy = this.getRequiredStrategy(contentType);
        final Dto currentDto = strategy.getOwnedDto(userId, key);
        return historyService.getHistoryList(currentDto.getClsfKey());
    }

    public <Dto extends BaseClsfDto> Dto getHistoryTargetByUser(
            final ContentType contentType,
            final String userId,
            final Integer key
    ) throws Exception {
        final HistoryStrategy<Dto> strategy = this.getRequiredStrategy(contentType);
        final Dto currentDto = strategy.getOwnedDto(userId, key);
        final List<HistoryDto> historyList = historyService.getHistoryList(currentDto.getClsfKey());
        return strategy.applyHistoryList(currentDto, historyList);
    }

    public <Dto extends BaseClsfDto> Dto restoreHistoryByUser(
            final ContentType contentType,
            final String userId,
            final Integer key,
            final Integer historyNo
    ) throws Exception {
        final HistoryStrategy<Dto> strategy = this.getRequiredStrategy(contentType);
        final Dto currentDto = strategy.getOwnedDto(userId, key);
        final Optional<HistoryDto> history = historyService.getHistory(currentDto.getClsfKey(), historyNo);
        if (history.isEmpty()) {
            throw new IllegalArgumentException("복구할 이력이 없습니다.");
        }

        return strategy.updtCn(key, history.get().getCn(), HistoryType.RESTORE, historyNo);
    }

    public <Dto extends BaseClsfDto> boolean deleteHistoryByUser(
            final ContentType contentType,
            final String userId,
            final Integer key,
            final Integer historyNo
    ) throws Exception {
        final HistoryStrategy<Dto> strategy = this.getRequiredStrategy(contentType);
        final Dto currentDto = strategy.getOwnedDto(userId, key);
        return historyService.deleteHistory(currentDto.getClsfKey(), historyNo);
    }

    public <Dto extends BaseClsfDto> boolean deleteAllHistoryByUser(
            final ContentType contentType,
            final String userId,
            final Integer key
    ) throws Exception {
        final HistoryStrategy<Dto> strategy = this.getRequiredStrategy(contentType);
        final Dto currentDto = strategy.getOwnedDto(userId, key);
        return historyService.deleteAllHistory(currentDto.getClsfKey());
    }

    @SuppressWarnings("unchecked")
    private <Dto extends BaseClsfDto> HistoryStrategy<Dto> getRequiredStrategy(final ContentType contentType) {
        final HistoryStrategy<? extends BaseClsfDto> strategy = strategyMap.get(contentType);
        if (strategy == null) {
            throw new IllegalStateException("Missing HistoryStrategy for ContentType: " + contentType);
        }
        return (HistoryStrategy<Dto>) strategy;
    }
}
