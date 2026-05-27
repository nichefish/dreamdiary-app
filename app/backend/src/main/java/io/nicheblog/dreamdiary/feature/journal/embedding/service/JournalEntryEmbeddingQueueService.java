package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingStatsDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingSyncResultDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa.JournalEntryEmbeddingRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 저널 엔트리 임베딩 작업 큐의 상태 전이를 담당하는 서비스입니다.
 *
 * <p>대기 작업 선점, 오래된 처리 중 작업 재대기, 성공/실패/스킵 마킹,
 * 관리자 진행률 집계를 한곳에서 처리한다.</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEmbeddingQueueService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_EMBEDDED = "EMBEDDED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_SKIPPED = "SKIPPED";

    private static final int MIN_BATCH_SIZE = 1;
    private static final int MAX_BATCH_SIZE = 100;
    private static final int ERROR_MESSAGE_LIMIT = 4000;

    @Getter
    private final JournalEntryEmbeddingRepository repository;
    private final JournalEntryEmbeddingSearchService searchService;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalChapterRepository journalChapterRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private enum SyncAction {
        CREATED,
        REQUEUED,
        UNCHANGED,
        SKIPPED
    }

    /**
     * 원본 저널 엔트리 ID를 기준으로 임베딩 작업을 생성하거나 갱신한다.
     *
     * @param journalEntryId 원본 저널 엔트리 ID
     * @throws Exception 임베딩 작업 구성 중 예외가 발생한 경우
     */
    @Transactional
    public void queueForEntryId(final Integer journalEntryId) throws Exception {
        if (journalEntryId == null) return;

        final JournalEntryEntity entry = journalEntryRepository.findById(journalEntryId).orElse(null);
        if (entry == null) {
            removeByJournalEntryId(journalEntryId);
            return;
        }

        queueForEntry(entry);
    }

    /**
     * 원본 저널 엔트리 내용을 기반으로 임베딩 작업 row를 upsert한다.
     *
     * <p>본문 해시가 같고 이미 벡터가 있으면 벡터를 재생성하지 않고 메타데이터만 최신화한다.
     * 본문이 바뀌었거나 아직 완료되지 않은 작업이면 {@code PENDING} 상태로 되돌린다.</p>
     *
     * @param entry 원본 저널 엔트리 엔티티
     * @throws Exception payload JSON 또는 해시 생성 중 예외가 발생한 경우
     */
    private SyncAction queueForEntry(final JournalEntryEntity entry) throws Exception {
        if (entry == null || entry.getId() == null) return SyncAction.UNCHANGED;

        final JournalChapterEntity chapter = entry.getJournalChapterId() == null
                ? null
                : journalChapterRepository.findById(entry.getJournalChapterId()).orElse(null);
        final JournalDaySmpEntity journalDay = chapter == null ? null : chapter.getJournalDay();
        final String contentKind = resolveContentKind(entry, chapter);
        final BigDecimal retrievalWeight = resolveRetrievalWeight(contentKind);
        final String embeddingText = buildEmbeddingText(entry, chapter, journalDay, contentKind);
        final String contentHash = sha256Hex(embeddingText);
        final String payloadJson = objectMapper.writeValueAsString(buildPayload(entry, chapter, journalDay, contentKind, retrievalWeight));
        final boolean hasEmbeddableContent = hasEmbeddableContent(entry, chapter);

        final Optional<JournalEntryEmbeddingEntity> existing = repository.findFirstByJournalEntryId(entry.getId());
        final boolean exists = existing.isPresent();
        final JournalEntryEmbeddingEntity entity = existing.orElseGet(() -> JournalEntryEmbeddingEntity.builder()
                .journalEntryId(entry.getId())
                .build());
        final boolean hasReusableVector = Objects.equals(entity.getContentHash(), contentHash)
                && STATUS_EMBEDDED.equals(entity.getEmbeddingStatus())
                && StringUtils.isNotBlank(entity.getEmbeddingVectorJson());

        entity.setJournalEntryId(entry.getId());
        entity.setContentType(entry.getContentType());
        entity.setContentKind(contentKind);
        entity.setJournalDate(journalDay == null ? null : journalDay.getJournalDate());
        entity.setJournalDatePrecision(journalDay == null || journalDay.getJournalDatePrecision() == null ? null : journalDay.getJournalDatePrecision().name());
        entity.setRetrievalWeight(retrievalWeight);
        entity.setEmbeddingText(embeddingText);
        entity.setEmbeddingPayloadJson(payloadJson);
        entity.setContentHash(contentHash);

        if (!hasEmbeddableContent) {
            entity.setEmbeddingStatus(STATUS_SKIPPED);
            entity.setEmbeddingModel(null);
            entity.setEmbeddingVectorJson(null);
            entity.setEmbeddedAt(null);
            entity.setErrorMessage("no embeddable title, content, tag, chapter, or dreamer context");
            repository.saveAndFlush(entity);
            return SyncAction.SKIPPED;
        } else if (!hasReusableVector) {
            entity.setEmbeddingStatus(STATUS_PENDING);
            entity.setEmbeddingModel(null);
            entity.setEmbeddingVectorJson(null);
            entity.setEmbeddedAt(null);
            entity.setErrorMessage(null);
            repository.saveAndFlush(entity);
            return exists ? SyncAction.REQUEUED : SyncAction.CREATED;
        } else {
            entity.setErrorMessage(null);
            repository.saveAndFlush(entity);
            return SyncAction.UNCHANGED;
        }
    }

    /**
     * 원본 저널 엔트리가 삭제되었을 때 활성 임베딩 작업을 검색 대상에서 제거한다.
     *
     * @param journalEntryId 원본 저널 엔트리 ID
     */
    @Transactional
    public void removeByJournalEntryId(final Integer journalEntryId) {
        if (journalEntryId == null) return;

        repository.findFirstByJournalEntryId(journalEntryId)
                .ifPresent(repository::delete);
        searchService.removeEntry(journalEntryId);
    }

    /**
     * 현재 활성 저널 엔트리를 기준으로 임베딩 작업 테이블을 재동기화한다.
     *
     * <p>누락된 임베딩 작업은 생성하고, 원본이 사라진 활성 임베딩 작업은 제거하며,
     * 본문 해시가 달라진 작업은 다시 {@code PENDING} 상태로 돌린다.</p>
     *
     * @return 동기화 처리 결과
     * @throws Exception 임베딩 작업 구성 중 예외가 발생한 경우
     */
    @Transactional
    public JournalEntryEmbeddingSyncResultDto syncWithJournalEntries() throws Exception {
        final List<JournalEntryEntity> entryList = journalEntryRepository.findAll();
        final List<JournalEntryEmbeddingEntity> embeddingListBefore = repository.findAll();
        final Set<Integer> activeEntryIdSet = entryList.stream()
                .map(JournalEntryEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        long removed = 0L;
        for (final JournalEntryEmbeddingEntity embedding : embeddingListBefore) {
            if (!activeEntryIdSet.contains(embedding.getJournalEntryId())) {
                repository.delete(embedding);
                removed++;
            }
        }
        repository.flush();

        long created = 0L;
        long requeued = 0L;
        long unchanged = 0L;
        long skipped = 0L;
        for (final JournalEntryEntity entry : entryList) {
            final SyncAction action = queueForEntry(entry);
            switch (action) {
                case CREATED -> created++;
                case REQUEUED -> requeued++;
                case SKIPPED -> skipped++;
                case UNCHANGED -> unchanged++;
                default -> unchanged++;
            }
        }

        return JournalEntryEmbeddingSyncResultDto.builder()
                .activeEntryCount(entryList.size())
                .activeEmbeddingCountBefore(embeddingListBefore.size())
                .created(created)
                .requeued(requeued)
                .unchanged(unchanged)
                .skipped(skipped)
                .removed(removed)
                .activeEmbeddingCountAfter(repository.count())
                .build();
    }

    /**
     * 대기 중인 작업을 배치 크기만큼 선점하고 처리 중 상태로 변경한다.
     *
     * @param batchSize 선점할 최대 작업 개수
     * @return 처리 중 상태로 변경된 임베딩 작업 목록
     */
    @Transactional
    public List<JournalEntryEmbeddingEntity> claimPendingBatch(final Integer batchSize) {
        final int normalizedBatchSize = normalizeBatchSize(batchSize);
        final List<JournalEntryEmbeddingEntity> entityList = repository.findAndLockPendingBatch(normalizedBatchSize);

        entityList.forEach(entity -> {
            entity.setEmbeddingStatus(STATUS_PROCESSING);
            entity.setErrorMessage(null);
        });
        repository.saveAll(entityList);
        repository.flush();

        return entityList;
    }

    /**
     * 일정 시간 이상 처리 중 상태에 머문 작업을 다시 대기 상태로 되돌린다.
     *
     * @param staleBefore 오래된 처리 중 작업으로 판단할 기준 시각
     * @param batchSize 한 번에 재대기시킬 최대 작업 개수
     * @return 재대기 처리한 작업 건수
     */
    @Transactional
    public int requeueStaleProcessing(final Date staleBefore, final Integer batchSize) {
        final int normalizedBatchSize = normalizeBatchSize(batchSize);
        final List<JournalEntryEmbeddingEntity> entityList =
                repository.findAllByEmbeddingStatusAndUpdatedAtBeforeOrderByUpdatedAtAscIdAsc(
                        STATUS_PROCESSING,
                        staleBefore,
                        PageRequest.of(0, normalizedBatchSize)
                );

        entityList.forEach(entity -> {
            entity.setEmbeddingStatus(STATUS_PENDING);
            entity.setErrorMessage("Requeued stale PROCESSING row.");
        });
        repository.saveAll(entityList);

        if (!entityList.isEmpty()) {
            log.info("Requeued {} stale journal entry embedding rows.", entityList.size());
        }

        return entityList.size();
    }

    /**
     * 임베딩 벡터 생성에 성공한 작업을 완료 상태로 마킹한다.
     *
     * @param id 임베딩 작업 ID
     * @param expectedContentHash 워커가 벡터화한 시점의 본문 해시
     * @param embeddingModel 벡터 생성에 사용한 모델명
     * @param embeddingVectorJson 생성된 벡터 JSON 배열 문자열
     */
    @Transactional
    public void markEmbedded(final Integer id, final String expectedContentHash, final String embeddingModel, final String embeddingVectorJson) {
        repository.findById(id).ifPresent(entity -> {
            if (!Objects.equals(entity.getContentHash(), expectedContentHash)) {
                log.info("Skip stale embedding result. id={}, expectedHash={}, currentHash={}", id, expectedContentHash, entity.getContentHash());
                return;
            }
            entity.setEmbeddingStatus(STATUS_EMBEDDED);
            entity.setEmbeddingModel(embeddingModel);
            entity.setEmbeddingVectorJson(embeddingVectorJson);
            entity.setEmbeddedAt(new Date());
            entity.setErrorMessage(null);
        });
    }

    /**
     * 처리 중 예외가 발생한 임베딩 작업을 실패 상태로 마킹한다.
     *
     * @param id 임베딩 작업 ID
     * @param exception 실패 원인이 된 예외
     */
    @Transactional
    public void markFailed(final Integer id, final Exception exception) {
        repository.findById(id).ifPresent(entity -> {
            entity.setEmbeddingStatus(STATUS_FAILED);
            entity.setErrorMessage(toErrorMessage(exception));
        });
    }

    /**
     * 지정한 작업들을 다시 대기 상태로 되돌린다.
     *
     * @param idList 재대기시킬 임베딩 작업 ID 목록
     * @param reason 재대기 사유
     */
    @Transactional
    public void requeueByIds(final List<Integer> idList, final String reason) {
        if (idList == null || idList.isEmpty()) return;

        final List<JournalEntryEmbeddingEntity> entityList = repository.findAllById(idList);
        entityList.forEach(entity -> {
            entity.setEmbeddingStatus(STATUS_PENDING);
            entity.setErrorMessage(StringUtils.abbreviate(reason, ERROR_MESSAGE_LIMIT));
        });
    }

    /**
     * 임베딩할 수 없는 작업을 스킵 상태로 마킹한다.
     *
     * @param id 임베딩 작업 ID
     * @param reason 스킵 사유
     */
    @Transactional
    public void markSkipped(final Integer id, final String reason) {
        repository.findById(id).ifPresent(entity -> {
            entity.setEmbeddingStatus(STATUS_SKIPPED);
            entity.setErrorMessage(StringUtils.abbreviate(reason, ERROR_MESSAGE_LIMIT));
        });
    }

    /**
     * 대기 중인 임베딩 작업 건수를 조회한다.
     *
     * @return 대기 중인 작업 건수
     */
    @Transactional(readOnly = true)
    public long countPending() {
        return repository.countByEmbeddingStatus(STATUS_PENDING);
    }

    /**
     * 관리자 화면에 표시할 임베딩 작업 통계를 집계한다.
     *
     * @return 임베딩 작업 상태별 건수와 진행률 DTO
     */
    @Transactional(readOnly = true)
    public JournalEntryEmbeddingStatsDto getStats() {
        final long total = repository.count();
        final long pending = repository.countByEmbeddingStatus(STATUS_PENDING);
        final long processing = repository.countByEmbeddingStatus(STATUS_PROCESSING);
        final long embedded = repository.countByEmbeddingStatus(STATUS_EMBEDDED);
        final long failed = repository.countByEmbeddingStatus(STATUS_FAILED);
        final long skipped = repository.countByEmbeddingStatus(STATUS_SKIPPED);
        final long remaining = pending + processing;
        final long completed = embedded + failed + skipped;

        return JournalEntryEmbeddingStatsDto.builder()
                .total(total)
                .pending(pending)
                .processing(processing)
                .embedded(embedded)
                .failed(failed)
                .skipped(skipped)
                .remaining(remaining)
                .completed(completed)
                .completionRate(toPercent(completed, total))
                .vectorizedRate(toPercent(embedded, total))
                .build();
    }

    /**
     * 배치 크기를 허용 범위 안으로 보정한다.
     *
     * @param batchSize 요청된 배치 크기
     * @return 보정된 배치 크기
     */
    private int normalizeBatchSize(final Integer batchSize) {
        if (batchSize == null) return 20;
        return Math.max(MIN_BATCH_SIZE, Math.min(MAX_BATCH_SIZE, batchSize));
    }

    /**
     * 예외 메시지를 DB에 저장 가능한 길이로 축약한다.
     *
     * @param exception 실패 원인이 된 예외
     * @return 축약된 예외 메시지
     */
    private String toErrorMessage(final Exception exception) {
        if (exception == null) return null;
        final String message = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        return StringUtils.abbreviate(message, ERROR_MESSAGE_LIMIT);
    }

    /**
     * 부분 건수를 전체 건수 기준 백분율로 변환한다.
     *
     * @param count 부분 건수
     * @param total 전체 건수
     * @return 소수점 둘째 자리까지 반올림한 백분율
     */
    private double toPercent(final long count, final long total) {
        if (total <= 0) return 0.0D;
        return Math.round(((double) count * 10000.0D) / (double) total) / 100.0D;
    }

    /**
     * 컨텐츠 타입과 챕터 타입을 기준으로 검색 가중치 분류를 결정한다.
     *
     * @param entry 원본 저널 엔트리
     * @param chapter 소속 저널 챕터
     * @return 검색 가중치 분류
     */
    private String resolveContentKind(final JournalEntryEntity entry, final JournalChapterEntity chapter) {
        if (chapter != null && chapter.getChapterType() == ChapterType.NOTE) return "NOTE";
        if (ContentType.JOURNAL_DREAM.key.equals(entry.getContentType())) return "DREAM";
        if (ContentType.JOURNAL_DIARY.key.equals(entry.getContentType())) return "DIARY";
        if (ContentType.JOURNAL_NOTE.key.equals(entry.getContentType())) return "NOTE";
        return "UNKNOWN";
    }

    /**
     * 검색 가중치 분류별 기본 랭킹 가중치를 반환한다.
     *
     * @param contentKind 검색 가중치 분류
     * @return 검색 랭킹 가중치
     */
    private BigDecimal resolveRetrievalWeight(final String contentKind) {
        return switch (StringUtils.defaultString(contentKind)) {
            case "DREAM" -> new BigDecimal("1.30");
            case "NOTE" -> new BigDecimal("0.85");
            case "DIARY" -> new BigDecimal("1.00");
            default -> new BigDecimal("1.00");
        };
    }

    /**
     * 임베딩 모델에 전달할 정규화 텍스트를 구성한다.
     *
     * @param entry 원본 저널 엔트리
     * @param journalDay 소속 저널 일자
     * @param contentKind 검색 가중치 분류
     * @return 임베딩 입력 텍스트
     */
    private String buildEmbeddingText(
            final JournalEntryEntity entry,
            final JournalChapterEntity chapter,
            final JournalDaySmpEntity journalDay,
            final String contentKind
    ) {
        final StringBuilder builder = new StringBuilder();
        final String tagSummary = buildTagSummary(entry);
        appendLine(builder, "유형", contentKind);
        appendLine(builder, "날짜", formatDate(journalDay == null ? null : journalDay.getJournalDate()));
        appendLine(builder, "챕터", chapter == null ? null : chapter.getTitle());
        appendLine(builder, "챕터 분류", chapter == null ? null : StringUtils.firstNonBlank(chapter.getCategoryName(), chapter.getCategoryCode()));
        appendLine(builder, "핵심 태그", tagSummary);
        appendLine(builder, "주제 태그", tagSummary);
        appendLine(builder, "태그", tagSummary);
        appendLine(builder, "제목", entry.getTitle());
        appendLine(builder, "본문", entry.getContent());
        appendLine(builder, "타인의 꿈 여부", entry.getElseDreamYn());
        appendLine(builder, "꿈 제공자", entry.getElseDreamerNm());
        return StringUtils.trim(builder.toString());
    }

    /**
     * 실제 벡터화할 만한 제목 또는 본문이 있는지 확인한다.
     *
     * @param entry 원본 저널 엔트리
     * @return 벡터화 가능한 본문 존재 여부
     */
    private boolean hasEmbeddableContent(final JournalEntryEntity entry, final JournalChapterEntity chapter) {
        return StringUtils.isNotBlank(entry.getTitle())
                || StringUtils.isNotBlank(entry.getContent())
                || StringUtils.isNotBlank(entry.getElseDreamerNm())
                || (chapter != null && StringUtils.isNotBlank(chapter.getTitle()))
                || StringUtils.isNotBlank(buildTagSummary(entry));
    }

    /**
     * 검색/디버깅에 사용할 구조화 payload를 구성한다.
     *
     * @param entry 원본 저널 엔트리
     * @param chapter 소속 저널 챕터
     * @param journalDay 소속 저널 일자
     * @param contentKind 검색 가중치 분류
     * @param retrievalWeight 검색 랭킹 가중치
     * @return JSON 직렬화 대상 payload
     */
    private Map<String, Object> buildPayload(
            final JournalEntryEntity entry,
            final JournalChapterEntity chapter,
            final JournalDaySmpEntity journalDay,
            final String contentKind,
            final BigDecimal retrievalWeight
    ) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("source", "journal_entry");
        payload.put("journalEntryId", entry.getId());
        payload.put("contentType", entry.getContentType());
        payload.put("contentKind", contentKind);
        payload.put("retrievalWeight", retrievalWeight);
        payload.put("title", entry.getTitle());
        payload.put("journalChapterId", entry.getJournalChapterId());
        payload.put("journalChapterTitle", chapter == null ? null : chapter.getTitle());
        payload.put("journalChapterType", chapter == null || chapter.getChapterType() == null ? null : chapter.getChapterType().name());
        payload.put("journalChapterCategoryCode", chapter == null ? null : chapter.getCategoryCode());
        payload.put("journalChapterCategoryName", chapter == null ? null : chapter.getCategoryName());
        payload.put("journalDayId", chapter == null ? null : chapter.getJournalDayId());
        payload.put("journalDate", formatDate(journalDay == null ? null : journalDay.getJournalDate()));
        payload.put("journalDatePrecision", journalDay == null || journalDay.getJournalDatePrecision() == null ? null : journalDay.getJournalDatePrecision().name());
        payload.put("yy", journalDay == null ? null : journalDay.getYy());
        payload.put("mnth", journalDay == null ? null : journalDay.getMnth());
        payload.put("sortOrder", entry.getSortOrder());
        payload.put("elseDreamYn", entry.getElseDreamYn());
        payload.put("elseDreamerNm", entry.getElseDreamerNm());
        payload.put("tags", buildTagSummary(entry));
        return payload;
    }

    /**
     * 태그 이름과 태그 카테고리를 임베딩 텍스트에 넣기 좋은 문자열로 구성한다.
     */
    private String buildTagSummary(final JournalEntryEntity entry) {
        if (entry == null || entry.getTag() == null || CollectionUtils.isEmpty(entry.getTag().getList())) {
            return null;
        }
        return entry.getTag().getList().stream()
                .map(this::formatTag)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.joining(" "));
    }

    /**
     * 태그를 [카테고리]#태그 형식으로 변환한다.
     */
    private String formatTag(final TagContentEntity tagContent) {
        if (tagContent == null || tagContent.getTag() == null || StringUtils.isBlank(tagContent.getTag().getName())) {
            return null;
        }
        final String tagName = StringUtils.trim(tagContent.getTag().getName());
        final String category = StringUtils.trimToNull(tagContent.getTag().getCtgr());
        return category == null ? "#" + tagName : "[" + category + "]#" + tagName;
    }

    /**
     * 값이 비어 있지 않을 때만 라벨과 값을 한 줄로 추가한다.
     *
     * @param builder 텍스트 빌더
     * @param label 라벨
     * @param value 값
     */
    private void appendLine(final StringBuilder builder, final String label, final String value) {
        if (StringUtils.isBlank(value)) return;
        builder.append(label).append(": ").append(StringUtils.trim(value)).append('\n');
    }

    /**
     * 날짜 값을 임베딩 payload용 문자열로 변환한다.
     *
     * @param date 날짜 값
     * @return 날짜 문자열, 변환할 수 없으면 {@code null}
     */
    private String formatDate(final Date date) {
        try {
            return DateUtils.asStr(date, DatePtn.DATE);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * 임베딩 입력 텍스트의 SHA-256 해시를 계산한다.
     *
     * @param text 해시를 계산할 텍스트
     * @return 16진수 SHA-256 해시
     * @throws Exception 해시 알고리즘을 사용할 수 없는 경우
     */
    private String sha256Hex(final String text) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[] hash = digest.digest(StringUtils.defaultString(text).getBytes(StandardCharsets.UTF_8));
        final StringBuilder builder = new StringBuilder(hash.length * 2);
        for (final byte b : hash) {
            builder.append(String.format("%02x", b & 0xff));
        }
        return builder.toString();
    }
}
