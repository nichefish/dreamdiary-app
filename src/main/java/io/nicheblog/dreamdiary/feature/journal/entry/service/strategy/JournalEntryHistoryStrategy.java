package io.nicheblog.dreamdiary.feature.journal.entry.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.attachable.history.service.strategy.HistoryStrategy;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JournalEntryHistoryStrategy implements HistoryStrategy<JournalEntryDto> {

    private final JournalEntryService journalEntryService;

    /**
     * 이 전략이 처리하는 콘텐츠 타입 집합을 반환한다.
     *
     * @return 처리 가능한 콘텐츠 타입 집합
     */
    @Override
    public Set<ContentType> getContentTypes() {
        return EnumSet.of(ContentType.JOURNAL_DIARY, ContentType.JOURNAL_DREAM);
    }

    /**
     * 콘텐츠 타입이 없는 호출은 지원하지 않는다.
     *
     * @param username 사용자 아이디
     * @param key 엔트리 ID
     * @return 엔트리 DTO
     * @throws Exception 조회 중 예외
     */
    @Override
    public JournalEntryDto getOwnedDto(final String username, final Integer key) throws Exception {
        throw new UnsupportedOperationException("Journal entry history strategy requires contentType.");
    }

    /**
     * 사용자 소유 엔트리 상세를 콘텐츠 타입 기준으로 조회한다.
     *
     * @param username 사용자 아이디
     * @param key 엔트리 ID
     * @param contentType 콘텐츠 타입
     * @return 엔트리 DTO
     * @throws Exception 조회 중 예외
     */
    @Override
    public JournalEntryDto getOwnedDto(final String username, final Integer key, final ContentType contentType) throws Exception {
        return journalEntryService.getDtlDtoWithCacheByUser(username, key, contentType);
    }

    /**
     * 콘텐츠 타입이 없는 본문 복구 호출은 지원하지 않는다.
     *
     * @param key 엔트리 ID
     * @param content 변경 본문
     * @param historyType 이력 타입
     * @param fromHistoryId 원본 이력 ID
     * @return 엔트리 DTO
     * @throws Exception 복구 중 예외
     */
    @Override
    public JournalEntryDto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        throw new UnsupportedOperationException("Journal entry history strategy requires contentType.");
    }

    /**
     * 이력 복원 본문을 콘텐츠 타입 기준으로 반영한다.
     *
     * @param key 엔트리 ID
     * @param content 변경 본문
     * @param historyType 이력 타입
     * @param fromHistoryId 원본 이력 ID
     * @param contentType 콘텐츠 타입
     * @return 엔트리 DTO
     * @throws Exception 복구 중 예외
     */
    @Override
    public JournalEntryDto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId,
            final ContentType contentType
    ) throws Exception {
        return journalEntryService.updtContent(key, content, historyType, fromHistoryId, contentType);
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
