package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypePolicy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JournalEntryStateEnricher {

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
        final JournalSearchStateCacheHelper.MonthlyStateApplier<JournalEntryDto> stateMerger = getStateMerger(policy);
        if (cacheName == null || stateMerger == null) return;
        JournalSearchStateCacheHelper.mergeStatesFromUserCaches(username, listDto, cacheName, stateMerger);
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
