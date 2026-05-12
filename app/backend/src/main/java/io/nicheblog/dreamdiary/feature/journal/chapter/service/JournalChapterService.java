package io.nicheblog.dreamdiary.feature.journal.chapter.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableManagtHelper;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableProcPostProcessor;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
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
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    /** 동일 일자 내 첫 항목 등록 시 기본 카테고리 코드 */
    private static final String FIRST_CHAPTER_CTGR_CD = "SUMMARY";

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
    private final JournalEntryService journalEntryService;
    private final JournalEntryRepository journalEntryRepository;

    private final ApplicationContext context;
    private JournalChapterService getSelf() {
        return context.getBean(this.getClass());
    }

    public List<JournalChapterDto> getListDtoByUser(final String username, final JournalChapterSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
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
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
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
            throw new BusinessException("msg.journal.chapter.dream-auto-only");
        }
        applyNewChapterSortOrderAndDefaultCategory(registDto);
    }

    /**
     * 꿈 챕터 자동 생성 전용 등록 전처리 (DREAM 허용).
     *
     * @param registDto 등록할 객체
     */
    private void preRegistDreamChapterAuto(final JournalChapterDto registDto) throws Exception {
        registDto.setChapterType(ChapterType.DREAM);
        applyNewChapterSortOrder(registDto);
    }

    /**
     * 새 챕터의 정렬값을 계산하고, 첫 DIARY 챕터에는 기본 SUMMARY 카테고리를 보정한다.
     *
     * @param registDto 등록할 챕터 DTO
     */
    private void applyNewChapterSortOrderAndDefaultCategory(final JournalChapterDto registDto) throws Exception {
        applyNewChapterSortOrder(registDto);
        if (registDto.getSortOrder() == 1 && StringUtils.isBlank(registDto.getCategoryCode())) {
            registDto.setCategoryCode(FIRST_CHAPTER_CTGR_CD);
        }
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
                .orElseThrow(() -> new BusinessException("msg.journal.day.not-found"));
        if (!AuthUtils.isCreatedBy(day.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }

        final JournalChapterEntity existing = repository.findFirstByJournalDayIdAndChapterType(journalDayId, ChapterType.DREAM).orElse(null);
        if (existing != null) {
            clearFirstChapterCategoryIfDream(existing);
            this.getSelf().normalizeSortOrder(journalDayId);
            final JournalChapterEntity synced = repository.findById(existing.getId()).orElse(existing);
            final JournalChapterDto dto = mapstruct.toDto(synced);
            final ServiceResponse response = new ServiceResponse();
            response.setRslt(dto.getId() != null);
            response.setRsltObj(dto);
            return response;
        }

        final JournalChapterDto registDto = new JournalChapterDto();
        registDto.setJournalDayId(journalDayId);
        registDto.setTitle(MessageUtils.getMessage("txt.dream", null));
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
     * 과거 로직으로 DREAM 자동 챕터에 SUMMARY가 들어간 경우 즉시 제거한다.
     * SUMMARY 기본값은 첫 DIARY 챕터 전용 정책이다.
     *
     * @param chapter 보정할 기존 꿈 챕터 엔티티
     */
    private void clearFirstChapterCategoryIfDream(final JournalChapterEntity chapter) {
        if (chapter == null || chapter.getChapterType() != ChapterType.DREAM) return;
        if (!StringUtils.equals(chapter.getCategoryCode(), FIRST_CHAPTER_CTGR_CD)) return;
        chapter.setCategoryCode(null);
        repository.saveAndFlush(chapter);
    }

    private void createDefaultDiaryWhenSummaryAutoApplied(final JournalChapterDto updatedDto) throws Exception {
        if (updatedDto == null || updatedDto.getId() == null) return;
        if (updatedDto.getChapterType() != ChapterType.DIARY) return;
        if (updatedDto.getSortOrder() == null || updatedDto.getSortOrder() != 1) return;
        if (!StringUtils.equals(updatedDto.getCategoryCode(), FIRST_CHAPTER_CTGR_CD)) return;

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
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        if (modifyEntity.getChapterType() == ChapterType.DREAM) {
            if (modifyDto.getChapterType() != null && modifyDto.getChapterType() != ChapterType.DREAM) {
                throw new BusinessException("msg.journal.chapter.dream-type-locked");
            }
        } else if (modifyDto.getChapterType() == ChapterType.DREAM) {
            throw new BusinessException("msg.journal.chapter.dream-auto-only");
        }
        final boolean isSortOrderChanged = !Objects.equals(modifyDto.getSortOrder(), modifyEntity.getSortOrder());
        modifyDto.setIsSortOrderChanged(isSortOrderChanged);
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
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JournalChapterDto deletedDto) throws Exception {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
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
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
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
}
