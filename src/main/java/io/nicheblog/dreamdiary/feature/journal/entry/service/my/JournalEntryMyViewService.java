package io.nicheblog.dreamdiary.feature.journal.entry.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntrySearchParam;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryInterpretationEnricher;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryRelatedEnricher;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryStateEnricher;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalEntryMyViewService {

    private final JournalEntryService journalEntryService;
    private final JournalEntryInterpretationEnricher interpretationEnricher;
    private final JournalEntryRelatedEnricher relatedEnricher;
    private final JournalEntryStateEnricher stateEnricher;

    /**
     * 사용자 목록 조회 후 부가 정보(해석/연관/상태)를 병합한다.
     *
     * @param searchParam 검색 조건
     * @param contentType 콘텐츠 타입
     * @return 부가 정보가 병합된 목록
     * @throws Exception 조회 중 예외
     */
    public List<JournalEntryDto> getMyList(final JournalEntrySearchParam searchParam, final ContentType contentType) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final List<JournalEntryDto> listDto = journalEntryService.getListDtoByUser(username, searchParam, contentType);
        enrichEntries(username, contentType, listDto, true);
        return listDto;
    }

    /**
     * 사용자 연간 목록 조회 후 부가 정보(해석/연관/상태)를 병합한다.
     *
     * @param searchParam 검색 조건
     * @param contentType 콘텐츠 타입
     * @return 부가 정보가 병합된 연간 목록
     * @throws Exception 조회 중 예외
     */
    public List<JournalEntryDto> getMyAnnualList(final JournalEntrySearchParam searchParam, final ContentType contentType) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final List<JournalEntryDto> listDto = journalEntryService.getAnnualListDtoByUser(username, searchParam, contentType);
        enrichEntries(username, contentType, listDto, true);
        return listDto;
    }

    /**
     * 사용자 상세 조회 후 부가 정보(해석/연관)를 병합한다.
     *
     * @param key 엔트리 ID
     * @param contentType 콘텐츠 타입
     * @return 부가 정보가 병합된 상세
     * @throws Exception 조회 중 예외
     */
    public JournalEntryDto getMyDetail(final Integer key, final ContentType contentType) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final JournalEntryDto retrieved = journalEntryService.getDtlDtoWithCacheByUser(username, key, contentType);
        enrichEntries(username, contentType, retrieved == null ? List.of() : List.of(retrieved), false);
        return retrieved;
    }

    /**
     * 정책에 맞춰 해석/연관/상태 정보를 일괄 병합한다.
     *
     * @param username 사용자 아이디
     * @param contentType 콘텐츠 타입
     * @param listDto 대상 목록
     * @param includeStates 상태 병합 여부
     * @throws Exception 병합 중 예외
     */
    private void enrichEntries(
            final String username,
            final ContentType contentType,
            final List<JournalEntryDto> listDto,
            final boolean includeStates
    ) throws Exception {
        final JournalEntryTypePolicy policy = JournalEntryTypePolicy.from(contentType);
        if (policy.supportsInterpretation()) {
            interpretationEnricher.enrich(contentType, username, listDto);
        }
        relatedEnricher.enrich(contentType, username, listDto);
        if (!includeStates) return;
        stateEnricher.enrich(policy, username, listDto);
    }
}
