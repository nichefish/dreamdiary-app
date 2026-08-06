package io.nicheblog.dreamdiary.feature.journal.reflection.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.reflection.service.JournalReflectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * JournalReflectionHistoryStrategy
 * <pre>
 *  JOURNAL_REFLECTION contentType 용 이력 조회/복원 전략.
 *  Reflection 은 별도 테이블(journal_reflection)에 영속되며, JournalReflectionService 를 통해 조회·수정한다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JournalReflectionHistoryStrategy implements HistoryStrategy<JournalEntryDto> {

    private final JournalReflectionService journalReflectionService;

    /**
     * 이 전략이 처리하는 콘텐츠 타입.
     *
     * @return JOURNAL_REFLECTION 단일 집합
     */
    @Override
    public Set<ContentType> getContentTypes() {
        return Set.of(ContentType.JOURNAL_REFLECTION);
    }

    /**
     * 사용자 소유 Reflection 상세를 조회한다.
     *
     * @param username 사용자 아이디
     * @param key Reflection ID
     * @return Reflection DTO
     * @throws Exception 조회 중 예외
     */
    @Override
    public JournalEntryDto getOwnedDto(final String username, final Integer key) throws Exception {
        return journalReflectionService.getDtlDtoByUser(key);
    }

    /**
     * 이력 복원 본문을 반영한다.
     *
     * @param key Reflection ID
     * @param content 복원할 본문
     * @param historyType 이력 타입
     * @param fromHistoryId 원본 이력 ID
     * @return 수정된 Reflection DTO
     * @throws Exception 수정 중 예외
     */
    @Override
    public JournalEntryDto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        return journalReflectionService.updtContent(key, content, historyType, fromHistoryId);
    }

    /**
     * 이력 목록을 현재 DTO에 결합한다.
     *
     * @param currentDto 현재 DTO
     * @param historyList 이력 목록
     * @return 이력이 반영된 DTO
     */
    @Override
    public JournalEntryDto applyHistoryList(final JournalEntryDto currentDto, final List<HistoryDto> historyList) {
        currentDto.setHistoryList(historyList);
        return currentDto;
    }
}
