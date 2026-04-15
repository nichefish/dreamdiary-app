package io.nicheblog.dreamdiary.feature.journal.diary.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.helper.BaseClsfHistoryHelper;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.clsf.related.service.RelatedContentService;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.mapstruct.JournalDiaryMapstruct;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryPostDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiarySearchParam;
import io.nicheblog.dreamdiary.feature.journal.diary.repository.jpa.JournalDiaryRepository;
import io.nicheblog.dreamdiary.feature.journal.diary.repository.mybatis.JournalDiaryMapper;
import io.nicheblog.dreamdiary.feature.journal.diary.spec.JournalDiarySpec;
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
 * JournalDiaryService
 * <pre>
 *  저널 일기 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalDiaryService
        implements BaseClsfService<JournalDiaryPostDto, JournalDiaryDto, Integer, JournalDiaryEntity>, BaseMultipartWritableService<JournalDiaryPostDto, JournalDiaryDto, Integer, JournalDiaryEntity> {

    @Getter
    private final JournalDiaryRepository repository;
    @Getter
    private final JournalDiarySpec spec;
    @Getter
    private final JournalDiaryMapstruct mapstruct;
    @Getter
    private final JournalDiaryMapper mapper;

    public JournalDiaryMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalDiaryMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JournalCacheEvictWorker journalCacheEvictWorker;
    private final RelatedContentService relatedContentService;

    private final ApplicationContext context;
    private JournalDiaryService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 사용자별 특정 년도의 일기 목록 조회 :: 캐시 처리
     *
     * @param username String
     * @param searchParam JournalDiarySearchParam
     * @return {@link List} -- 해당 년도의 중요 목록
     */
    @Cacheable(value="journalDiaryYySumryStatedListByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #searchParam.toSummaryCacheKey())")
    public List<JournalDiaryDto> getSumryDiaryListByUser(final String username, final JournalDiarySearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        final List<JournalDiaryDto> journalDiaryYySumryStatedListByUser = this.getSelf().getListDto(searchParam);
        Collections.sort(journalDiaryYySumryStatedListByUser);

        return journalDiaryYySumryStatedListByUser;
    }

    public List<JournalDiaryDto> getListDtoByUser(final String username, final JournalDiarySearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JournalDiaryPostDto registDto) throws Exception {
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
    public void postRegist(final JournalDiaryDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_DIARY);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체
     * @param modifyEntity - 수정할 객체
     */
    @Override
    public void preModify(final JournalDiaryPostDto modifyDto, final JournalDiaryEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }

        final boolean isSortOrderChanged = !Objects.equals(modifyDto.getSortOrder(), modifyEntity.getSortOrder());
        modifyDto.setIsSortOrderChanged(isSortOrderChanged);
        final boolean isChapterChanged = !Objects.equals(modifyDto.getJournalChapterId(), modifyEntity.getJournalChapter().getId());
        modifyDto.setIsChapterChanged(isChapterChanged);
        if (isChapterChanged) {
            modifyDto.setPrevJournalChapterId(modifyEntity.getJournalChapter().getId());
        }
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JournalDiaryPostDto postDto, final JournalDiaryDto updatedDto) throws Exception {
        // 정렬 순서 재조정 ('이동' 포함)
        if (postDto.getIsChapterChanged()) {
            this.getSelf().reorderWhenChapterChanged(postDto);
        } else if (postDto.getIsSortOrderChanged()) {
            this.getSelf().reorderSortOrder(postDto);
        }

        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_DIARY);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JournalDiaryDto} -- 조회된 객체
     */
    @Cacheable(value="journalDiaryDtlDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #key)")
    public JournalDiaryDto getDtlDtoWithCacheByUser(final String username, final Integer key) throws Exception {
        final JournalDiaryEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JournalDiaryDto retrieved = mapstruct.toDto(retrievedEntity);
        // 권한 체크
        if (!retrieved.getIsCreatedBy(AuthUtils.requireUsername(username))) throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        return retrieved;
    }

    @Transactional
    public JournalDiaryDto updtCn(
            final Integer key,
            final String updatedCn,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        final JournalDiaryEntity restoreEntity = this.getSelf().getDtlEntity(key);
        final JournalDiaryEntity historySnapshot = BaseClsfHistoryHelper.isHistoryModule(restoreEntity)
                ? restoreEntity.toBuilder().build()
                : null;

        restoreEntity.setCn(updatedCn);
        BaseClsfHistoryHelper.applyModifyHistory(historySnapshot, restoreEntity);

        final JournalDiaryEntity updatedEntity = getRepository().saveAndFlush(restoreEntity);
        BaseClsfHistoryHelper.publishHistoryEventIfSupported(this, historySnapshot, updatedEntity, historyType, fromHistoryId);

        final JournalDiaryDto updatedDto = getReadMapstruct().toDto(updatedEntity);
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_DIARY);
        return updatedDto;
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JournalDiaryDto deletedDto) throws Exception {
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
    public void postDelete(final JournalDiaryDto deletedDto) throws Exception {
        // 정렬 순서 재조정
        this.getSelf().normalizeSortOrder(deletedDto.getJournalChapterId());
        
        // 관련 캐시 삭제
        // 관련글 soft-delete
        relatedContentService.deleteAllByRef(new BaseClsfKey(deletedDto.getId(), ContentType.JOURNAL_DIARY), deletedDto.getCreatedBy());

        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_DIARY);
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JournalDiaryDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JournalDiaryDto getDeletedDtlDto(final Integer key) throws Exception {
        final JournalDiaryDto deleted = mapper.getDeletedById(key);
        if (deleted == null) return null;
        if (!AuthUtils.isCreatedBy(deleted.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return deleted;
    }

    /**
     * 해당 그룹 전체를 sortOrder = 1부터 다시 정렬한다.
     *
     * @param journalChapterId 정렬을 수행할 상위 키
     */
    @Transactional
    public void normalizeSortOrder(final Integer journalChapterId) {
        final List<JournalDiaryDto> list = mapper.findAllForReorder(journalChapterId);
        if (CollectionUtils.isEmpty(list)) return;

        int sortOrder = 1;
        for (final JournalDiaryDto e : list) {
            e.setSortOrder(sortOrder++);
            EhCacheUtils.evictUserCacheByKey("journalDiaryDtlDtoByUser", e.getCreatedBy(), e.getId());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * 대상 상위 키에 엔티티를 특정 위치에 삽입 후 재정렬한다.
     *
     * @param journalChapterId 정렬을 수행할 상위 키
     * @param id 게시물 PK
     * @param targetSortOrder 삽입할 목표 위치(1-based). null이면 맨 뒤에 삽입됨
     */
    @Transactional
    public void insert(final Integer journalChapterId, final Integer id, Integer targetSortOrder) throws Exception {
        final List<JournalDiaryDto> list = mapper.findAllForReorder(journalChapterId);

        // target 조회
        final JournalDiaryEntity targetEntity = getDtlEntity(id);
        final JournalDiaryDto target = mapstruct.toDto(targetEntity);
        if (target == null) return;

        // 혹시 이미 포함되어 있으면 제거
        list.removeIf(e -> Objects.equals(e.getId(), id));

        // chapterNo 변경
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
        for (final JournalDiaryDto e : list) {
            e.setSortOrder(sortOrder++);
            EhCacheUtils.evictUserCacheByKey("journalDiaryDtlDtoByUser", e.getCreatedBy(), e.getId());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * chapterNo가 바뀌었을 때 챕터 이동 + 정렬 처리
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderWhenChapterChanged(final JournalDiaryPostDto updatedDto) throws Exception {
        // 1) 기존 chapter 그룹 정리 (삭제처리와 동일한 효과)
        normalizeSortOrder(updatedDto.getPrevJournalChapterId());
        // 2) 새 chapter 그룹에 삽입
        insert(updatedDto.getJournalChapterId(), updatedDto.getId(), updatedDto.getSortOrder());
    }

    /**
     * 정렬 순서 변경 시 관련 순서를 업데이트
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderSortOrder(final JournalDiaryPostDto updatedDto) throws Exception {
        // 1단계: 현재 chapter 그룹 정리 (기존 sortOrder 값을 normalize하여 안정화)
        normalizeSortOrder(updatedDto.getJournalChapterId());
        // 2단계: 해당 group에 새 위치로 target 삽입
        insert(updatedDto.getJournalChapterId(), updatedDto.getId(), updatedDto.getSortOrder());
    }

    /**
     * 주어진 {@link JournalDayDto} 객체에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param journalDiary 공휴일 및 주말 정보를 설정할 대상 DTO
     * @param holydayMap 날짜(String: yyyy-MM-dd) → 공휴일 이름 목록 매핑 정보
     */
    private void setHolydayInfo(final JournalDiaryDto journalDiary, final Map<String, List<String>> holydayMap) throws Exception {
        if (journalDiary == null || holydayMap == null) return;

        final String stdrdDt = journalDiary.getStdrdDt();
        final boolean isHolyday = holydayMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        journalDiary.setIsHolyday(isHolyday || isWeekend);
        if (isHolyday) {
            final String concatHolydayNm = String.join(", ", holydayMap.get(stdrdDt));
            journalDiary.setHolydayNm(concatHolydayNm);
        }
    }
}


