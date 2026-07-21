package io.nicheblog.dreamdiary.feature.journal.thread.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntrySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadSmpEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadEntryDto;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadRepository;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    /**
     * 엔트리를 스레드에 소속시킨다.
     * <p>
     * 멱등하다. 이미 살아있는 소속이면 아무것도 바꾸지 않고 성공으로 응답하고,
     * 해제된(소프트 삭제) 소속이면 되살린다.
     * 스레드와 대상 엔트리가 모두 현재 사용자 소유인지 확인한 뒤에만 등록·복원한다.
     *
     * @param threadId 스레드 ID
     * @param entryId 엔트리 ID
     * @param sortOrder 스레드 내 표시 순서 (null 허용 — 엔트리 일자순으로 정렬)
     * @return {@link ServiceResponse} -- 처리 결과
     */
    @Transactional
    public ServiceResponse regist(final Integer threadId, final Integer entryId, final Integer sortOrder) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        this.requireOwnedThread(threadId, username);
        this.requireOwnedEntry(entryId, username);

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
     * 스레드 상세에서 소속 엔트리를 저널 일자와 동일한 카드로 보여주기 위한 계약이다.
     * 소속 메타(JournalThreadEntryDto)가 아니라 실제 엔트리(JournalEntryDto)를 일자 오름차순으로 돌려준다.
     * 정렬 근거: 흐름의 선후는 엔트리 일자에서 파생한다({@link JournalEntryService#getListDtoByIds}).
     * 같은 일자 안에서는 엔트리 ID 오름차순을 tiebreak 로 써서 매 조회마다 순서가 뒤바뀌지 않게 고정한다.
     *
     * @param threadId 스레드 ID
     * @return 소속 엔트리 DTO 목록 (일자 오름차순, 동일 일자는 ID 오름차순)
     */
    @Transactional(readOnly = true)
    public List<JournalEntryDto> getEntriesByThread(final Integer threadId) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        this.requireOwnedThread(threadId, username);
        final List<Integer> entryIds = repository.findAllByThread(threadId, username).stream()
                .map(JournalThreadEntryEntity::getEntryId)
                .collect(Collectors.toList());
        final List<JournalEntryDto> entries = journalEntryService.getListDtoByIds(entryIds);
        // 같은 일자 내 순서는 sort_order 가 비어 있어(findAllById 의 IN 순서에 의존) 비결정적이다.
        // 일자 오름차순은 유지하되, 동일 일자는 엔트리 ID 오름차순으로 고정해 매 조회마다 순서가 뒤바뀌지 않게 한다.
        entries.sort(Comparator
                .comparing(JournalEntryDto::getStdrdDt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(JournalEntryDto::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        return entries;
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
    private void requireOwnedEntry(final Integer entryId, final String username) throws Exception {
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
}
