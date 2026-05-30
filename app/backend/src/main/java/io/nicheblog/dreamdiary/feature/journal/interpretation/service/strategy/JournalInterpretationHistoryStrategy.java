package io.nicheblog.dreamdiary.feature.journal.interpretation.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.service.JournalInterpretationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JournalInterpretationHistoryStrategy
 * <pre>
 *  journal interpretation history 복구 전략.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JournalInterpretationHistoryStrategy implements HistoryStrategy<JournalInterpretationDto> {

    private final JournalInterpretationService journalInterpretationService;

    /**
     * 이 전략이 처리하는 콘텐츠 타입을 반환한다.
     *
     * @return 콘텐츠 타입
     */
    @Override
    public ContentType getContentType() {
        return ContentType.JOURNAL_INTERPRETATION;
    }

    /**
     * 사용자 소유 해석 상세를 조회한다.
     *
     * @param username 사용자 아이디
     * @param key 해석 ID
     * @return 해석 DTO
     * @throws Exception 조회 중 예외
     */
    @Override
    public JournalInterpretationDto getOwnedDto(final String username, final Integer key) throws Exception {
        return journalInterpretationService.getDetailDtoWithCacheByUser(username, key);
    }

    /**
     * 이력 복원 본문을 해석 엔트리에 반영한다.
     *
     * @param key 해석 ID
     * @param content 변경 본문
     * @param historyType 이력 타입
     * @param fromHistoryId 원본 이력 ID
     * @return 해석 DTO
     * @throws Exception 복원 중 예외
     */
    @Override
    public JournalInterpretationDto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        return journalInterpretationService.updtContent(key, content, historyType, fromHistoryId);
    }

    /**
     * 이력 목록을 현재 DTO에 반영한다.
     *
     * @param currentDto 현재 DTO
     * @param historyList 이력 목록
     * @return 이력이 반영된 DTO
     */
    @Override
    public JournalInterpretationDto applyHistoryList(final JournalInterpretationDto currentDto, final List<HistoryDto> historyList) {
        currentDto.setHistoryList(historyList);
        return currentDto;
    }
}
