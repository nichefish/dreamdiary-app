package io.nicheblog.dreamdiary.feature.journal.chapter.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableManagtHelper;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableProcPostProcessor;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixContentService;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.mapstruct.JournalChapterMapstruct;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterSearchParam;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.mybatis.JournalChapterMapper;
import io.nicheblog.dreamdiary.feature.journal.chapter.spec.JournalChapterSpec;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayService;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayResolvedGuard;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDayResolvedAxis;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JournalChapterService
 * <pre>
 *  저널 챕터 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalChapterService
        implements BaseAttachableService<JournalChapterDto, JournalChapterDto, Integer, JournalChapterEntity> {

    public static final String DETAIL_CACHE_NAME = "journalChapterDetailDtoByUser";

    /** 시스템 요약 챕터 */
    private static final String SUMMARY_YN = "Y";
    /** 일반 챕터 */
    private static final String NON_SUMMARY_YN = "N";

    @Getter
    private final JournalChapterRepository repository;
    @Getter
    private final JournalChapterSpec spec;
    @Getter
    private final JournalChapterMapstruct mapstruct;

    public JournalChapterMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalChapterMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JournalChapterMapper journalChapterMapper;
    private final JournalCacheEvictWorker journalCacheEvictWorker;
    private final JournalDayRepository journalDayRepository;
    private final JournalDayService journalDayService;
    private final JournalEntryService journalEntryService;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalReflectionRepository journalReflectionRepository;
    private final JournalDayResolvedGuard journalDayResolvedGuard;
    private final PrefixContentService prefixContentService;

    private final ApplicationContext context;
    private JournalChapterService getSelf() {
        return context.getBean(this.getClass());
    }

    public List<JournalChapterDto> getListDtoByUser(final String username, final JournalChapterSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 챕터 본문과 개인 Prefix 선택을 같은 트랜잭션에서 등록한다.
     * 시스템 요약·DREAM 챕터는 Prefix를 갖지 않는다.
     */
    @Override
    @Transactional
    public ServiceResponse regist(final JournalChapterDto registDto) throws Exception {
        final ServiceResponse response = BaseAttachableService.super.regist(registDto);
        final JournalChapterDto updatedDto = (JournalChapterDto) response.getRsltObj();
        applyPrefixSelection(updatedDto, registDto.getPrefixId());
        return response;
    }

    /**
     * 챕터 본문과 개인 Prefix 선택을 같은 트랜잭션에서 수정한다.
     * 시스템 요약·DREAM 챕터는 Prefix를 갖지 않는다.
     */
    @Override
    @Transactional
    public ServiceResponse modify(final JournalChapterDto modifyDto) throws Exception {
        final ServiceResponse response = BaseAttachableService.super.modify(modifyDto);
        final JournalChapterDto updatedDto = (JournalChapterDto) response.getRsltObj();
        applyPrefixSelection(updatedDto, modifyDto.getPrefixId());
        return response;
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param username 사용자 계정명
     * @param key 일련번호
     * @return {@link JournalChapterDto} -- 조회된 객체
     */
    @Cacheable(value=DETAIL_CACHE_NAME, key="new org.springframework.cache.interceptor.SimpleKey(#username, #key)")
    public JournalChapterDto getDetailDtoWithCacheByUser(final String username, final Integer key) throws Exception {
        final JournalChapterEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JournalChapterDto retrieved = mapstruct.toDto(retrievedEntity);
        if (!retrieved.getIsCreatedBy(AuthUtils.requireUsername(username))) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
        return retrieved;
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JournalChapterDto registDto) throws Exception {
        if (registDto.getChapterType() == null) {
            registDto.setChapterType(ChapterType.DIARY);
        }
        if (registDto.getChapterType() == ChapterType.DREAM) {
            throw new BusinessException("journal.chapter.dream-auto-only");
        }
        journalDayResolvedGuard.assertWritable(registDto.getJournalDayId(), JournalDayResolvedAxis.DIARY);
        applyNewChapterSortOrderAndSummaryRole(registDto);
    }

    /**
     * 꿈 챕터 자동 생성 전용 등록 전처리 (DREAM 허용).
     *
     * @param registDto 등록할 객체
     */
    private void preRegistDreamChapterAuto(final JournalChapterDto registDto) throws Exception {
        registDto.setChapterType(ChapterType.DREAM);
        registDto.setSummaryYn(NON_SUMMARY_YN);
        registDto.setPrefixId(null);
        applyNewChapterSortOrder(registDto);
    }

    /**
     * 새 챕터의 정렬값을 계산하고, 첫 일반(non-DREAM) 챕터에는 시스템 요약 역할을 부여한다.
     * <p>
     * 변경 전: {@code sortOrder == 1} 을 기준으로 SUMMARY 를 부여했다. 그러나 sortOrder 는
     * DREAM 챕터까지 포함해 계산되므로, 꿈 챕터가 먼저 있던 날에 첫 일반 챕터를 등록하면
     * sortOrder 가 2 이상이 되어 SUMMARY 가 누락됐다.
     * 변경 후: DREAM 은 항상 마지막에 배치되는 개념 챕터이므로 판정에서 제외하고,
     * "기존 non-DREAM 챕터가 없을 때"를 첫 일반 챕터로 보아 {@code summaryYn=Y}를 부여한다.
     * sortOrder 계산 자체는 그대로 두어 배치/순서에는 영향을 주지 않는다.
     * <p>
     * 시스템 요약 동작은 {@code summaryYn}이 담당한다. 첫 일반 챕터는 클라이언트가 보낸
     * Prefix와 무관하게 시스템 요약으로 확정하며 Prefix 선택을 제거한다.
     * </p>
     *
     * @param registDto 등록할 챕터 DTO
     */
    private void applyNewChapterSortOrderAndSummaryRole(final JournalChapterDto registDto) throws Exception {
        applyNewChapterSortOrder(registDto);
        final boolean hasNonDreamChapter = repository.existsByJournalDayIdAndChapterTypeNot(registDto.getJournalDayId(), ChapterType.DREAM);
        if (!hasNonDreamChapter) {
            if (registDto.getPrefixId() != null) {
                log.warn("[JournalChapter.summary] 첫 일반 챕터의 사용자 Prefix를 무시하고 시스템 요약으로 확정. journalDayId={}, requestedPrefixId={}",
                        registDto.getJournalDayId(), registDto.getPrefixId());
            }
            registDto.setSummaryYn(SUMMARY_YN);
            registDto.setPrefixId(null);
            log.debug("[JournalChapter.summary] 첫 일반 챕터 → 시스템 요약 부여. journalDayId={}, sortOrder={}",
                    registDto.getJournalDayId(), registDto.getSortOrder());
            return;
        }
        if (StringUtils.equals(registDto.getSummaryYn(), SUMMARY_YN)) {
            log.warn("[JournalChapter.summary] 일반 챕터의 시스템 요약 직접 지정 거부. journalDayId={}, summaryYn={}",
                    registDto.getJournalDayId(), registDto.getSummaryYn());
            throw new BusinessException("journal.chapter.summary-auto-only");
        }
        registDto.setSummaryYn(NON_SUMMARY_YN);
    }

    /**
     * 같은 일자 안에서 새 챕터가 들어갈 다음 정렬값을 계산한다.
     *
     * @param registDto 등록할 챕터 DTO
     */
    private void applyNewChapterSortOrder(final JournalChapterDto registDto) throws Exception {
        final int lastSortOrder = repository.findLastIndexByJournalDay(registDto.getJournalDayId()).orElse(0);
        registDto.setSortOrder(lastSortOrder + 1);
    }

    /**
     * 꿈(DREAM) 챕터가 없을 때만 생성한다. 이미 있으면 기존 챕터를 반환한다.
     *
     * @param journalDayId 저널 일자 ID
     * @return 등록 또는 기존 챕터 결과
     */
    @Transactional
    public ServiceResponse registAutoDreamChapter(final Integer journalDayId) throws Exception {
        final JournalDayEntity day = journalDayRepository.findById(journalDayId)
                .orElseThrow(() -> new BusinessException("journal.day.not-found"));
        if (!AuthUtils.isCreatedBy(day.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }

        final JournalChapterEntity existing = repository.findFirstByJournalDayIdAndChapterType(journalDayId, ChapterType.DREAM).orElse(null);
        if (existing != null) {
            clearSummaryRoleIfDream(existing);
            // DREAM 챕터는 사용자 말머리를 갖지 않는다. 기존 선택이 남아 있으면 해제만 한다(해제는 Scope content_type이 필요 없다).
            prefixContentService.applySelection(
                    new BaseAttachableKey(existing.getId(), ContentType.JOURNAL_CHAPTER.key),
                    null,
                    null
            );
            this.getSelf().normalizeSortOrder(journalDayId);
            final JournalChapterEntity synced = repository.findById(existing.getId()).orElse(existing);
            final JournalChapterDto dto = mapstruct.toDto(synced);
            final ServiceResponse response = new ServiceResponse();
            response.setRslt(dto.getId() != null);
            response.setRsltObj(dto);
            return response;
        }

        journalDayResolvedGuard.assertWritable(journalDayId, JournalDayResolvedAxis.DREAM);

        final JournalChapterDto registDto = new JournalChapterDto();
        registDto.setJournalDayId(journalDayId);
        registDto.setTitle(MessageUtils.getMessage("common.dream", null));
        preRegistDreamChapterAuto(registDto);

        final JournalChapterEntity registEntity = mapstruct.toEntity(registDto);
        BaseAttachableManagtHelper.applyRegistManagt(registDto, registEntity);
        final JournalChapterEntity updatedEntity = this.updt(registEntity);
        final JournalChapterDto updatedDto = mapstruct.toDto(updatedEntity);
        BaseAttachableProcPostProcessor.afterWrite(registDto, updatedDto);
        this.postRegist(updatedDto);

        final ServiceResponse response = new ServiceResponse();
        response.setRslt(updatedDto != null && updatedDto.getId() != null);
        response.setRsltObj(updatedDto);
        return response;
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto 등록된 객체
     */
    @Override
    public void postRegist(final JournalChapterDto updatedDto) throws Exception {
        this.createDefaultDiaryWhenSummaryAutoApplied(updatedDto);
        this.getSelf().normalizeSortOrder(updatedDto.getJournalDayId());
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_CHAPTER);
    }

    /**
     * 과거 로직으로 DREAM 자동 챕터에 시스템 요약 역할이 들어간 경우 즉시 제거한다.
     * 시스템 요약 역할은 첫 일반(non-DREAM) 챕터 전용 정책이다.
     *
     * @param chapter 보정할 기존 꿈 챕터 엔티티
     */
    private void clearSummaryRoleIfDream(final JournalChapterEntity chapter) {
        if (chapter == null || chapter.getChapterType() != ChapterType.DREAM) return;
        if (!StringUtils.equals(chapter.getSummaryYn(), SUMMARY_YN)) return;
        chapter.setSummaryYn(NON_SUMMARY_YN);
        repository.saveAndFlush(chapter);
        log.warn("[JournalChapter.summary] DREAM 챕터의 잘못된 시스템 요약 역할 제거. chapterId={}", chapter.getId());
    }

    private void createDefaultDiaryWhenSummaryAutoApplied(final JournalChapterDto updatedDto) throws Exception {
        if (updatedDto == null || updatedDto.getId() == null) return;
        if (updatedDto.getChapterType() != ChapterType.DIARY) return;
        if (!StringUtils.equals(updatedDto.getSummaryYn(), SUMMARY_YN)) return;

        final boolean hasDiary = journalEntryRepository
                .findFirstByJournalChapterIdAndContentTypeOrderBySortOrderDesc(
                        updatedDto.getId(),
                        ContentType.JOURNAL_DIARY.key
                )
                .isPresent();
        if (hasDiary) return;

        final JournalEntryPostDto diaryPostDto = new JournalEntryPostDto();
        diaryPostDto.setJournalChapterId(updatedDto.getId());
        diaryPostDto.setContentType(ContentType.JOURNAL_DIARY.key);
        journalEntryService.regist(diaryPostDto);
    }
    
    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체
     * @param modifyEntity - 수정할 객체
     */
    @Override
    public void preModify(final JournalChapterDto modifyDto, final JournalChapterEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            final Object principal = AuthUtils.isAuthenticated()
                    ? AuthUtils.getAuthentication().getPrincipal()
                    : null;
            log.warn(
                    "[preModify] chapter ownership check failed chapterId={} entityCreatedBy=[{}] loginUsername=[{}] principalType={}",
                    modifyEntity.getId(),
                    modifyEntity.getCreatedBy(),
                    AuthUtils.getLoginUsername(),
                    principal == null ? "null" : principal.getClass().getName()
            );
            throw new NotAuthorizedException("common.result.not-owner");
        }
        journalDayResolvedGuard.assertWritableForChapter(modifyEntity.getId());
        if (modifyEntity.getChapterType() == ChapterType.DREAM) {
            if (modifyDto.getChapterType() != null && modifyDto.getChapterType() != ChapterType.DREAM) {
                throw new BusinessException("journal.chapter.dream-type-locked");
            }
        } else if (modifyDto.getChapterType() == ChapterType.DREAM) {
            throw new BusinessException("journal.chapter.dream-auto-only");
        }
        if (modifyDto.getSummaryYn() != null
                && !StringUtils.equals(modifyDto.getSummaryYn(), modifyEntity.getSummaryYn())) {
            log.warn("[JournalChapter.summary] 수정 요청의 시스템 요약 역할 변경 무시. chapterId={}, requestedSummaryYn={}, persistedSummaryYn={}",
                    modifyEntity.getId(), modifyDto.getSummaryYn(), modifyEntity.getSummaryYn());
        }
        modifyDto.setSummaryYn(modifyEntity.getSummaryYn());
        if (StringUtils.equals(modifyEntity.getSummaryYn(), SUMMARY_YN)) {
            modifyDto.setPrefixId(null);
        }
        final boolean isSortOrderChanged = !Objects.equals(modifyDto.getSortOrder(), modifyEntity.getSortOrder());
        modifyDto.setIsSortOrderChanged(isSortOrderChanged);
    }

    /**
     * 선택한 Prefix의 소유권·활성 상태를 검증하고 prefix_content 연결을 반영한다.
     * 시스템 요약·DREAM 챕터는 사용자 Prefix를 허용하지 않는다.
     *
     * @param dto 저장된 챕터 DTO
     * @param requestedPrefixId 요청한 Prefix ID
     */
    private void applyPrefixSelection(final JournalChapterDto dto, final Integer requestedPrefixId) {
        final JournalChapterEntity entity = repository.findByIdAndCreatedBy(
                        dto.getId(), AuthUtils.requireLoginUsername())
                .orElseThrow(() -> new javax.persistence.EntityNotFoundException("Journal chapter not found."));
        final boolean prefixForbidden = entity.getChapterType() == ChapterType.DREAM
                || StringUtils.equals(entity.getSummaryYn(), SUMMARY_YN);
        final Integer resolvedPrefixId = prefixForbidden ? null : requestedPrefixId;
        if (prefixForbidden && requestedPrefixId != null) {
            log.warn("[JournalChapter.prefix] 시스템 챕터 Prefix 선택 무시. chapterId={}, chapterType={}, summaryYn={}, requestedPrefixId={}",
                    entity.getId(), entity.getChapterType(), entity.getSummaryYn(), requestedPrefixId);
        }
        // 말머리 목록 Scope는 챕터 유형(일기/노트)으로 분리한다. attachable 정체성 키는 JOURNAL_CHAPTER로 유지한다.
        // 말머리 금지(요약·DREAM) 챕터는 기존 선택을 해제만 하므로 Scope content_type이 필요 없다(검증 경로가 null이면 조회하지 않음).
        final String prefixScopeContentType = prefixForbidden
                ? null
                : resolveChapterPrefixScopeContentType(entity.getChapterType()).key;
        final PrefixEntity prefix = prefixContentService.applySelection(
                new BaseAttachableKey(entity.getId(), ContentType.JOURNAL_CHAPTER.key),
                prefixScopeContentType,
                resolvedPrefixId
        );
        dto.setPrefix(prefix == null ? null : PrefixDto.builder()
                .id(prefix.getId())
                .name(prefix.getName())
                .color(prefix.getColor())
                .sortOrder(prefix.getSortOrder())
                .activeYn(prefix.getActiveYn())
                .build());
        dto.setPrefixId(resolvedPrefixId);
    }

    /**
     * 챕터 유형을 챕터 말머리 목록의 content_type으로 변환한다.
     * <p>
     * 챕터의 attachable 정체성은 {@link ContentType#JOURNAL_CHAPTER}로 불변이지만, 말머리 목록은
     * 일기 챕터({@link ContentType#JOURNAL_CHAPTER_DIARY})와 노트 챕터
     * ({@link ContentType#JOURNAL_CHAPTER_NOTE})가 각각 사용자 정의로 분리된다.
     * DREAM 챕터는 사용자 말머리를 허용하지 않으므로(요약 챕터와 함께 {@code prefixForbidden})
     * 이 변환을 호출하지 않는다. 방어적으로 DREAM·null 입력은 예외로 차단한다.
     * </p>
     *
     * @param chapterType 챕터 유형 (DIARY 또는 NOTE)
     * @return 챕터 말머리 Scope content_type
     */
    static ContentType resolveChapterPrefixScopeContentType(final ChapterType chapterType) {
        if (chapterType == null) {
            throw new BusinessException("journal.chapter.invalid-chapter-type");
        }
        return switch (chapterType) {
            case DIARY -> ContentType.JOURNAL_CHAPTER_DIARY;
            case NOTE -> ContentType.JOURNAL_CHAPTER_NOTE;
            case DREAM -> throw new BusinessException("journal.chapter.prefix-not-allowed");
        };
    }

    /**
     * 수정 후처리. (override)
     *
     * @param postDto 수정 요청 객체
     * @param updatedDto 수정 결과 객체
     */
    @Override
    public void postModify(final JournalChapterDto postDto, final JournalChapterDto updatedDto) throws Exception {
        if (Boolean.TRUE.equals(postDto.getIsSortOrderChanged())) {
            // sortOrder 변경 시에는 목표 위치로 삽입 재배치 후 정규화
            this.getSelf().insert(updatedDto.getJournalDayId(), updatedDto.getId(), postDto.getSortOrder());
        }
        // DREAM 마지막 규칙을 포함한 최종 정규화
        this.getSelf().normalizeSortOrder(updatedDto.getJournalDayId());

        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_CHAPTER);
    }

    /**
     * 삭제 전처리. 작성자·일자 잠금·Reflection 참조 Block 을 검증한다.
     * 챕터 내 엔트리를 대상으로 둔 Reflection 이 있으면 삭제를 거부한다(Reference→Block).
     * Hibernate cascade 는 서비스 preDelete 를 우회하므로 여기서 막는다.
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JournalChapterDto deletedDto) throws Exception {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.not-owner");
        }
        journalDayResolvedGuard.assertWritableForChapter(deletedDto.getId());
        assertNoChildEntries(deletedDto.getId());
        assertNoAttachedReflections(deletedDto.getId());
    }

    /**
     * 챕터에 하위 엔트리가 하나라도 있으면 삭제를 Block 한다.
     *
     * @param chapterId 삭제 대상 챕터 ID
     */
    private void assertNoChildEntries(final Integer chapterId) {
        if (chapterId == null) return;
        final List<JournalEntryEntity> entries = journalEntryRepository.findAllByJournalChapterId(chapterId);
        if (!CollectionUtils.isEmpty(entries)) {
            log.warn("[JournalChapter] 삭제 Block — 하위 엔트리 존재. chapterId={}, entryCount={}",
                    chapterId, entries.size());
            throw new BusinessException("journal.chapter.delete.blocked-by-entries");
        }
    }

    /**
     * 챕터 내 엔트리에 참조 Reflection 이 있으면 삭제를 Block 한다.
     *
     * @param chapterId 삭제 대상 챕터 ID
     */
    private void assertNoAttachedReflections(final Integer chapterId) {
        if (chapterId == null) return;
        final List<JournalEntryEntity> entries = journalEntryRepository.findAllByJournalChapterId(chapterId);
        if (CollectionUtils.isEmpty(entries)) return;

        final List<Integer> entryIds = entries.stream()
                .map(JournalEntryEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        final List<ContentType> contentTypes = entries.stream()
                .map(JournalEntryEntity::getContentType)
                .filter(StringUtils::isNotBlank)
                .map(ContentType::get)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (entryIds.isEmpty() || contentTypes.isEmpty()) return;

        if (journalReflectionRepository.existsByRefIdInAndRefContentTypeIn(entryIds, contentTypes)) {
            log.warn("[JournalChapter] 삭제 Block — 챕터 내 엔트리에 참조 Reflection 존재. chapterId={}, entryCount={}",
                    chapterId, entryIds.size());
            throw new BusinessException("journal.chapter.delete.blocked-by-reflection");
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */

    @Override
    public void postDelete(final JournalChapterDto deletedDto) throws Exception {
        this.getSelf().normalizeSortOrder(deletedDto.getJournalDayId());

        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_CHAPTER);
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JournalChapterDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JournalChapterDto getDeletedDetailDto(final Integer key) throws Exception {
        final JournalChapterDto deleted = journalChapterMapper.getDeletedById(key);
        if (deleted == null) return null;
        if (!AuthUtils.isCreatedBy(deleted.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.not-owner");
        }
        return deleted;
    }
    
    /**
     * 해당 그룹 전체를 sortOrder = 1부터 다시 정렬한다.
     *
     * @param journalDayId 정렬을 수행할 상위 키
     */
    @Transactional
    public void normalizeSortOrder(final Integer journalDayId) {
        final List<JournalChapterEntity> list = repository.findAllByJournalDayId(journalDayId);
        if (CollectionUtils.isEmpty(list)) return;

        list.sort(Comparator
                // DREAM 챕터는 sortOrder와 무관하게 항상 마지막으로 배치
                .comparingInt((JournalChapterEntity e) -> e.getChapterType() == ChapterType.DREAM ? 1 : 0)
                .thenComparingInt((JournalChapterEntity e) -> e.getSortOrder() == null ? Integer.MAX_VALUE : e.getSortOrder())
                .thenComparing(JournalChapterEntity::getId));

        int sortOrder = 1;
        for (final JournalChapterEntity e : list) {
            e.setSortOrder(sortOrder++);
        }
        repository.saveAllAndFlush(list);
    }

    /**
     * 대상 상위 키에 엔티티를 특정 위치에 삽입 후 재정렬한다.
     *
     * @param journalDayId 정렬을 수행할 상위 키
     * @param id 게시물 PK
     * @param targetSortOrder 삽입할 목표 위치(1-based). null이면 맨 뒤에 삽입됨
     */
    @Transactional
    public void insert(final Integer journalDayId, final Integer id, Integer targetSortOrder) throws Exception {
        final List<JournalChapterDto> list = journalChapterMapper.findAllForReorder(journalDayId);

        // target 조회
        final JournalChapterEntity targetEntity = findDtlEntity(id);
        final JournalChapterDto target = mapstruct.toDto(targetEntity);
        if (target == null) return;

        // 혹시 이미 포함되어 있으면 제거
        list.removeIf(e -> Objects.equals(e.getId(), id));

        // chapterNo 변경
        target.setJournalDayId(journalDayId);

        // targetSortOrder 보정 (upper bound)
        final int maxIdx = list.size() + 1;
        final int normalizedIdx = Math.min(targetSortOrder == null ? maxIdx : targetSortOrder, maxIdx);
        // 삽입 위치 계산
        int pos = normalizedIdx - 1;
        pos = Math.min(pos, list.size());
        list.add(pos, target);

        // sortOrder 재정렬
        int sortOrder = 1;
        for (final JournalChapterDto e : list) {
            e.setSortOrder(sortOrder++);
        }

        journalChapterMapper.batchUpdateIdx(list);
    }

    /**
     * 정렬 순서 변경 시 관련 순서를 업데이트
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderSortOrder(final JournalChapterDto updatedDto) throws Exception {
        // sort_order 재부여 (동순위는 id 순)
        normalizeSortOrder(updatedDto.getJournalDayId());
    }

    /**
     * 챕터를 다른 일자로 이동한다. DREAM 챕터는 이동 불가.
     * 대상 일자가 없으면 신규 일자를 생성하고 이동한다.
     * 이동 후 구 일자/신 일자 모두 sortOrder 정규화 및 캐시 무효화를 수행한다.
     *
     * @param id 챕터 식별자
     * @param targetStdrdDt 이동할 대상 일자 (yyyy-MM-dd)
     * @return {@link ServiceResponse} -- 처리 결과
     */
    @Transactional
    public ServiceResponse moveChapter(final Integer id, final String targetStdrdDt) throws Exception {
        // 챕터 조회
        final JournalChapterEntity chapterEntity = this.getDtlEntity(id);
        // 권한 체크
        if (!AuthUtils.isCreatedBy(chapterEntity.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.not-owner");
        }
        journalDayResolvedGuard.assertWritableForChapter(id);
        // DREAM 챕터 이동 불가
        if (chapterEntity.getChapterType() == ChapterType.DREAM) {
            throw new BusinessException("journal.chapter.dream-type-locked");
        }

        final Integer oldJournalDayId = chapterEntity.getJournalDayId();
        final String username = AuthUtils.getLoginUsername();

        // 대상 일자 찾기
        final LocalDate journalDate = DateUtils.asLocalDate(targetStdrdDt);
        JournalDayEntity targetDay = journalDayRepository.findByJournalDate(journalDate, username);
        if (targetDay == null) {
            // 대상 일자가 없으면 신규 등록
            log.info("[moveChapter] 대상 일자 없음 → 신규 등록: targetStdrdDt={}", targetStdrdDt);
            final JournalDayDto newDayDto = new JournalDayDto();
            newDayDto.setJournalDate(targetStdrdDt);
            journalDayService.regist(newDayDto);
            targetDay = journalDayRepository.findByJournalDate(journalDate, username);
            if (targetDay == null) {
                throw new BusinessException("journal.day.not-found");
            }
        }

        final Integer newJournalDayId = targetDay.getId();
        // 같은 일자면 no-op
        if (Objects.equals(oldJournalDayId, newJournalDayId)) {
            log.info("[moveChapter] 같은 일자로 이동 요청 → no-op: chapterId={}, dayId={}", id, newJournalDayId);
            final ServiceResponse response = new ServiceResponse();
            response.setRslt(true);
            return response;
        }

        // 챕터를 신 일자 마지막 순서로 이동
        final int lastSortOrder = repository.findLastIndexByJournalDay(newJournalDayId).orElse(0);
        chapterEntity.setJournalDayId(newJournalDayId);
        chapterEntity.setSortOrder(lastSortOrder + 1);
        repository.saveAndFlush(chapterEntity);
        log.info("[moveChapter] 챕터 이동: chapterId={}, oldDayId={} → newDayId={}", id, oldJournalDayId, newJournalDayId);

        // 구 일자 정규화
        this.getSelf().normalizeSortOrder(oldJournalDayId);
        // 신 일자 정규화 (DREAM 마지막 규칙 포함)
        this.getSelf().normalizeSortOrder(newJournalDayId);

        // 구 일자 캐시 무효화
        final JournalDayEntity oldDay = journalDayRepository.findById(oldJournalDayId).orElse(null);
        if (oldDay != null) {
            journalCacheEvictWorker.evictAfterCommit(
                JournalCacheEvictParam.builder()
                    .createdBy(username)
                    .journalDayId(oldJournalDayId)
                    .yy(oldDay.getYy())
                    .mnth(oldDay.getMnth())
                    .weekStartDt(DateUtils.asStr(oldDay.getWeekStartDt(), DatePtn.DATE))
                    .build(),
                ContentType.JOURNAL_CHAPTER
            );
        }
        // 신 일자 캐시 무효화
        journalCacheEvictWorker.evictAfterCommit(
            JournalCacheEvictParam.builder()
                .createdBy(username)
                .journalDayId(newJournalDayId)
                .yy(targetDay.getYy())
                .mnth(targetDay.getMnth())
                .weekStartDt(DateUtils.asStr(targetDay.getWeekStartDt(), DatePtn.DATE))
                .build(),
            ContentType.JOURNAL_CHAPTER
        );
        // 챕터 상세 캐시 무효화 (신 일자 기준으로 명시적으로 구성 — mapstruct 시 구 journalDay association이 잔류할 수 있어 직접 빌드)
        journalCacheEvictWorker.evictAfterCommit(
            JournalCacheEvictParam.builder()
                .createdBy(chapterEntity.getCreatedBy())
                .contentType(chapterEntity.getContentType())
                .id(chapterEntity.getId())
                .journalDayId(newJournalDayId)
                .yy(targetDay.getYy())
                .mnth(targetDay.getMnth())
                .weekStartDt(DateUtils.asStr(targetDay.getWeekStartDt(), DatePtn.DATE))
                .build(),
            ContentType.JOURNAL_CHAPTER
        );

        final ServiceResponse response = new ServiceResponse();
        response.setRslt(true);
        return response;
    }
}
