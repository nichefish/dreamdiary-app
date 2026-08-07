package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalLifecycleViewHelper;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypePolicy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JournalEntryStateEnricher {

    private final LifecycleService lifecycleService;

    public JournalEntryStateEnricher(final LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    /**
     * 사용자 월별 상태 캐시를 현재 목록 DTO에 합친다.
     *
     * @param policy 엔트리 정책
     * @param username 사용자 아이디
     * @param listDto 대상 목록
     */
    public void enrich(
            final JournalEntryTypePolicy policy,
            final String username,
            final List<JournalEntryDto> listDto
    ) {
        final String cacheName = policy.stateCacheName();
        final String lifecycleCacheName = policy.lifecycleCacheName();
        final JournalSearchStateCacheHelper.MonthlyStateApplier<JournalEntryDto> stateMerger = getStateMerger(policy);
        if (cacheName == null || stateMerger == null) {
            enrichLifecycle(policy, listDto);
            return;
        }
        JournalSearchStateCacheHelper.mergeStatesFromUserCaches(username, listDto, cacheName, lifecycleCacheName, stateMerger);
        enrichLifecycle(policy, listDto);
    }

    /**
     * 캐시 누락 여부와 상관없이 DB 기준 lifecycle 값을 목록 DTO에 병합한다.
     *
     * @param policy 엔트리 정책
     * @param listDto 대상 목록
     */
    public void enrichLifecycle(
            final JournalEntryTypePolicy policy,
            final List<JournalEntryDto> listDto
    ) {
        if (policy == null || listDto == null || listDto.isEmpty()) return;

        final List<Integer> entryIds = listDto.stream()
                .filter(Objects::nonNull)
                .map(JournalEntryDto::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        JournalLifecycleViewHelper.applyEntryLifecycle(
                listDto,
                lifecycleService.getLifecycleMap(policy.contentType, entryIds)
        );

        final List<JournalEntryDto> reflectionList = listDto.stream()
                .filter(Objects::nonNull)
                .flatMap(entry -> entry.getReflectionList() == null
                        ? java.util.stream.Stream.empty()
                        : entry.getReflectionList().stream())
                .filter(Objects::nonNull)
                .toList();
        final List<Integer> reflectionIds = reflectionList.stream()
                .map(JournalEntryDto::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        JournalLifecycleViewHelper.applyEntryLifecycle(
                reflectionList,
                lifecycleService.getLifecycleMap(ContentType.JOURNAL_REFLECTION, reflectionIds)
        );
    }

    /**
     * 혼합 콘텐츠 타입 목록에 DB 기준 lifecycle 값을 병합한다.
     * <p>
     * 스레드 상세의 소속 엔트리처럼 일기·꿈·노트가 섞인 목록에 쓴다. 타입별로 묶어 lifecycle 을
     * 조회한 뒤 하나의 map 으로 합쳐 적용한다(엔트리 ID 는 타입과 무관하게 유일). state 는 병합하지
     * 않는다 — 스레드 표시에서는 서버 접힘 상태를 쓰지 않기 때문이다.
     *
     * @param entries 혼합 타입 엔트리 목록
     */
    public void enrichLifecycleMixed(final List<JournalEntryDto> entries) {
        if (entries == null || entries.isEmpty()) return;

        final Map<Integer, String> lifecycleMap = new HashMap<>();
        entries.stream()
                .filter(Objects::nonNull)
                .filter(entry -> entry.getContentType() != null && entry.getId() != null)
                .collect(Collectors.groupingBy(JournalEntryDto::getContentType))
                .forEach((contentTypeKey, group) -> {
                    final ContentType contentType = ContentType.get(contentTypeKey);
                    final List<Integer> ids = group.stream()
                            .map(JournalEntryDto::getId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
                    lifecycleMap.putAll(lifecycleService.getLifecycleMap(contentType, ids));
                });
        JournalLifecycleViewHelper.applyEntryLifecycle(entries, lifecycleMap);
    }

    /**
     * 정책별 상태 병합 함수를 선택한다.
     *
     * @param policy 엔트리 정책
     * @return 상태 병합 함수
     */
    private JournalSearchStateCacheHelper.MonthlyStateApplier<JournalEntryDto> getStateMerger(final JournalEntryTypePolicy policy) {
        return switch (policy) {
            case DIARY, REFLECTION -> JournalEntryStateViewHelper::applyStates;
            case DREAM -> JournalEntryStateViewHelper::applyDreamStates;
            default -> null;
        };
    }
}
