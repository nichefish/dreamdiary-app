package io.nicheblog.dreamdiary.feature.journal.thread.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.related.service.RelatedContentService;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntrySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryReflectionEnricher;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryStateEnricher;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDayViewType;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadSmpEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadMembershipStatsProjection;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadMembershipTagProjection;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadPeriodSummaryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadPeriodSummaryProjection;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadRepository;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JournalThreadEntryService
 * <pre>
 *  저널 스레드-엔트리 소속 관리 서비스 모듈.
 *
 *  스레드를 컨테이너로, 엔트리를 그 멤버로 잇는다. 한 엔트리가 여러 스레드에 속할 수 있다.
 *
 *  소속 해제는 소프트 삭제({@code deleted_at})다. UNIQUE KEY 가 deleted_at 을 포함하지 않으므로
 *  해제 후 재등록 시 INSERT 가 제약에 걸린다. 등록 경로는 기존 행을 먼저 찾아 되살린다.
 *
 *  본인이 소유한 스레드와 엔트리 사이의 소속만 등록하고, 본인이 등록한 소속만 조회·수정할 수 있다
 *  (tag_content·related_content 와 동일한 관례).
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalThreadEntryService {

    private final JournalThreadEntryRepository repository;
    private final JournalThreadRepository journalThreadRepository;
    private final JournalEntryService journalEntryService;
    private final JournalEntryReflectionEnricher journalEntryReflectionEnricher;
    private final JournalEntryStateEnricher journalEntryStateEnricher;
    private final RelatedContentService relatedContentService;

    /**
     * 엔트리를 스레드에 소속시킨다.
     * <p>
     * 멱등하다. 이미 살아있는 소속이면 아무것도 바꾸지 않고 성공으로 응답하고,
     * 해제된(소프트 삭제) 소속이면 되살린다.
     * 스레드와 대상 엔트리가 모두 현재 사용자 소유인지 확인한 뒤에만 등록·복원한다.
     *
     * @param threadId 스레드 ID
     * @param entryId 엔트리 ID
     * @param sortOrder 소속의 예약 정렬값. 현재 상세 표시는 원본 엔트리의 일자·sortOrder를 사용한다.
     * @return {@link ServiceResponse} -- 처리 결과
     */
    @Transactional
    public ServiceResponse regist(final Integer threadId, final Integer entryId, final Integer sortOrder) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        this.requireOwnedThread(threadId, username);
        final JournalEntryEntity entry = this.requireOwnedEntry(entryId, username);
        this.assertEligibleForThreadMembership(entry);

        final JournalThreadEntryEntity existing = repository.findAnyByPair(threadId, entryId, username).orElse(null);
        if (existing != null) {
            if (existing.getDeletedAt() == null) {
                log.info("[JournalThreadEntry.regist] 이미 소속된 엔트리 — 변경 없음. threadId={}, entryId={}", threadId, entryId);
                return new ServiceResponse(true, MessageUtils.getMessage("journal.thread.entry.regist.already"));
            }
            // 해제됐던 소속을 되살린다. INSERT 하면 UNIQUE KEY 에 걸린다.
            repository.reviveById(existing.getId());
            log.info("[JournalThreadEntry.regist] 해제됐던 소속 복원. threadId={}, entryId={}, id={}", threadId, entryId, existing.getId());
            return new ServiceResponse(true, MessageUtils.getMessage("journal.thread.entry.regist.success"));
        }

        final JournalThreadEntryEntity saved = repository.save(
                JournalThreadEntryEntity.builder()
                        .threadId(threadId)
                        .entryId(entryId)
                        .sortOrder(sortOrder)
                        .build()
        );
        log.info("[JournalThreadEntry.regist] 소속 등록. threadId={}, entryId={}, id={}", threadId, entryId, saved.getId());

        final ServiceResponse response = new ServiceResponse(true, MessageUtils.getMessage("journal.thread.entry.regist.success"));
        response.setRsltObj(this.toDto(saved));
        return response;
    }

    /**
     * 엔트리의 스레드 소속을 해제한다. (소프트 삭제)
     * <p>
     * 멱등하다. 이미 해제된 소속이면 성공으로 응답한다.
     *
     * @param threadId 스레드 ID
     * @param entryId 엔트리 ID
     * @return {@link ServiceResponse} -- 처리 결과
     */
    @Transactional
    public ServiceResponse delete(final Integer threadId, final Integer entryId) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        this.requireOwnedThread(threadId, username);

        final JournalThreadEntryEntity existing = repository.findAnyByPair(threadId, entryId, username).orElse(null);
        if (existing == null || existing.getDeletedAt() != null) {
            log.info("[JournalThreadEntry.delete] 해제할 소속 없음 — 변경 없음. threadId={}, entryId={}", threadId, entryId);
            return new ServiceResponse(true, MessageUtils.getMessage("journal.thread.entry.delete.already"));
        }
        repository.delete(existing);        // @SQLDelete 로 소프트 삭제된다.
        log.info("[JournalThreadEntry.delete] 소속 해제. threadId={}, entryId={}, id={}", threadId, entryId, existing.getId());
        return new ServiceResponse(true, MessageUtils.getMessage("journal.thread.entry.delete.success"));
    }

    /**
     * 스레드의 소속 엔트리를 화면 카드용 full DTO 로 조회한다.
     * <p>
     * {@link #getEntriesByThread(Integer, Collection)} 의 기본값 오버로드. 합성 대상 없음.
     *
     * @param threadId 스레드 ID
     * @return 소속 엔트리 DTO 목록 (일자, 원본 엔트리 sortOrder, ID 오름차순)
     */
    @Transactional(readOnly = true)
    public List<JournalEntryDto> getEntriesByThread(final Integer threadId) throws Exception {
        return this.getEntriesByThread(threadId, List.of());
    }

    /**
     * 스레드의 소속 엔트리를 화면 카드용 full DTO 로 조회한다.
     * <p>
     * 스레드 상세에서 소속 엔트리를 저널 일자와 동일한 카드로 보여주기 위한 계약이다.
     * Reflection 교차뷰({@code reflectionList})와 lifecycle 병합을 일자 조회와 같은 계약으로 채운다.
     * 소속 메타(JournalThreadEntryDto)가 아니라 실제 엔트리(JournalEntryDto)를 일자 오름차순으로 돌려준다.
     * 정렬 근거: 스레드의 선후는 엔트리 일자와 원본 엔트리 인덱스에서 파생한다
     * ({@link JournalEntryService#getListDtoByIds}).
     * 같은 일자 안에서는 원본 엔트리 {@code sortOrder}를 우선하고, 값이 없거나 중복된 경우에만
     * 엔트리 ID 오름차순을 tiebreak 로 써서 매 조회마다 순서가 뒤바뀌지 않게 고정한다.
     * <p>
     * {@code relatedThreadIds} 가 비어 있지 않으면 요청된 연관 스레드 중 실제 1-hop 연관인 것만 합성한다.
     * 합성 규칙(설계 정본: docs/migration/journal/thread-relation.md §2·§4):
     * <ul>
     *   <li>base·연관 양쪽 소속 엔트리는 한 번만, base 멤버로 표시 ({@code sourceThreadId=null})</li>
     *   <li>연관 스레드에서만 속한 엔트리는 {@code sourceThreadId}에 연관 스레드 ID를 세팅</li>
     *   <li>정렬은 base 단독 조회와 동일한 축(일자 → 챕터 sortOrder → 엔트리 sortOrder → ID)</li>
     * </ul>
     *
     * @param threadId 스레드 ID
     * @param relatedThreadIds 뷰에 합성할 연관 스레드 ID 목록 (화면 임시 선택). null/빈 목록이면 base만
     * @return 소속 엔트리 DTO 목록 (일자, 원본 엔트리 sortOrder, ID 오름차순)
     */
    @Transactional(readOnly = true)
    public List<JournalEntryDto> getEntriesByThread(final Integer threadId, final Collection<Integer> relatedThreadIds) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        this.requireOwnedThread(threadId, username);
        final List<Integer> entryIds = repository.findAllByThread(threadId, username).stream()
                .map(JournalThreadEntryEntity::getEntryId)
                .collect(Collectors.toList());
        final List<JournalEntryDto> entries = journalEntryService.getListDtoByIds(entryIds);
        journalEntryService.sortByChapterAndEntryOrder(entries);
        journalEntryReflectionEnricher.enrichMixed(entries);
        journalEntryStateEnricher.enrichLifecycleMixed(entries);
        final List<JournalEntryDto> nestedReflections = entries.stream()
                .filter(entry -> entry != null && entry.getReflectionList() != null)
                .flatMap(entry -> entry.getReflectionList().stream())
                .filter(reflection -> reflection != null)
                .toList();
        if (!nestedReflections.isEmpty()) {
            journalEntryStateEnricher.enrichLifecycleMixed(nestedReflections);
        }

        if (CollectionUtils.isEmpty(relatedThreadIds)) return entries;

        // ----- 연관 스레드 엔트리 합성 -----
        // base 소속 entryId 집합 (중복 제거 기준)
        final Set<Integer> baseEntryIds = entries.stream()
                .filter(e -> e != null && e.getId() != null)
                .map(JournalEntryDto::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 요청 ID ∩ 실제 1-hop 연관 ID — 비연관 ID는 무시한다
        final Set<Integer> allowedRelatedIds = new LinkedHashSet<>(this.resolveRelatedThreadIds(threadId, username));
        final List<Integer> selectedRelatedIds = relatedThreadIds.stream()
                .filter(id -> id != null && allowedRelatedIds.contains(id))
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(selectedRelatedIds)) return entries;

        // 연관 스레드별 엔트리 합성
        final List<JournalEntryDto> merged = new ArrayList<>(entries);
        for (final Integer relatedThreadId : selectedRelatedIds) {
            final List<Integer> relatedEntryIds = repository.findAllByThread(relatedThreadId, username).stream()
                    .map(JournalThreadEntryEntity::getEntryId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(relatedEntryIds)) continue;

            // base 소속과 중복된 엔트리는 건너뛴다 — base 멤버로 취급
            final List<Integer> borrowedIds = relatedEntryIds.stream()
                    .filter(id -> !baseEntryIds.contains(id))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(borrowedIds)) continue;

            final List<JournalEntryDto> borrowedEntries = journalEntryService.getListDtoByIds(borrowedIds);
            journalEntryReflectionEnricher.enrichMixed(borrowedEntries);
            journalEntryStateEnricher.enrichLifecycleMixed(borrowedEntries);
            // provenance 세팅: 연관 스레드에서 빌려온 엔트리에 출처 스레드 ID를 표시
            borrowedEntries.forEach(entry -> {
                if (entry != null) entry.setSourceThreadId(relatedThreadId);
            });
            merged.addAll(borrowedEntries);
            // baseEntryIds에 추가해 다음 연관 스레드와의 중복도 방지
            borrowedEntries.stream()
                    .filter(e -> e != null && e.getId() != null)
                    .forEach(e -> baseEntryIds.add(e.getId()));
        }

        // 합성 후 전체를 동일 정렬축으로 재정렬
        journalEntryService.sortByChapterAndEntryOrder(merged);
        log.debug("[JournalThreadEntry.getEntriesByThread] 연관 합성. threadId={}, base={}, merged={}, relatedThreads={}",
                threadId, entries.size(), merged.size(), selectedRelatedIds.size());
        return merged;
    }

    /**
     * base 스레드에 직접 연관된 스레드 ID 목록을 조회한다 (1-hop 대칭).
     * <p>
     * related_content 테이블에서 JOURNAL_THREAD 타입 연관 행을 읽어 상대방 스레드 ID를 추출한다.
     * 소유권 검증은 {@link RelatedContentService#getListDtoByRef} 에서 수행한다.
     *
     * @param threadId base 스레드 ID
     * @param username 로그인 사용자
     * @return 연관 스레드 ID 목록 (없으면 빈 목록)
     */
    private List<Integer> resolveRelatedThreadIds(final Integer threadId, final String username) {
        try {
            return relatedContentService
                    .getListDtoByRef(threadId, ContentType.JOURNAL_THREAD)
                    .stream()
                    .map(RelatedContentDto::getTargetId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            log.warn("[JournalThreadEntry.resolveRelatedThreadIds] 연관 스레드 조회 실패 — 합성 건너뜀. threadId={}, username={}, error={}",
                    threadId, username, e.getMessage());
            return List.of();
        }
    }


    /**
     * 여러 스레드의 소속 엔트리 ID 를 스레드별로 묶는다. (목록 태그 집계용)
     *
     * @param threadIds 스레드 ID 집합
     * @return threadId → entryId 목록 (등록 순). 소속이 없는 스레드는 키 자체가 없을 수 있다.
     */
    @Transactional(readOnly = true)
    public Map<Integer, List<Integer>> getEntryIdsGroupedByThread(final Collection<Integer> threadIds) throws Exception {
        if (CollectionUtils.isEmpty(threadIds)) return Map.of();
        final String username = AuthUtils.requireLoginUsername();
        final Map<Integer, List<Integer>> grouped = new LinkedHashMap<>();
        for (final JournalThreadEntryEntity membership : repository.findAllByThreadIds(threadIds, username)) {
            if (membership == null || membership.getThreadId() == null || membership.getEntryId() == null) continue;
            grouped.computeIfAbsent(membership.getThreadId(), ignored -> new ArrayList<>()).add(membership.getEntryId());
        }
        return grouped;
    }


    /**
     * 스레드 목록 enrich 용 소속 수·기간 집계.
     *
     * @param threadIds 스레드 ID 집합
     * @return 스레드별 집계. 소속 없는 스레드는 결과에 없다.
     */
    @Transactional(readOnly = true)
    public List<JournalThreadMembershipStatsProjection> getMembershipStatsByThreadIds(
            final Collection<Integer> threadIds
    ) throws Exception {
        if (CollectionUtils.isEmpty(threadIds)) return List.of();
        final String username = AuthUtils.requireLoginUsername();
        return repository.findMembershipStatsByThreadIds(threadIds, username);
    }

    /**
     * 스레드 목록 enrich 용 소속 엔트리 태그 원천 행.
     *
     * @param threadIds 스레드 ID 집합
     * @return 스레드·태그 행 (tagId 중복 가능)
     */
    @Transactional(readOnly = true)
    public List<JournalThreadMembershipTagProjection> getMembershipTagsByThreadIds(
            final Collection<Integer> threadIds
    ) throws Exception {
        if (CollectionUtils.isEmpty(threadIds)) return List.of();
        final String username = AuthUtils.requireLoginUsername();
        return repository.findMembershipTagsByThreadIds(threadIds, username);
    }

    /**
     * 엔트리가 속한 스레드 목록 조회.
     * 한 엔트리가 여러 스레드에 속할 수 있다.
     *
     * @param entryId 엔트리 ID
     * @return 소속 목록 (등록 순)
     */
    @Transactional(readOnly = true)
    public List<JournalThreadEntryDto> getListByEntry(final Integer entryId) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return repository.findAllByEntryIdAndCreatedByOrderByCreatedAtAsc(entryId, username).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 여러 엔트리의 소속을 한 번에 조회해 엔트리 ID 별로 묶는다. (목록 화면 N+1 방지)
     * <p>
     * 소속이 없는 엔트리는 결과 맵에 키가 아예 없다. 호출부에서 기본값을 쓴다.
     * 소유권 검증은 {@code createdBy} 조건으로 대신한다 — 해당 사용자의 소속만 조회된다.
     * <p>
     * username 을 파라미터로 받는 이유: 이 메서드를 부르는 보강(enrich) 경로는 조회 대상 사용자를
     * 명시적으로 넘겨받는다({@code getRelatedContentMapByRefs} 와 동일한 계약).
     * 여기서만 로그인 사용자를 직접 읽으면 그 계약이 깨진다.
     *
     * @param entryIds 대상 엔트리 ID 목록
     * @param username 조회 대상 사용자 계정명
     * @return 엔트리 ID -> 소속 목록
     */
    @Transactional(readOnly = true)
    public Map<Integer, List<JournalThreadEntryDto>> getMapByEntryIds(
            final Collection<Integer> entryIds,
            final String username
    ) throws Exception {
        if (CollectionUtils.isEmpty(entryIds)) return Map.of();
        final String resolvedUsername = AuthUtils.requireUsername(username);
        return repository.findAllByEntryIds(entryIds, resolvedUsername).stream()
                .map(this::toDto)
                .collect(Collectors.groupingBy(JournalThreadEntryDto::getEntryId));
    }

    /**
     * 월간·주간 저널 화면의 기간별 스레드 요약을 조회한다.
     * <p>
     * 변경 전에는 기간 요약 계약이 없어 화면이 현재 필터가 적용된 일자 목록을 재집계해야 했다.
     * 변경 후에는 활성 소속을 서버에서 직접 집계해 일기/꿈 표시·키워드·챕터 필터와 무관한
     * 기간 전체 스레드 목록을 반환한다.
     * <p>
     * 주간은 최초 등장일순, 월간·연간은 기간 내 엔트리 수 내림차순으로 정렬한다.
     * 동률은 최초 등장일과 스레드 ID로 고정해 응답 순서가 매 조회마다 바뀌지 않게 한다.
     *
     * @param viewType {@link JournalDayViewType#WEEKLY}, {@link JournalDayViewType#LIST} 또는 {@link JournalDayViewType#ANNUAL}
     * @param searchParam 주간 시작일 또는 연·월
     * @return 기간별 스레드 요약
     */
    @Transactional(readOnly = true)
    public List<JournalThreadPeriodSummaryDto> getPeriodSummary(
            final JournalDayViewType viewType,
            final JournalDaySearchParam searchParam
    ) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final List<JournalThreadPeriodSummaryProjection> projections;
        final Comparator<JournalThreadPeriodSummaryDto> comparator;

        if (viewType == JournalDayViewType.WEEKLY) {
            final LocalDate weekStartDt = parseWeekStartDt(searchParam != null ? searchParam.getWeekStartDt() : null);
            projections = repository.findPeriodSummaryByWeekStartDt(username, weekStartDt);
            comparator = Comparator
                    .comparing(JournalThreadPeriodSummaryDto::getFirstEntryDate,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(JournalThreadPeriodSummaryDto::getThreadId,
                            Comparator.nullsLast(Comparator.naturalOrder()));
        } else if (viewType == JournalDayViewType.LIST) {
            final Integer yy = searchParam != null ? searchParam.getYy() : null;
            final Integer mnth = searchParam != null ? searchParam.getMnth() : null;
            if (yy == null || yy < 1 || mnth == null || mnth < 1 || mnth > 12) {
                log.warn("[JournalThreadEntry.periodSummary] 잘못된 월간 기간. yy={}, mnth={}", yy, mnth);
                throw new IllegalArgumentException("월간 스레드 집계에는 올바른 yy와 mnth가 필요합니다.");
            }
            projections = repository.findPeriodSummaryByMonth(username, yy, mnth);
            comparator = Comparator
                    .comparingLong(JournalThreadPeriodSummaryDto::getEntryCount)
                    .reversed()
                    .thenComparing(JournalThreadPeriodSummaryDto::getFirstEntryDate,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(JournalThreadPeriodSummaryDto::getThreadId,
                            Comparator.nullsLast(Comparator.naturalOrder()));
        } else if (viewType == JournalDayViewType.ANNUAL) {
            final Integer yy = searchParam != null ? searchParam.getYy() : null;
            if (yy == null || yy < 1) {
                log.warn("[JournalThreadEntry.periodSummary] 잘못된 연간 기간. yy={}", yy);
                throw new IllegalArgumentException("연간 스레드 집계에는 올바른 yy가 필요합니다.");
            }
            projections = repository.findPeriodSummaryByYear(username, yy);
            comparator = Comparator
                    .comparingLong(JournalThreadPeriodSummaryDto::getEntryCount)
                    .reversed()
                    .thenComparing(JournalThreadPeriodSummaryDto::getFirstEntryDate,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(JournalThreadPeriodSummaryDto::getThreadId,
                            Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            log.warn("[JournalThreadEntry.periodSummary] 지원하지 않는 보기 타입. viewType={}", viewType);
            throw new IllegalArgumentException("기간별 스레드 집계는 LIST, WEEKLY, ANNUAL 보기만 지원합니다.");
        }

        return projections.stream()
                .map(this::toPeriodSummaryDto)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * 스레드의 소속을 일괄 해제한다. (스레드 삭제 시 정리용)
     *
     * @param threadId 스레드 ID
     * @return 해제된 소속 수
     */
    @Transactional
    public int deleteAllByThread(final Integer threadId) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final int affected = repository.softDeleteAllByThread(threadId, username);
        log.info("[JournalThreadEntry.deleteAllByThread] 스레드 소속 일괄 해제. threadId={}, affected={}", threadId, affected);
        return affected;
    }

    /**
     * 대상 스레드가 존재하고 본인 소유인지 확인한다.
     *
     * @param threadId 스레드 ID
     * @param username 사용자 계정명
     */
    private void requireOwnedThread(final Integer threadId, final String username) throws Exception {
        final JournalThreadEntity thread = journalThreadRepository.findById(threadId)
                .orElseThrow(() -> new BusinessException("journal.thread.not-found"));
        if (!AuthUtils.isCreatedBy(thread.getCreatedBy())) {
            log.warn("[JournalThreadEntry] 타인 소유 스레드 접근 차단. threadId={}, username={}", threadId, username);
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
    }

    /**
     * 대상 엔트리가 존재하고 현재 사용자 소유인지 확인한다.
     * <p>
     * 소속 행을 찾거나 되살리기 전에 호출해, 직접 API 요청으로 타인 엔트리나
     * 존재하지 않는 엔트리의 소속이 저장되는 것을 차단한다.
     *
     * @param entryId 엔트리 ID
     * @param username 사용자 계정명
     */
    private JournalEntryEntity requireOwnedEntry(final Integer entryId, final String username) throws Exception {
        final JournalEntryEntity entry;
        try {
            entry = journalEntryService.getDtlEntity(entryId);
        } catch (final EntityNotFoundException exception) {
            log.warn("[JournalThreadEntry] 소속 대상 엔트리 없음. entryId={}, username={}", entryId, username);
            throw exception;
        }
        if (!username.equals(entry.getCreatedBy())) {
            log.warn("[JournalThreadEntry] 타인 소유 엔트리 소속 차단. entryId={}, username={}, createdBy={}",
                    entryId, username, entry.getCreatedBy());
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
        return entry;
    }

    /**
     * 일기·꿈·노트를 target으로 둔 Reflection은 스레드 소속 추가 대상이 아니다.
     * 정본: docs/spec/REFLECTION_ONE_TYPE.md §4
     */
    private void assertEligibleForThreadMembership(final JournalEntryEntity entry) {
        if (entry == null || !ContentType.JOURNAL_REFLECTION.key.equals(entry.getContentType())) return;
        if (entry.getRefId() == null) return;
        final ContentType targetType = entry.getRefContentType();
        if (targetType != ContentType.JOURNAL_DIARY
                && targetType != ContentType.JOURNAL_DREAM
                && targetType != ContentType.JOURNAL_NOTE) {
            return;
        }
        log.warn("[JournalThreadEntry.regist] 원문 target Reflection 스레드 소속 차단. entryId={}, refId={}, refContentType={}",
                entry.getId(), entry.getRefId(), targetType);
        throw new BusinessException("journal.thread.entry.regist.reflection-target-forbidden");
    }

    /**
     * Entity -> Dto 변환.
     * 조인된 스레드·엔트리가 없을 수 있어(@NotFound IGNORE) null 방어한다.
     *
     * @param entity 소속 엔티티
     * @return {@link JournalThreadEntryDto} 변환된 DTO
     */
    private JournalThreadEntryDto toDto(final JournalThreadEntryEntity entity) {
        final JournalThreadSmpEntity thread = entity.getJournalThread();
        final JournalEntrySmpEntity entry = entity.getJournalEntry();
        return JournalThreadEntryDto.builder()
                .id(entity.getId())
                .threadId(entity.getThreadId())
                .entryId(entity.getEntryId())
                .sortOrder(entity.getSortOrder())
                .threadTitle(thread != null ? thread.getTitle() : null)
                .entryContentType(entry != null ? entry.getContentType() : null)
                .build();
    }

    /**
     * 주 시작일 문자열을 엄격한 ISO 일자로 변환한다.
     *
     * @param value YYYY-MM-DD 문자열
     * @return 주 시작일
     */
    private LocalDate parseWeekStartDt(final String value) {
        try {
            if (value == null || value.isBlank()) throw new DateTimeParseException("blank", "", 0);
            return LocalDate.parse(value.trim());
        } catch (final DateTimeParseException exception) {
            log.warn("[JournalThreadEntry.periodSummary] 잘못된 주간 기간. weekStartDt={}", value);
            throw new IllegalArgumentException("주간 스레드 집계에는 올바른 weekStartDt가 필요합니다.", exception);
        }
    }

    /**
     * 기간 집계 Projection을 API DTO로 변환한다.
     *
     * @param projection 스레드별 기간 집계
     * @return 기간 요약 DTO
     */
    private JournalThreadPeriodSummaryDto toPeriodSummaryDto(
            final JournalThreadPeriodSummaryProjection projection
    ) {
        return JournalThreadPeriodSummaryDto.builder()
                .threadId(projection.getThreadId())
                .title(projection.getTitle())
                .prefix(toPeriodSummaryPrefixDto(projection))
                .entryCount(projection.getEntryCount() != null ? projection.getEntryCount().longValue() : 0L)
                .firstEntryDate(projection.getFirstEntryDate())
                .build();
    }

    /**
     * 기간 요약 Projection의 nullable 말머리를 공통 {@link PrefixDto} 계약으로 변환한다.
     * 비활성 과거 말머리도 연결이 남아 있으면 이름·색과 활성 상태를 그대로 반환한다.
     *
     * @param projection 스레드별 기간 집계
     * @return 선택된 말머리 DTO 또는 null
     */
    private PrefixDto toPeriodSummaryPrefixDto(final JournalThreadPeriodSummaryProjection projection) {
        if (projection.getPrefixId() == null) return null;
        return PrefixDto.builder()
                .id(projection.getPrefixId())
                .name(projection.getPrefixName())
                .color(projection.getPrefixColor())
                .sortOrder(0)
                .activeYn(projection.getPrefixActiveYn())
                .build();
    }
}
