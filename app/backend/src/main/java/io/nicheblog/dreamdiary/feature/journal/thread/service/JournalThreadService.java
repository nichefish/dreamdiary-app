package io.nicheblog.dreamdiary.feature.journal.thread.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable.managt.event.ManagtrAddEvent;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.mapstruct.JournalThreadMapstruct;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadCandidateDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadCandidateProjection;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadRepository;
import io.nicheblog.dreamdiary.feature.journal.thread.spec.JournalThreadSpec;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
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

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.Objects;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private final JournalEntryService journalEntryService;
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
     * 서버 우선순위를 사용한다. 검색어와 분류는 후보 집합을 먼저 좁힌 뒤 같은 순위를 적용한다.
     *
     * @param entryId 후보를 요청한 엔트리 ID
     * @param keyword 제목 검색어
     * @param categoryCode 분류 코드
     * @param limit 최대 후보 수
     * @return 경량 스레드 후보 목록
     */
    @Transactional(readOnly = true)
    public List<JournalThreadCandidateDto> getCandidates(
            final Integer entryId,
            final String keyword,
            final String categoryCode,
            final Integer limit
    ) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final String resolvedKeyword = StringUtils.trimToEmpty(keyword);
        final String resolvedCategoryCode = StringUtils.trimToEmpty(categoryCode);
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
                resolvedCategoryCode,
                PageRequest.of(0, resolvedLimit)
        );
        log.debug("[JournalThread.candidates] 후보 조회. entryId={}, keyword={}, categoryCode={}, limit={}, size={}, username={}",
                entryId, resolvedKeyword, resolvedCategoryCode, resolvedLimit, candidates.size(), username);
        return candidates.stream()
                .map(candidate -> JournalThreadCandidateDto.builder()
                        .id(candidate.getId())
                        .title(candidate.getTitle())
                        .categoryCode(candidate.getCategoryCode())
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
     * 페이징 목록 조회 후 소속 엔트리 태그 집계를 붙인다.
     * <p>
     * 변경 전: 목록 DTO 의 {@code tag} 가 비어 목록 UI 의 태그 영역이 렌더되지 않았다.
     * 상세({@link #viewDetailPage}) 만 {@link #applyEntryTagSummary} 를 호출했기 때문이다.
     * 변경 후: 목록도 동일 계약(소속 엔트리 태그 합집합, tagId 중복 제거)을 일괄 적용한다.
     * </p>
     *
     * @param searchParamMap 검색 조건
     * @param pageable 페이징
     * @return 태그 집계가 채워진 페이징 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public Page<JournalThreadDto> getPageDto(final Map<String, Object> searchParamMap, final Pageable pageable) throws Exception {
        final Page<JournalThreadDto> page = BaseAttachableService.super.getPageDto(searchParamMap, pageable);
        this.applyEntryTagSummaries(page.getContent());
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
     * 상세 페이지 조회 전처리. (dto level)
     *
     * @param key 조회할 DTO 식별자
     */
    @Transactional
    public JournalThreadDto viewDetailPage(final Integer key) throws Exception {
        final JournalThreadDto dto = this.getDtlDto(key);
        this.applyEntryTagSummary(dto);
        return dto;
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
     * 여러 스레드에 소속 엔트리 태그 집계를 일괄 적용한다. (목록 N+1 방지)
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

        final Map<Integer, List<Integer>> entryIdsByThread = journalThreadEntryService.getEntryIdsGroupedByThread(threadIds);
        final Set<Integer> allEntryIds = new HashSet<>();
        for (final List<Integer> entryIds : entryIdsByThread.values()) {
            if (CollectionUtils.isEmpty(entryIds)) continue;
            allEntryIds.addAll(entryIds);
        }

        final Map<Integer, JournalEntryDto> entryById = new LinkedHashMap<>();
        if (!allEntryIds.isEmpty()) {
            for (final JournalEntryDto entry : journalEntryService.getListDtoByIds(allEntryIds)) {
                if (entry == null || entry.getId() == null) continue;
                entryById.put(entry.getId(), entry);
            }
        }

        for (final JournalThreadDto dto : dtoList) {
            if (dto == null || dto.getId() == null) continue;
            final List<Integer> entryIds = entryIdsByThread.getOrDefault(dto.getId(), List.of());
            final Map<Integer, TagContentDto> tagMap = new LinkedHashMap<>();
            for (final Integer entryId : entryIds) {
                final JournalEntryDto entry = entryById.get(entryId);
                if (entry == null) continue;
                final TagCmpstn entryTag = entry.getTag();
                final List<TagContentDto> tagList = (entryTag == null) ? null : entryTag.getList();
                if (CollectionUtils.isEmpty(tagList)) continue;
                for (final TagContentDto tag : tagList) {
                    if (tag == null || tag.getTagId() == null) continue;
                    tagMap.putIfAbsent(tag.getTagId(), tag);
                }
            }
            if (dto.getTag() == null) dto.setTag(new TagCmpstn());
            dto.getTag().setList(new ArrayList<>(tagMap.values()));
        }
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
