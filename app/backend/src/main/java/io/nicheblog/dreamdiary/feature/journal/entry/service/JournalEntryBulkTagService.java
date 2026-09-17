package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.repository.jpa.TagContentRepository;
import io.nicheblog.dreamdiary.feature.journal._shared.security.JournalContentOwnershipGuard;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayResolvedGuard;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryBulkTagReqDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryBulkTagResDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryBulkTagUndoReqDto;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingQueueService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntryEntityQueueService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.my.JournalEntryMyTagService;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 저널 엔트리 일괄 태그 서비스.
 * <pre>
 *  검색 결과에서 선택한 엔트리들에 기존 태그 연결을 일괄 추가(ADD)·제거(REMOVE)한다.
 *  전체 요청을 한 트랜잭션으로 처리한다(전체 성공/전체 실패). 검증은 쓰기 전에 전량 수행하며,
 *  하나라도 실패하면 어떤 연결도 변경하지 않는다.
 *  태그 연결 추가는 tag_content 멱등 계약(uk_tag_content_pair, soft 삭제분 복원)을 따른다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryBulkTagService {

    /** 한 요청의 엔트리 선택 상한. */
    private static final int MAX_ENTRY_IDS = 200;
    /** 한 요청의 태그 선택 상한. */
    private static final int MAX_TAG_IDS = 50;

    private final JournalEntryRepository journalEntryRepository;
    private final TagContentRepository tagContentRepository;
    private final JournalContentOwnershipGuard ownershipGuard;
    private final JournalDayResolvedGuard resolvedGuard;
    private final JournalEntryMyTagService journalEntryMyTagService;
    private final JournalEntryEntityQueueService journalEntryEntityQueueService;
    private final JournalEntryEmbeddingQueueService journalEntryEmbeddingQueueService;

    /**
     * 선택 엔트리들에 태그를 일괄 추가·제거한다.
     *
     * @param req 작업 종류·콘텐츠 타입·엔트리/태그 ID 목록
     * @return 실제 변경된 연결 쌍과 집계
     * @throws Exception 검증 실패(빈 선택·상한 초과·미존재·소유권·완결·태그 미소속) 또는 처리 예외
     */
    @Transactional
    public JournalEntryBulkTagResDto bulkTag(final JournalEntryBulkTagReqDto req) throws Exception {
        final String operation = req.getOperation();
        if (!"ADD".equals(operation) && !"REMOVE".equals(operation)) {
            throw new BusinessException("journal.entry.bulk-tag.invalid-operation");
        }
        final ContentType contentType = resolveContentType(req.getContentType());
        final List<Integer> entryIds = distinct(req.getEntryIds());
        final List<Integer> tagIds = distinct(req.getTagIds());

        validate(entryIds, tagIds, contentType);

        final String createdBy = AuthUtils.requireLoginUsername();
        final List<JournalEntryBulkTagResDto.ChangedPair> changed = new ArrayList<>();
        final Set<Integer> affectedEntries = new HashSet<>();
        int unchanged = 0;

        for (final Integer entryId : entryIds) {
            for (final Integer tagId : tagIds) {
                final boolean didChange = "ADD".equals(operation)
                        ? applyAdd(entryId, tagId, contentType, createdBy)
                        : applyRemove(entryId, tagId, contentType, createdBy);
                if (didChange) {
                    changed.add(JournalEntryBulkTagResDto.ChangedPair.builder()
                            .entryId(entryId).tagId(tagId).build());
                    affectedEntries.add(entryId);
                } else {
                    unchanged++;
                }
            }
        }

        evictAfterBulk(affectedEntries, contentType, createdBy);
        log.info("[JournalEntryBulkTag] op={}, contentType={}, entries={}, tags={}, changed={}, unchanged={}",
                operation, contentType.key, entryIds.size(), tagIds.size(), changed.size(), unchanged);

        return JournalEntryBulkTagResDto.builder()
                .operation(operation)
                .requestedEntryCount(entryIds.size())
                .requestedTagCount(tagIds.size())
                .changedLinkCount(changed.size())
                .unchangedLinkCount(unchanged)
                .affectedEntryCount(affectedEntries.size())
                .changedPairs(changed)
                .build();
    }

    /** ADD: 소프트 삭제분 복원, 없으면 신규 저장, 이미 활성이면 no-op. 실제 변경 시 true. */
    private boolean applyAdd(final Integer entryId, final Integer tagId, final ContentType ct, final String createdBy) {
        final Optional<TagContentEntity> existing =
                tagContentRepository.findAnyByPair(tagId, entryId, ct.key, createdBy);
        if (existing.isPresent()) {
            final TagContentEntity row = existing.get();
            if (row.getDeletedAt() != null) {
                tagContentRepository.reviveById(row.getId());
                return true;
            }
            return false;
        }
        tagContentRepository.save(new TagContentEntity(tagId, new BaseAttachableKey(entryId, ct)));
        return true;
    }

    /** REMOVE: 활성 연결을 소프트 삭제한다. 실제 제거 시 true. */
    private boolean applyRemove(final Integer entryId, final Integer tagId, final ContentType ct, final String createdBy) {
        final Optional<TagContentEntity> existing =
                tagContentRepository.findAnyByPair(tagId, entryId, ct.key, createdBy);
        if (existing.isPresent() && existing.get().getDeletedAt() == null) {
            tagContentRepository.delete(existing.get());
            return true;
        }
        return false;
    }

    /**
     * 쓰기 전 전량 검증. 하나라도 실패하면 예외를 던져 전체를 롤백한다.
     * 소유권 실패 로그에는 엔트리 ID와 콘텐츠 타입만 남기고 제목·본문·태그명은 기록하지 않는다.
     */
    private void validate(final List<Integer> entryIds, final List<Integer> tagIds, final ContentType ct) throws Exception {
        if (entryIds.isEmpty() || tagIds.isEmpty()) {
            throw new BusinessException("journal.entry.bulk-tag.empty-selection");
        }
        if (entryIds.size() > MAX_ENTRY_IDS || tagIds.size() > MAX_TAG_IDS) {
            throw new BusinessException("journal.entry.bulk-tag.too-many");
        }
        validateEntries(entryIds, ct);
        validateTags(tagIds, ct);
    }

    /** 각 엔트리의 존재·타입 일치·소유권·완결 축 쓰기 정책을 검증한다. */
    private void validateEntries(final List<Integer> entryIds, final ContentType ct) throws Exception {
        for (final Integer entryId : entryIds) {
            journalEntryRepository.findByIdAndContentType(entryId, ct.key)
                    .orElseThrow(() -> new BusinessException("journal.entry.bulk-tag.entry-not-found"));
            ownershipGuard.assertOwned(entryId, ct);
            resolvedGuard.assertWritableForRef(entryId, ct);
        }
    }

    /** 모든 태그가 현재 사용자의 요청 콘텐츠 타입 태그 목록에 속하는지 검증한다. */
    private void validateTags(final List<Integer> tagIds, final ContentType ct) throws Exception {
        final Set<Integer> myTagIds = journalEntryMyTagService.getMyTagList(ct).stream()
                .map(TagDto::getId)
                .collect(Collectors.toSet());
        for (final Integer tagId : tagIds) {
            if (!myTagIds.contains(tagId)) {
                throw new BusinessException("journal.entry.bulk-tag.tag-not-owned");
            }
        }
    }

    /**
     * 일괄 태그 변경 후처리. 문서 §6대로 단건 반복이 아니라 일괄 집계로 무효화한다.
     * 엔트리 태그 축 캐시(목록·기간·카테고리·건수)는 사용자 범위로 한 번만 비우고,
     * 상세·태그 연결 캐시는 변경 엔트리별로 비운 뒤, 변경 엔트리만 entity/embedding 큐에
     * 다시 넣는다(ai_enabled 게이트는 큐 서비스 진입점이 적용한다).
     */
    private void evictAfterBulk(final Set<Integer> affectedEntries, final ContentType ct, final String username) throws Exception {
        if (affectedEntries.isEmpty()) return;
        EhCacheUtils.clearUserCache("journalEntryTagListByUser", username);
        EhCacheUtils.clearUserCache("journalEntryPeriodTagListByUser", username);
        EhCacheUtils.clearUserCache("journalEntryTagCategoryMapByUser", username);
        EhCacheUtils.clearUserCache("journalEntryTagCountMapByUser", username);
        for (final Integer entryId : affectedEntries) {
            EhCacheUtils.evictUserCacheByKey(JournalEntryService.DTL_CACHE_NAME, username, ct.key + "_" + entryId);
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", entryId + "_" + ct.key);
            journalEntryEntityQueueService.queueForEntryId(entryId);
            journalEntryEmbeddingQueueService.queueForEntryId(entryId);
        }
    }

    /** 요청 콘텐츠 타입 문자열을 일기/꿈 엔트리 타입으로 해석한다. */
    private ContentType resolveContentType(final String raw) {
        if (!"JOURNAL_DIARY".equals(raw) && !"JOURNAL_DREAM".equals(raw)) {
            throw new BusinessException("journal.entry.bulk-tag.invalid-content-type");
        }
        return ContentType.valueOf(raw);
    }

    /**
     * 직전 일괄 작업을 되돌린다. 원 ADD 는 pairs 를 제거, 원 REMOVE 는 pairs 를 복원한다.
     * 서버가 응답한 실제 변경 연결 쌍만 역연산하며, 엔트리 소유·완결을 다시 검증한다.
     *
     * @param req 원 작업 종류·콘텐츠 타입·되돌릴 연결 쌍
     * @return 실제 역연산된 연결 쌍과 집계
     * @throws Exception 검증 실패 또는 처리 예외
     */
    @Transactional
    public JournalEntryBulkTagResDto undoBulkTag(final JournalEntryBulkTagUndoReqDto req) throws Exception {
        final String original = req.getOperation();
        if (!"ADD".equals(original) && !"REMOVE".equals(original)) {
            throw new BusinessException("journal.entry.bulk-tag.invalid-operation");
        }
        final ContentType contentType = resolveContentType(req.getContentType());
        final List<JournalEntryBulkTagResDto.ChangedPair> pairs = distinctPairs(req.getPairs());
        if (pairs.isEmpty()) {
            throw new BusinessException("journal.entry.bulk-tag.empty-selection");
        }
        if (pairs.size() > MAX_ENTRY_IDS * MAX_TAG_IDS) {
            throw new BusinessException("journal.entry.bulk-tag.too-many");
        }

        final List<Integer> entryIds = pairs.stream()
                .map(JournalEntryBulkTagResDto.ChangedPair::getEntryId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        validateEntries(entryIds, contentType);

        final String createdBy = AuthUtils.requireLoginUsername();
        final List<JournalEntryBulkTagResDto.ChangedPair> reverted = new ArrayList<>();
        final Set<Integer> affectedEntries = new HashSet<>();
        int unchanged = 0;

        for (final JournalEntryBulkTagResDto.ChangedPair pair : pairs) {
            final Integer entryId = pair.getEntryId();
            final Integer tagId = pair.getTagId();
            if (entryId == null || tagId == null) { unchanged++; continue; }
            final boolean didChange = "ADD".equals(original)
                    ? applyRemove(entryId, tagId, contentType, createdBy)
                    : applyAdd(entryId, tagId, contentType, createdBy);
            if (didChange) {
                reverted.add(pair);
                affectedEntries.add(entryId);
            } else {
                unchanged++;
            }
        }

        evictAfterBulk(affectedEntries, contentType, createdBy);
        log.info("[JournalEntryBulkTag] undo of op={}, contentType={}, pairs={}, reverted={}",
                original, contentType.key, pairs.size(), reverted.size());

        final long tagCount = pairs.stream().map(JournalEntryBulkTagResDto.ChangedPair::getTagId)
                .filter(Objects::nonNull).distinct().count();
        return JournalEntryBulkTagResDto.builder()
                .operation("UNDO_" + original)
                .requestedEntryCount(entryIds.size())
                .requestedTagCount((int) tagCount)
                .changedLinkCount(reverted.size())
                .unchangedLinkCount(unchanged)
                .affectedEntryCount(affectedEntries.size())
                .changedPairs(reverted)
                .build();
    }

    /** null 쌍·중복 (entryId, tagId) 쌍을 제거한다. */
    private List<JournalEntryBulkTagResDto.ChangedPair> distinctPairs(final List<JournalEntryBulkTagResDto.ChangedPair> pairs) {
        if (pairs == null) return List.of();
        final Set<String> seen = new HashSet<>();
        final List<JournalEntryBulkTagResDto.ChangedPair> result = new ArrayList<>();
        for (final JournalEntryBulkTagResDto.ChangedPair p : pairs) {
            if (p == null || p.getEntryId() == null || p.getTagId() == null) continue;
            if (seen.add(p.getEntryId() + ":" + p.getTagId())) result.add(p);
        }
        return result;
    }

    /** null 제거 후 중복을 제거한 ID 목록. */
    private List<Integer> distinct(final List<Integer> ids) {
        if (ids == null) return List.of();
        return ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }
}