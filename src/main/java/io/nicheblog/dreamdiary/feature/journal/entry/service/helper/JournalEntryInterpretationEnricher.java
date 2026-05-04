package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.service.JournalInterpretationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JournalEntryInterpretationEnricher {

    private final JournalInterpretationQueryService journalInterpretationQueryService;

    /**
     * 엔트리 목록에 해석 콘텐츠 정보를 병합한다.
     *
     * @param contentType 콘텐츠 타입
     * @param username 사용자 아이디
     * @param listDto 대상 목록
     * @param <Dto> DTO 타입
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

        final Map<String, List<JournalInterpretationDto>> interpretationMap =
                journalInterpretationQueryService.getInterpretationMapByRefs(refKeyList, username);
        for (final JournalEntryDto dto : listDto) {
            if (dto == null || dto.getId() == null) continue;
            dto.setJournalInterpretationList(
                    interpretationMap.getOrDefault(String.format("%s:%d", contentType.key, dto.getId()), List.of())
            );
        }
    }
}
