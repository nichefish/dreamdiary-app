package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.related.service.RelatedContentQueryService;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.service.JournalThreadEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JournalEntryRelatedEnricher {

    private final RelatedContentQueryService relatedContentQueryService;
    private final JournalThreadEntryService journalThreadEntryService;

    /**
     * 엔트리 목록에 연관 콘텐츠 정보를 병합한다.
     *
     * @param contentType 콘텐츠 타입
     * @param username 사용자 아이디
     * @param listDto 대상 목록
     * @throws Exception 병합 중 예외
     */
    public void enrich(
            final ContentType contentType,
            final String username,
            final List<JournalEntryDto> listDto
    ) throws Exception {
        if (listDto == null || listDto.isEmpty()) return;

        final List<BaseAttachableKey> refKeyList = new ArrayList<>();
        listDto.stream()
                .filter(dto -> dto != null && dto.getId() != null)
                .forEach(dto -> refKeyList.add(new BaseAttachableKey(dto.getId(), contentType)));

        final Map<String, List<RelatedContentDto>> relatedMap =
                relatedContentQueryService.getRelatedContentMapByRefs(refKeyList, username);
        // 흐름(스레드) 소속. 엔트리마다 단건 조회하면 N+1 이라 목록 단위로 한 번에 받는다.
        final Map<Integer, List<JournalThreadEntryDto>> threadMap =
                journalThreadEntryService.getMapByEntryIds(
                        listDto.stream()
                                .filter(dto -> dto != null && dto.getId() != null)
                                .map(JournalEntryDto::getId)
                                .collect(Collectors.toList()),
                        username
                );
        for (final JournalEntryDto dto : listDto) {
            if (dto == null || dto.getId() == null) continue;
            dto.setRelatedContentList(
                    relatedMap.getOrDefault(String.format("%s:%d", contentType.key, dto.getId()), List.of())
            );
            dto.setThreadList(threadMap.getOrDefault(dto.getId(), List.of()));
        }
    }
}
