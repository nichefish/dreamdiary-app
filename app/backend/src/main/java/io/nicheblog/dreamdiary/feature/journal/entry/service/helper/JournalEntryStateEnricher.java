package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalLifecycleViewHelper;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypePolicy;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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

        final List<JournalInterpretationDto> interpretationList = listDto.stream()
                .filter(Objects::nonNull)
                .flatMap(entry -> entry.getJournalInterpretationList() == null
                        ? java.util.stream.Stream.empty()
                        : entry.getJournalInterpretationList().stream())
                .filter(Objects::nonNull)
                .toList();
        final List<Integer> interpretationIds = interpretationList.stream()
                .map(JournalInterpretationDto::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        JournalLifecycleViewHelper.applyInterpretationLifecycle(
                interpretationList,
                lifecycleService.getLifecycleMap(
                        io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType.JOURNAL_INTERPRETATION,
                        interpretationIds
                )
        );
    }

    /**
     * 정책별 상태 병합 함수를 선택한다.
     *
     * @param policy 엔트리 정책
     * @return 상태 병합 함수
     */
    private JournalSearchStateCacheHelper.MonthlyStateApplier<JournalEntryDto> getStateMerger(final JournalEntryTypePolicy policy) {
        return switch (policy) {
            case DIARY -> JournalEntryStateViewHelper::applyStates;
            case DREAM -> JournalEntryStateViewHelper::applyDreamStates;
            default -> null;
        };
    }
}
