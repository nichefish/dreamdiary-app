package io.nicheblog.dreamdiary.feature.journal.thread.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import io.nicheblog.dreamdiary.feature.journal.thread.service.JournalThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * JOURNAL_THREAD 콘텐츠의 본문 이력 조회·복원 전략.
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JournalThreadHistoryStrategy implements HistoryStrategy<JournalThreadDto> {

    private final JournalThreadService journalThreadService;

    /**
     * 이 전략이 처리하는 콘텐츠 타입을 반환한다.
     *
     * @return JOURNAL_THREAD 단일 집합
     */
    @Override
    public Set<ContentType> getContentTypes() {
        return Set.of(ContentType.JOURNAL_THREAD);
    }

    /**
     * 사용자 소유 스레드를 조회한다.
     *
     * @param username 사용자 계정명
     * @param key 스레드 ID
     * @return 사용자 소유 스레드 DTO
     * @throws Exception 조회 중 예외
     */
    @Override
    public JournalThreadDto getOwnedDto(final String username, final Integer key) throws Exception {
        return journalThreadService.getDtlDtoByUser(username, key);
    }

    /**
     * 선택한 이력 본문을 스레드에 복원한다.
     *
     * @param key 스레드 ID
     * @param content 복원할 본문
     * @param historyType 이력 유형
     * @param fromHistoryId 복원 원본 이력 ID
     * @return 복원된 스레드 DTO
     * @throws Exception 복원 중 예외
     */
    @Override
    public JournalThreadDto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        return journalThreadService.updtContent(key, content, historyType, fromHistoryId);
    }

    /**
     * 이력 목록을 현재 스레드 DTO에 결합한다.
     *
     * @param currentDto 현재 스레드 DTO
     * @param historyList 본문 이력 목록
     * @return 이력 목록이 반영된 스레드 DTO
     */
    @Override
    public JournalThreadDto applyHistoryList(
            final JournalThreadDto currentDto,
            final List<HistoryDto> historyList
    ) {
        currentDto.setHistoryList(historyList);
        return currentDto;
    }
}
