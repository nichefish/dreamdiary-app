package io.nicheblog.dreamdiary.feature.journal.dream.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableHistoryHelper;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.related.service.RelatedContentService;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.feature.journal.dream.mapstruct.JournalDreamMapstruct;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamPostDto;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamSearchParam;
import io.nicheblog.dreamdiary.feature.journal.dream.repository.jpa.JournalDreamRepository;
import io.nicheblog.dreamdiary.feature.journal.dream.repository.mybatis.JournalDreamMapper;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.dream.spec.JournalDreamSpec;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JournalDreamService
 * <pre>
 *  저널 꿈 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalDreamService
        implements BaseAttachableService<JournalDreamPostDto, JournalDreamDto, Integer, JournalDreamEntity>, BaseMultipartWritableService<JournalDreamPostDto, JournalDreamDto, Integer, JournalDreamEntity> {

    @Getter
    private final JournalDreamRepository repository;
    @Getter
    private final JournalDreamSpec spec;
    @Getter
    private final JournalDreamMapstruct mapstruct;
    @Getter
    private final JournalDreamMapper mapper;

    public JournalDreamMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalDreamMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JournalCacheEvictWorker journalCacheEvictWorker;
    private final RelatedContentService relatedContentService;
    private final JournalChapterRepository journalChapterRepository;

    private final ApplicationContext context;
    private JournalDreamService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 사용자별 특정 년도의 중요 꿈 목록 조회 :: 캐시 처리
     *
     * @param username 사용자 계정명
     * @param searchParam JournalDreamSearchParam
     * @return {@link List} -- 해당 년도의 중요 목록
     */
    @Cacheable(value="journalDreamYyAnnualStatedListByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #searchParam.toSummaryCacheKey())")
    public List<JournalDreamDto> getAnnualDreamListByUser(final String username, final JournalDreamSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        final List<JournalDreamDto> journalDreamYyAnnualStatedListByUser = this.getSelf().getListDto(searchParam);
        Collections.sort(journalDreamYyAnnualStatedListByUser);

        return journalDreamYyAnnualStatedListByUser;
    }

    public List<JournalDreamDto> getListDtoByUser(final String username, final JournalDreamSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JournalDreamPostDto registDto) throws Exception {
        assertDreamChapterForDream(registDto.getJournalChapterId());
        // 정렬 순서 처리
        final Integer lastSortOrder = repository.findLastIndexByJournalChapter(registDto.getJournalChapterId()).orElse(0);
        registDto.setSortOrder(lastSortOrder + 1);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JournalDreamDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_DREAM);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JournalDreamDto} -- 조회된 객체
     */
    @Cacheable(value="journalDreamDtlDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #key)")
    public JournalDreamDto getDtlDtoWithCacheByUser(final String username, final Integer key) throws Exception {
        final JournalDreamEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JournalDreamDto retrieved = mapstruct.toDto(retrievedEntity);
        // 권한 체크
        if (!retrieved.getIsCreatedBy(AuthUtils.requireUsername(username))) throw new NotAuthorizedException("msg.rslt.access-not-authorized");

        return retrieved;
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체
     * @param modifyEntity - 수정할 객체
     */
    @Override
    public void preModify(final JournalDreamPostDto modifyDto, final JournalDreamEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        final Integer chapterId = modifyDto.getJournalChapterId() != null
                ? modifyDto.getJournalChapterId()
                : modifyEntity.getJournalChapter().getId();
        assertDreamChapterForDream(chapterId);
        final boolean isSortOrderChanged = !Objects.equals(modifyDto.getSortOrder(), modifyEntity.getSortOrder());
        modifyDto.setIsSortOrderChanged(isSortOrderChanged);
    }
    
    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JournalDreamPostDto postDto, final JournalDreamDto updatedDto) throws Exception {
        // 정렬 순서 재조정
        if (updatedDto.getIsSortOrderChanged()) this.getSelf().reorderSortOrder(updatedDto);
        
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_DREAM);
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JournalDreamDto deletedDto) throws Exception {
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
    public void postDelete(final JournalDreamDto deletedDto) throws Exception {
        // 정렬 순서 재조정
        this.getSelf().reorderSortOrder(deletedDto);

        // 관련 캐시 삭제
        // 관련글 soft-delete
        relatedContentService.deleteAllByRef(new BaseAttachableKey(deletedDto.getId(), ContentType.JOURNAL_DREAM), deletedDto.getCreatedBy());

        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_DREAM);
    }

    /**
     * 내용 수정 (이력 생성 포함).
     *
     * @param key 식별자
     * @param updatedCn 수정할 내용
     * @param historyType 이력 타입
     * @param fromHistoryId 복구 원본 이력 번호 (복구 시)
     * @return {@link JournalDreamDto} -- 수정된 객체
     */
    @Transactional
    public JournalDreamDto updtContent(
            final Integer key,
            final String updatedCn,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        final JournalDreamEntity restoreEntity = this.getSelf().getDtlEntity(key);
        final JournalDreamEntity historySnapshot = BaseAttachableHistoryHelper.isHistoryModule(restoreEntity)
                ? restoreEntity.toBuilder().build()
                : null;

        restoreEntity.setContent(updatedCn);
        BaseAttachableHistoryHelper.applyModifyHistory(historySnapshot, restoreEntity);

        final JournalDreamEntity updatedEntity = getRepository().saveAndFlush(restoreEntity);
        BaseAttachableHistoryHelper.publishHistoryEventIfSupported(this, historySnapshot, updatedEntity, historyType, fromHistoryId);

        final JournalDreamDto updatedDto = getReadMapstruct().toDto(updatedEntity);
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_DREAM);
        return updatedDto;
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JournalDreamDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JournalDreamDto getDeletedDtlDto(final Integer key) throws Exception {
        final JournalDreamDto deleted = mapper.getDeletedById(key);
        if (deleted == null) return null;
        if (!AuthUtils.isCreatedBy(deleted.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return deleted;
    }

    /**
     * 해당 챕터 그룹 전체를 sortOrder = 1부터 다시 정렬한다.
     *
     * @param journalChapterId 정렬을 수행할 챕터 키
     */
    @Transactional
    public void normalizeSortOrder(final Integer journalChapterId) {
        final List<JournalDreamDto> list = mapper.findAllForReorder(journalChapterId);
        if (CollectionUtils.isEmpty(list)) return;

        int sortOrder = 1;
        for (final JournalDreamDto e : list) {
            e.setSortOrder(sortOrder++);
            EhCacheUtils.evictUserCacheByKey("journalDreamDtlDtoByUser", e.getCreatedBy(), e.getId());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * 대상 챕터에 엔티티를 특정 위치에 삽입 후 재정렬한다.
     *
     * @param journalChapterId 정렬을 수행할 챕터 키
     * @param id 게시물 PK
     * @param targetSortOrder 삽입할 목표 위치(1-based). null이면 맨 뒤에 삽입됨
     */
    @Transactional
    public void insert(final Integer journalChapterId, final Integer id, Integer targetSortOrder) throws Exception {
        final List<JournalDreamDto> list = mapper.findAllForReorder(journalChapterId);

        // target 조회
        final JournalDreamEntity targetEntity = findDtlEntity(id);
        final JournalDreamDto target = mapstruct.toDto(targetEntity);
        if (target == null) return;

        // 혹시 이미 포함되어 있으면 제거
        list.removeIf(e -> Objects.equals(e.getId(), id));

        // journalChapterId 변경
        target.setJournalChapterId(journalChapterId);

        // targetSortOrder 보정 (upper bound)
        final int maxIdx = list.size() + 1;
        final int normalizedIdx = Math.min(targetSortOrder == null ? maxIdx : targetSortOrder, maxIdx);
        // 삽입 위치 계산
        int pos = normalizedIdx - 1;
        pos = Math.min(pos, list.size());
        list.add(pos, target);

        // sortOrder 재정렬
        int sortOrder = 1;
        for (final JournalDreamDto e : list) {
            e.setSortOrder(sortOrder++);
            EhCacheUtils.evictUserCacheByKey("journalDreamDtlDtoByUser", e.getCreatedBy(), e.getId());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * 정렬 순서 변경 시 관련 순서를 업데이트
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderSortOrder(final JournalDreamDto updatedDto) throws Exception {
        // 1단계: 현재 챕터 그룹 정리 (기존 sortOrder 값을 normalize하여 안정화)
        normalizeSortOrder(updatedDto.getJournalChapterId());
        // 2단계: 해당 group에 새 위치로 target 삽입
        insert(updatedDto.getJournalChapterId(), updatedDto.getId(), updatedDto.getSortOrder());
    }

    /**
     * 주어진 {@link JournalDayDto} 객체에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param journalDream 공휴일 및 주말 정보를 설정할 대상 DTO
     * @param holydayMap 날짜(String: yyyy-MM-dd) → 공휴일 이름 목록 매핑 정보
     */
    private void setHolydayInfo(final JournalDreamDto journalDream, final Map<String, List<String>> holydayMap) throws Exception {
        if (journalDream == null || holydayMap == null) return;

        final String stdrdDt = journalDream.getStdrdDt();
        final boolean isHolyday = holydayMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        journalDream.setIsHolyday(isHolyday || isWeekend);
        if (isHolyday) {
            final String concatHolydayNm = String.join(", ", holydayMap.get(stdrdDt));
            journalDream.setHolydayNm(concatHolydayNm);
        }
    }

    private void assertDreamChapterForDream(final Integer journalChapterId) {
        final JournalChapterEntity chapter = journalChapterRepository.findById(journalChapterId)
                .orElseThrow(() -> new BusinessException("msg.journal.chapter.not-found"));
        if (chapter.getChapterType() != ChapterType.DREAM) {
            throw new BusinessException("msg.journal.dream.dream-chapter-only");
        }
    }
}


