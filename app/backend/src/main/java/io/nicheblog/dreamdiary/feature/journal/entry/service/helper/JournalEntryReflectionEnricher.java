package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.mapstruct.JournalReflectionMapstruct;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 엔트리 목록에 target 역참조 Reflection 을 병합한다.
 * <p>
 * Reflection 은 별도 Aggregate(journal_reflection)이며, {@code refId = 대상 엔트리 ∧ refContentType = 대상 타입}
 * 으로 대상 엔트리의 {@code reflectionList} 에 싣는다. 자식 해석 합성이 아니라 target 역참조 연관이다.
 * 표시 정렬은 {@code created_at} 오름차순이다(chapter sortOrder 아님).
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JournalEntryReflectionEnricher {

    private final JournalReflectionRepository journalReflectionRepository;
    private final JournalReflectionMapstruct mapstruct;

    /**
     * 엔트리 목록에 target 역참조 Reflection 을 병합한다.
     *
     * @param contentType 대상 엔트리 콘텐츠 타입
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

        final List<Integer> targetIds = listDto.stream()
                .filter(dto -> dto != null && dto.getId() != null)
                .map(JournalEntryDto::getId)
                .distinct()
                .toList();
        final Map<String, List<JournalEntryDto>> reflectionMap = this.getReflectionMapByTargetIds(targetIds);

        for (final JournalEntryDto dto : listDto) {
            if (dto == null || dto.getId() == null) continue;
            dto.setReflectionList(
                    reflectionMap.getOrDefault(buildKey(contentType.key, dto.getId()), List.of())
            );
        }
    }

    /**
     * 혼합 콘텐츠 타입 목록에 target 역참조 Reflection 을 병합한다.
     * 스레드 상세처럼 일기·꿈이 섞인 목록에 쓰며, 각 엔트리의 contentType:id 키로 일자 화면과 동일하게 싣는다.
     *
     * @param listDto 혼합 타입 엔트리 목록
     * @throws Exception 병합 중 예외
     */
    public void enrichMixed(final List<JournalEntryDto> listDto) throws Exception {
        if (listDto == null || listDto.isEmpty()) return;

        final List<Integer> targetIds = listDto.stream()
                .filter(dto -> dto != null && dto.getId() != null)
                .map(JournalEntryDto::getId)
                .distinct()
                .toList();
        final Map<String, List<JournalEntryDto>> reflectionMap = this.getReflectionMapByTargetIds(targetIds);

        for (final JournalEntryDto dto : listDto) {
            if (dto == null || dto.getId() == null || dto.getContentType() == null) continue;
            dto.setReflectionList(
                    reflectionMap.getOrDefault(buildKey(dto.getContentType(), dto.getId()), List.of())
            );
        }
    }

    /**
     * target 엔트리 ID 집합에 대해 역참조 Reflection 을 {@code "targetContentType:targetId"} 키로 묶어 반환한다.
     *
     * @param targetIds target 엔트리 ID 목록
     * @return {@code "ct:id" -> Reflection DTO 목록}
     * @throws Exception 매핑 중 예외
     */
    public Map<String, List<JournalEntryDto>> getReflectionMapByTargetIds(final Collection<Integer> targetIds) throws Exception {
        if (targetIds == null || targetIds.isEmpty()) return Map.of();

        final List<JournalReflectionEntity> reflections =
                journalReflectionRepository.findAllByRefIdInOrderByCreatedAtAsc(targetIds);
        final Map<String, List<JournalEntryDto>> map = new HashMap<>();
        for (final JournalReflectionEntity reflection : reflections) {
            if (reflection == null || reflection.getRefId() == null || reflection.getRefContentType() == null) continue;
            final String key = buildKey(reflection.getRefContentType().key, reflection.getRefId());
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(mapstruct.toDto(reflection));
        }
        return map;
    }

    private static String buildKey(final String contentTypeKey, final Integer id) {
        return contentTypeKey + ":" + id;
    }
}