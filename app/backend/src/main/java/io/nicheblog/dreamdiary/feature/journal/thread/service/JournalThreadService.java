package io.nicheblog.dreamdiary.feature.journal.thread.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableHistoryHelper;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixContentService;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalLifecycleViewHelper;
import io.nicheblog.dreamdiary.feature.attachable.managt.event.ManagtrAddEvent;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.mapstruct.JournalThreadMapstruct;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadCandidateDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadMembershipStatsProjection;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadMembershipTagProjection;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadCandidateProjection;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadRepository;
import io.nicheblog.dreamdiary.feature.journal.thread.spec.JournalThreadSpec;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.Objects;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.HashMap;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageImpl;
import io.nicheblog.dreamdiary.feature.journal.thread.model.ThreadLatestEntryDateProjection;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;

/**
 * JournalThreadService
 * <pre>
 *  저널 스레드 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalThreadService
        implements BaseAttachableService<JournalThreadDto, JournalThreadDto, Integer, JournalThreadEntity>, BaseMultipartWritableService<JournalThreadDto, JournalThreadDto, Integer, JournalThreadEntity> {

    @Getter
    private final JournalThreadRepository repository;
    private final JournalThreadEntryService journalThreadEntryService;
    private final LifecycleService lifecycleService;
    private final PrefixContentService prefixContentService;
    @Getter
    private final JournalThreadSpec spec;
    @Getter
    private final JournalThreadMapstruct mapstruct = JournalThreadMapstruct.INSTANCE;
    public JournalThreadMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalThreadMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationEventPublisherWrapper publisher;

    /** 엔트리 소속 메뉴의 기본 후보 수. */
    private static final int DEFAULT_CANDIDATE_LIMIT = 7;
    /** 과도한 소속 후보 조회를 막는 서버 상한. */
    private static final int MAX_CANDIDATE_LIMIT = 20;

    /**
     * 엔트리 소속 메뉴에 노출할 스레드 후보를 조회한다.
     * <p>
     * 현재 소속, 최근 소속 추가 시각, 활성 소속 수, 스레드 수정·생성 시각 순의
     * 서버 우선순위를 사용한다. 검색어와 말머리는 후보 집합을 먼저 좁힌 뒤 같은 순위를 적용한다.
     * 기본은 완료({@code RESOLVED}) 스레드를 숨기고, {@code includeResolved=true} 일 때만 포함한다.
     * </p>
     *
     * @param entryId 후보를 요청한 엔트리 ID
     * @param keyword 제목 검색어
     * @param prefixId 말머리 ID
     * @param includeResolved 완료 스레드 포함 여부
     * @param limit 최대 후보 수
     * @return 경량 스레드 후보 목록
     */
    @Transactional(readOnly = true)
    public List<JournalThreadCandidateDto> getCandidates(
            final Integer entryId,
            final String keyword,
            final Integer prefixId,
            final Boolean includeResolved,
            final Integer limit
    ) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final String resolvedKeyword = StringUtils.trimToEmpty(keyword);
        final String resolvedIncludeResolved = Boolean.TRUE.equals(includeResolved) ? "Y" : "N";
        final int requestedLimit = (limit == null) ? DEFAULT_CANDIDATE_LIMIT : limit;
        final int resolvedLimit = Math.max(1, Math.min(requestedLimit, MAX_CANDIDATE_LIMIT));
        if (requestedLimit != resolvedLimit) {
            log.warn("[JournalThread.candidates] 후보 수 범위 보정. requestedLimit={}, resolvedLimit={}, username={}",
                    requestedLimit, resolvedLimit, username);
        }

        final List<JournalThreadCandidateProjection> candidates = repository.findCandidates(
                username,
                entryId,
                resolvedKeyword,
                prefixId,
                resolvedIncludeResolved,
                PageRequest.of(0, resolvedLimit)
        );
        log.debug("[JournalThread.candidates] 후보 조회. entryId={}, keyword={}, prefixId={}, includeResolved={}, limit={}, size={}, username={}",
                entryId, resolvedKeyword, prefixId, resolvedIncludeResolved, resolvedLimit, candidates.size(), username);
        return candidates.stream()
                .map(candidate -> JournalThreadCandidateDto.builder()
                        .id(candidate.getId())
                        .title(candidate.getTitle())
                        .prefix(toPrefixDto(candidate))
                        .lifecycleKey(StringUtils.defaultIfBlank(candidate.getLifecycleKey(), "OPEN"))
                        .membershipCount(candidate.getMembershipCount() == null
                                ? 0L
                                : candidate.getMembershipCount().longValue())
                        .lastMembershipAt(candidate.getLastMembershipAt())
                        .member(candidate.getCurrentEntryMembershipCount() != null
                                && candidate.getCurrentEntryMembershipCount().longValue() > 0)
                        .build())
                .toList();
    }


    /**
     * 페이징 목록 조회. 소속 엔트리의 최신 일기 날짜 기준으로 정렬하고 소속 집계·라이프사이클을 붙인다.
     * <p>
     * 필터는 기존 Spec 을 그대로 재사용해(필터·soft-delete 계약 단일화) 매칭 스레드 전체를 조회한 뒤,
     * 소속 엔트리(일기/꿈/노트)의 {@code journal_day.journal_date} 최대값 기준으로 최신 스레드를 앞에 둔다.
     * 소속 엔트리가 없는 스레드는 뒤로 보낸다(동점은 스레드 {@code createdAt} DESC → id DESC). 파생 정렬이라
     * 단순 property Sort 로는 표현할 수 없어 정렬·페이징을 이 메서드에서 수행한다. 페이지 DTO 에 소속 엔트리
     * 태그 합집합·활성 소속 수·소속 기간 집계와 라이프사이클을 병합한다. 엔트리 풀 DTO 로드는 하지 않는다.
     * </p>
     *
     * @param searchParamMap 검색 조건
     * @param pageable 페이징(정렬은 이 메서드가 소속 엔트리 최신 날짜 기준으로 수행)
     * @return 소속 엔트리 최신 날짜로 정렬되고 집계가 채워진 페이징 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public Page<JournalThreadDto> getPageDto(final Map<String, Object> searchParamMap, final Pageable pageable) throws Exception {
        // 필터는 기존 Spec 재사용(SSOT). 원본 getPageDto 와 동일하게 빈 값·비검색 키를 정리한 뒤 조회한다.
        final Map<String, Object> filteredSearchKey = CmmUtils.filterParamMap(searchParamMap);
        final List<JournalThreadEntity> filtered = this.getListEntity(filteredSearchKey);
        if (filtered.isEmpty()) return this.pageEntityToDto(new PageImpl<>(List.of(), pageable, 0));

        // 소속 엔트리 최신 일기 날짜 집계.
        final List<Integer> threadIds = filtered.stream().map(JournalThreadEntity::getId).toList();
        final Map<Integer, LocalDate> latestByThread = new HashMap<>();
        for (final ThreadLatestEntryDateProjection row : repository.findLatestMemberEntryDates(threadIds)) {
            latestByThread.put(row.getThreadId(), row.getLatestDate());
        }

        // 최신 날짜 DESC → 없으면 뒤 → 스레드 createdAt DESC → id DESC.
        final List<JournalThreadEntity> sorted = filtered.stream()
                .sorted(Comparator
                        .comparing((JournalThreadEntity t) -> latestByThread.get(t.getId()),
                                Comparator.nullsLast(Comparator.<LocalDate>reverseOrder()))
                        .thenComparing(JournalThreadEntity::getCreatedAt,
                                Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder()))
                        .thenComparing(JournalThreadEntity::getId, Comparator.reverseOrder()))
                .toList();

        final int total = sorted.size();
        final int from = (int) pageable.getOffset();
        final List<JournalThreadEntity> pageEntities = from >= total
                ? List.of()
                : sorted.subList(from, Math.min(from + pageable.getPageSize(), total));

        // 변환(rnum 포함)은 원본 pageEntityToDto 재사용, 이후 소속 집계·라이프사이클 병합.
        final Page<JournalThreadDto> page = this.pageEntityToDto(new PageImpl<>(pageEntities, pageable, total));
        this.applyEntryTagSummaries(page.getContent());
        this.applyThreadLifecycles(page.getContent());
        return page;
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto 등록된 객체
     */
    @Override
    public void postRegist(final JournalThreadDto updatedDto) throws Exception {
        // 조치자 추가는 메인 로직과 분리한다.
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getAttachableKey()));
        // 잔디 메시지 발송은 메인 로직과 분리한다.
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyJournalThreadReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 스레드 본문과 단일 Prefix 참조를 같은 트랜잭션에서 등록한다.
     */
    @Override
    @Transactional
    public ServiceResponse regist(final JournalThreadDto registDto) throws Exception {
        final ServiceResponse response = BaseAttachableService.super.regist(registDto);
        final JournalThreadDto updatedDto = (JournalThreadDto) response.getRsltObj();
        applyPrefixSelection(updatedDto, registDto.getPrefixId());
        return response;
    }

    /**
     * 스레드 본문과 단일 Prefix 참조를 같은 트랜잭션에서 수정한다.
     */
    @Override
    @Transactional
    public ServiceResponse modify(final JournalThreadDto modifyDto) throws Exception {
        final ServiceResponse response = BaseAttachableService.super.modify(modifyDto);
        final JournalThreadDto updatedDto = (JournalThreadDto) response.getRsltObj();
        applyPrefixSelection(updatedDto, modifyDto.getPrefixId());
        return response;
    }

    /**
     * 상세 페이지 조회 전처리. (dto level)
     *
     * @param key 조회할 DTO 식별자
     */
    @Transactional
    public JournalThreadDto viewDetailPage(final Integer key) throws Exception {
        final JournalThreadDto dto = this.getDtlDto(key);
        this.applyEntryTagSummary(dto);
        this.applyThreadLifecycles(List.of(dto));
        return dto;
    }

    /**
     * 사용자 소유 스레드를 이력 조회 대상으로 반환한다.
     *
     * @param username 소유 사용자 계정명
     * @param key 스레드 ID
     * @return 사용자 소유 스레드 DTO
     * @throws Exception 조회·변환 중 예외
     */
    @Transactional(readOnly = true)
    public JournalThreadDto getDtlDtoByUser(final String username, final Integer key) throws Exception {
        final JournalThreadEntity entity = repository.findByIdAndCreatedBy(key, username)
                .orElseThrow(() -> new NotAuthorizedException("common.result.access-not-authorized"));
        return mapstruct.toDto(entity);
    }

    /**
     * 이력 복원 본문을 사용자 소유 스레드에 반영한다.
     * 현재 본문은 복원 이력의 새 스냅샷으로 남고 제목·말머리·소속은 유지된다.
     *
     * @param key 스레드 ID
     * @param content 복원할 본문
     * @param historyType 이력 유형
     * @param fromHistoryId 복원 원본 이력 ID
     * @return 복원된 스레드 DTO
     * @throws Exception 소유권 검증·저장 중 예외
     */
    @Transactional
    public JournalThreadDto updtContent(
            final Integer key,
            final String content,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final JournalThreadEntity restoreEntity = repository.findByIdAndCreatedBy(key, username)
                .orElseThrow(() -> new NotAuthorizedException("common.result.access-not-authorized"));
        final JournalThreadEntity historySnapshot = restoreEntity.toBuilder().build();

        restoreEntity.setContent(content);
        BaseAttachableHistoryHelper.applyModifyHistory(historySnapshot, restoreEntity);

        final JournalThreadEntity updatedEntity = repository.saveAndFlush(restoreEntity);
        BaseAttachableHistoryHelper.publishHistoryEventIfSupported(
                this, historySnapshot, updatedEntity, historyType, fromHistoryId);
        log.info("[JournalThread.history] 본문 이력 복원. threadId={}, fromHistoryId={}, username={}",
                key, fromHistoryId, username);
        return mapstruct.toDto(updatedEntity);
    }

    /** 선택한 Prefix의 소유권·활성 상태를 검증하고 prefix_content 연결을 반영한다. */
    private void applyPrefixSelection(final JournalThreadDto dto, final Integer prefixId) {
        final JournalThreadEntity entity = repository.findByIdAndCreatedBy(
                        dto.getId(), AuthUtils.requireLoginUsername())
                .orElseThrow(() -> new javax.persistence.EntityNotFoundException("Journal thread not found."));
        final BaseAttachableKey key = new BaseAttachableKey(entity.getId(), ContentType.JOURNAL_THREAD.key);
        final PrefixEntity prefix = prefixContentService.applySelection(
                key, ContentType.JOURNAL_THREAD.key, prefixId);
        dto.setPrefix(prefix == null ? null : PrefixDto.builder()
                .id(prefix.getId())
                .name(prefix.getName())
                .color(prefix.getColor())
                .sortOrder(prefix.getSortOrder())
                .activeYn(prefix.getActiveYn())
                .build());
        dto.setPrefixId(prefixId);
    }

    private PrefixDto toPrefixDto(final JournalThreadCandidateProjection candidate) {
        if (candidate.getPrefixId() == null) return null;
        return PrefixDto.builder()
                .id(candidate.getPrefixId())
                .name(candidate.getPrefixName())
                .color(candidate.getPrefixColor())
                .sortOrder(0)
                .activeYn(candidate.getPrefixActiveYn())
                .build();
    }

    /**
     * 스레드 목록·상세에 부착 라이프사이클을 일괄 병합한다.
     * <p>
     * 행이 없으면 {@code OPEN}. 엔트리 enrich 와 동일 계약이다.
     * </p>
     *
     * @param dtoList 대상 스레드 DTO 목록
     */
    private void applyThreadLifecycles(final List<JournalThreadDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) return;
        final List<Integer> threadIds = dtoList.stream()
                .map(JournalThreadDto::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (threadIds.isEmpty()) return;
        JournalLifecycleViewHelper.applyThreadLifecycle(
                dtoList,
                lifecycleService.getLifecycleMap(ContentType.JOURNAL_THREAD, threadIds)
        );
    }

    /**
     * 소속 엔트리 태그를 스레드 표시 태그로 집계한다. (단건 — 상세)
     * <p>
     * 저널 챕터의 {@code applyChapterTagSummary} 와 동형이다. 스레드는 자체 태그를 소유하지 않으므로
     * (엔티티 TagEmbed 제거) 화면 태그는 소속 엔트리 태그의 합집합이다. tagId 로 중복 제거한다.
     * 엔티티에서 tag 가 매핑되지 않으므로 컨테이너를 non-null 로 보장한 뒤 집계 목록을 채운다.
     *
     * @param dto 대상 스레드 DTO
     */
    private void applyEntryTagSummary(final JournalThreadDto dto) throws Exception {
        if (dto == null || dto.getId() == null) return;
        this.applyEntryTagSummaries(List.of(dto));
    }

    /**
     * 여러 스레드에 소속 엔트리 태그 집계·활성 소속 수·소속 기간을 일괄 적용한다.
     * <p>
     * 목록·상세 공통이다. 엔트리 풀 DTO 로드 없이 소속 집계 쿼리 두 번으로
     * {@code membershipCount}/{@code firstEntryDate}/{@code lastEntryDate}/태그 합집합을 채운다.
     * </p>
     *
     * @param dtoList 대상 스레드 DTO 목록
     */
    private void applyEntryTagSummaries(final List<JournalThreadDto> dtoList) throws Exception {
        if (CollectionUtils.isEmpty(dtoList)) return;

        final List<Integer> threadIds = dtoList.stream()
                .map(JournalThreadDto::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (threadIds.isEmpty()) return;

        final Map<Integer, JournalThreadMembershipStatsProjection> statsByThread = new LinkedHashMap<>();
        for (final JournalThreadMembershipStatsProjection stats
                : journalThreadEntryService.getMembershipStatsByThreadIds(threadIds)) {
            if (stats == null || stats.getThreadId() == null) continue;
            statsByThread.put(stats.getThreadId(), stats);
        }

        final Map<Integer, Map<Integer, TagContentDto>> tagsByThread = new LinkedHashMap<>();
        for (final JournalThreadMembershipTagProjection row
                : journalThreadEntryService.getMembershipTagsByThreadIds(threadIds)) {
            if (row == null || row.getThreadId() == null || row.getTagId() == null) continue;
            final Map<Integer, TagContentDto> tagMap = tagsByThread.computeIfAbsent(
                    row.getThreadId(), ignored -> new LinkedHashMap<>());
            tagMap.putIfAbsent(row.getTagId(), TagContentDto.builder()
                    .tagId(row.getTagId())
                    .name(row.getName())
                    .ctgr(row.getCtgr())
                    .build());
        }

        for (final JournalThreadDto dto : dtoList) {
            if (dto == null || dto.getId() == null) continue;
            final JournalThreadMembershipStatsProjection stats = statsByThread.get(dto.getId());
            if (stats == null) {
                dto.setMembershipCount(0L);
                dto.setFirstEntryDate(null);
                dto.setLastEntryDate(null);
            } else {
                final Number count = stats.getMembershipCount();
                dto.setMembershipCount(count == null ? 0L : count.longValue());
                applyMembershipPeriod(dto, stats.getFirstEntryDate(), stats.getLastEntryDate());
            }
            if (dto.getTag() == null) dto.setTag(new TagCmpstn());
            final Map<Integer, TagContentDto> tagMap = tagsByThread.getOrDefault(dto.getId(), Map.of());
            dto.getTag().setList(new ArrayList<>(tagMap.values()));
        }
    }

    /**
     * 소속 기준일 min/max 를 목록 표시용 문자열로 채운다.
     *
     * @param dto 대상 스레드
     * @param first 최소 기준일
     * @param last 최대 기준일
     */
    static void applyMembershipPeriod(
            final JournalThreadDto dto,
            final LocalDate first,
            final LocalDate last
    ) {
        if (dto == null) return;
        dto.setFirstEntryDate(first == null ? null : first.toString());
        dto.setLastEntryDate(last == null ? null : last.toString());
    }

    /**
     * 소속 엔트리 DTO 목록에서 기준일 min/max 를 계산한다. (단위 테스트용)
     *
     * @param dto 대상 스레드
     * @param membershipEntries 활성 소속 엔트리
     */
    static void applyMembershipPeriod(
            final JournalThreadDto dto,
            final List<JournalEntryDto> membershipEntries
    ) {
        if (dto == null) return;
        String first = null;
        String last = null;
        if (membershipEntries != null) {
            for (final JournalEntryDto entry : membershipEntries) {
                if (entry == null) continue;
                final String day = normalizeEntryDate(entry.getStdrdDt());
                if (day == null) continue;
                if (first == null || day.compareTo(first) < 0) first = day;
                if (last == null || day.compareTo(last) > 0) last = day;
            }
        }
        dto.setFirstEntryDate(first);
        dto.setLastEntryDate(last);
    }

    /**
     * 엔트리 기준일을 {@code YYYY-MM-DD} 로 정규화한다.
     *
     * @param stdrdDt 원본 기준일
     * @return 정규화된 일자. 비어 있으면 {@code null}
     */
    static String normalizeEntryDate(final String stdrdDt) {
        if (StringUtils.isBlank(stdrdDt)) return null;
        final String trimmed = stdrdDt.trim();
        return trimmed.length() >= 10 ? trimmed.substring(0, 10) : trimmed;
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 수정 요청 객체
     * @param modifyEntity 수정 대상 엔티티
     */
    @Override
    public void preModify(final JournalThreadDto modifyDto, final JournalThreadEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
    }

    /**
     * 수정 후처리. (override)
     *
     * @param postDto 수정 요청 객체
     * @param updatedDto 수정 결과 객체
     */
    @Override
    public void postModify(final JournalThreadDto postDto, final JournalThreadDto updatedDto) throws Exception {
        // 조치자 추가는 메인 로직과 분리한다.
        publisher.publishEvent(new ManagtrAddEvent(this, updatedDto.getAttachableKey()));
        // 잔디 메시지 발송은 메인 로직과 분리한다.
        // if ("Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyJournalThreadReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto 삭제할 객체
     */
    @Override
    public void preDelete(final JournalThreadDto deletedDto) throws Exception {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
    }
}
