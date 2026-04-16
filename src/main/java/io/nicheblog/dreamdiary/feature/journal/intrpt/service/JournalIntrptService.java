package io.nicheblog.dreamdiary.feature.journal.intrpt.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.helper.BaseClsfHistoryHelper;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.intrpt.entity.JournalIntrptEntity;
import io.nicheblog.dreamdiary.feature.journal.intrpt.mapstruct.JournalIntrptMapstruct;
import io.nicheblog.dreamdiary.feature.journal.intrpt.model.JournalIntrptDto;
import io.nicheblog.dreamdiary.feature.journal.intrpt.model.JournalIntrptSearchParam;
import io.nicheblog.dreamdiary.feature.journal.intrpt.repository.jpa.JournalIntrptRepository;
import io.nicheblog.dreamdiary.feature.journal.intrpt.repository.mybatis.JournalIntrptMapper;
import io.nicheblog.dreamdiary.feature.journal.intrpt.spec.JournalIntrptSpec;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JournalIntrptService
 * <pre>
 *  저널 일기 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalIntrptService
        implements BaseClsfService<JournalIntrptDto, JournalIntrptDto, Integer, JournalIntrptEntity>, BaseMultipartWritableService<JournalIntrptDto, JournalIntrptDto, Integer, JournalIntrptEntity> {

    @Getter
    private final JournalIntrptRepository repository;
    @Getter
    private final JournalIntrptSpec spec;
    @Getter
    private final JournalIntrptMapstruct mapstruct;
    @Getter
    private final JournalIntrptMapper mapper;

    public JournalIntrptMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalIntrptMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }
    
    private final JournalCacheEvictWorker journalCacheEvictWorker;

    private final ApplicationContext context;
    private JournalIntrptService getSelf() {
        return context.getBean(this.getClass());
    }

    public List<JournalIntrptDto> getListDtoByUser(final String username, final JournalIntrptSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JournalIntrptDto registDto) throws Exception {
        // 정렬 순서 처리
        final Integer lastSortOrder = repository.findLastIndexByJournalDay(registDto.getJournalDreamId()).orElse(0);
        registDto.setSortOrder(lastSortOrder + 1);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JournalIntrptDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_INTRPT);
    }
    
    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체
     * @param modifyEntity - 수정할 객체
     */
    @Override
    public void preModify(final JournalIntrptDto modifyDto, final JournalIntrptEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }

        final boolean isSortOrderChanged = !Objects.equals(modifyDto.getSortOrder(), modifyEntity.getSortOrder());
        modifyDto.setIsSortOrderChanged(isSortOrderChanged);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JournalIntrptDto postDto, final JournalIntrptDto updatedDto) throws Exception {
        // 정렬 순서 재조정
        if (updatedDto.getIsSortOrderChanged()) this.getSelf().reorderSortOrder(updatedDto);

        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_INTRPT);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JournalIntrptDto} -- 조회된 객체
     */
    @Cacheable(value="journalIntrptDtlDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #key)")
    public JournalIntrptDto getDtlDtoWithCacheByUser(final String username, final Integer key) throws Exception {
        final JournalIntrptEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JournalIntrptDto retrieved = mapstruct.toDto(retrievedEntity);
        // 권한 체크
        if (!retrieved.getIsCreatedBy(AuthUtils.requireUsername(username))) throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        return retrieved;
    }

    @Transactional
    public JournalIntrptDto updtContent(
            final Integer key,
            final String updatedCn,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        final JournalIntrptEntity restoreEntity = this.getSelf().getDtlEntity(key);
        final JournalIntrptEntity historySnapshot = BaseClsfHistoryHelper.isHistoryModule(restoreEntity)
                ? restoreEntity.toBuilder().build()
                : null;

        restoreEntity.setContent(updatedCn);
        BaseClsfHistoryHelper.applyModifyHistory(historySnapshot, restoreEntity);

        final JournalIntrptEntity updatedEntity = getRepository().saveAndFlush(restoreEntity);
        BaseClsfHistoryHelper.publishHistoryEventIfSupported(this, historySnapshot, updatedEntity, historyType, fromHistoryId);

        final JournalIntrptDto updatedDto = getReadMapstruct().toDto(updatedEntity);
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_INTRPT);
        return updatedDto;
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JournalIntrptDto deletedDto) throws Exception {
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
    public void postDelete(final JournalIntrptDto deletedDto) throws Exception {
        // 정렬 순서 재조정
        this.getSelf().reorderSortOrder(deletedDto);

        // 태그 처리 :: 메인 로직과 분리
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_INTRPT);
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JournalIntrptDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JournalIntrptDto getDeletedDtlDto(final Integer key) throws Exception {
        final JournalIntrptDto deleted = mapper.getDeletedById(key);
        if (deleted == null) return null;
        if (!AuthUtils.isCreatedBy(deleted.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return deleted;
    }
    
    /**
     * 해당 그룹 전체를 sortOrder = 1부터 다시 정렬한다.
     */
    @Transactional
    public void normalizeSortOrder(final Integer journalDreamId) {
        final List<JournalIntrptDto> list = mapper.findAllForReorder(journalDreamId);
        if (CollectionUtils.isEmpty(list)) return;

        int sortOrder = 1;
        for (final JournalIntrptDto e : list) {
            e.setSortOrder(sortOrder++);
            EhCacheUtils.evictUserCacheByKey("journalIntrptDtlDtoByUser", e.getCreatedBy(), e.getId());
        }

        mapper.batchUpdateIdx(list);
    }
    
    /**
     * 대상 상위 키에 엔티티를 특정 위치에 삽입 후 재정렬한다.
     *
     * @param journalDreamId 정렬을 수행할 상위 키
     * @param id 게시물 PK
     * @param targetSortOrder 삽입할 목표 위치(1-based). null이면 맨 뒤에 삽입됨
     */
    @Transactional
    public void insert(final Integer journalDreamId, final Integer id, Integer targetSortOrder) throws Exception {
        final List<JournalIntrptDto> list = mapper.findAllForReorder(journalDreamId);

        // target 조회
        final JournalIntrptEntity targetEntity = findDtlEntity(id);
        if (targetEntity == null) return;
        final JournalIntrptDto target = mapstruct.toDto(targetEntity);

        // 혹시 이미 포함되어 있으면 제거
        list.removeIf(e -> Objects.equals(e.getId(), id));

        // chapterNo 변경
        target.setJournalDreamId(journalDreamId);

        // targetSortOrder 보정 (upper bound)
        final int maxIdx = list.size() + 1;
        final int normalizedIdx = Math.min(targetSortOrder == null ? maxIdx : targetSortOrder, maxIdx);
        // 삽입 위치 계산
        int pos = normalizedIdx - 1;
        pos = Math.min(pos, list.size());
        list.add(pos, target);

        // sortOrder 재정렬
        int sortOrder = 1;
        for (final JournalIntrptDto e : list) {
            e.setSortOrder(sortOrder++);
            EhCacheUtils.evictUserCacheByKey("journalIntrptDtlDtoByUser", e.getCreatedBy(), e.getId());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * 정렬 순서 변경 시 관련 순서를 업데이트
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderSortOrder(final JournalIntrptDto updatedDto) throws Exception {
        // 1단계: 현재 chapter 그룹 정리 (기존 sortOrder 값을 normalize하여 안정화)
        normalizeSortOrder(updatedDto.getJournalDreamId());
        // 2단계: 해당 group에 새 위치로 target 삽입
        insert(updatedDto.getJournalDreamId(), updatedDto.getId(), updatedDto.getSortOrder());
    }

    /**
     * 주어진 {@link JournalDayDto} 객체에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param journalIntrpt 공휴일 및 주말 정보를 설정할 대상 DTO
     * @param holydayMap 날짜(String: yyyy-MM-dd) → 공휴일 이름 목록 매핑 정보
     */
    private void setHolydayInfo(final JournalIntrptDto journalIntrpt, final Map<String, List<String>> holydayMap) throws Exception {
        if (journalIntrpt == null || holydayMap == null) return;

        final String stdrdDt = journalIntrpt.getStdrdDt();
        final boolean isHolyday = holydayMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        journalIntrpt.setIsHolyday(isHolyday || isWeekend);
        if (isHolyday) {
            final String concatHolydayNm = String.join(", ", holydayMap.get(stdrdDt));
            journalIntrpt.setHolydayNm(concatHolydayNm);
        }
    }
}


