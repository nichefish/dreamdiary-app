package io.nicheblog.dreamdiary.feature.journal.interpretation.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableHistoryHelper;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.entity.JournalInterpretationEntity;
import io.nicheblog.dreamdiary.feature.journal.interpretation.mapstruct.JournalInterpretationMapstruct;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationSearchParam;
import io.nicheblog.dreamdiary.feature.journal.interpretation.repository.jpa.JournalInterpretationRepository;
import io.nicheblog.dreamdiary.feature.journal.interpretation.repository.mybatis.JournalInterpretationMapper;
import io.nicheblog.dreamdiary.feature.journal.interpretation.spec.JournalInterpretationSpec;
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
 * JournalInterpretationService
 * <pre>
 *  저널 일기 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalInterpretationService
        implements BaseAttachableService<JournalInterpretationDto, JournalInterpretationDto, Integer, JournalInterpretationEntity>, BaseMultipartWritableService<JournalInterpretationDto, JournalInterpretationDto, Integer, JournalInterpretationEntity> {

    @Getter
    private final JournalInterpretationRepository repository;
    @Getter
    private final JournalInterpretationSpec spec;
    @Getter
    private final JournalInterpretationMapstruct mapstruct;
    @Getter
    private final JournalInterpretationMapper mapper;

    public JournalInterpretationMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalInterpretationMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }
    
    private final JournalCacheEvictWorker journalCacheEvictWorker;

    private final ApplicationContext context;
    private JournalInterpretationService getSelf() {
        return context.getBean(this.getClass());
    }

    public List<JournalInterpretationDto> getListDtoByUser(final String username, final JournalInterpretationSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JournalInterpretationDto registDto) throws Exception {
        // 정렬 순서 처리
        final Integer lastSortOrder = repository.findLastIndexByRef(registDto.getRefId(), registDto.getRefContentType()).orElse(0);
        registDto.setSortOrder(lastSortOrder + 1);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JournalInterpretationDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_INTERPRETATION);
    }
    
    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체
     * @param modifyEntity - 수정할 객체
     */
    @Override
    public void preModify(final JournalInterpretationDto modifyDto, final JournalInterpretationEntity modifyEntity) throws Exception {
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
    public void postModify(final JournalInterpretationDto postDto, final JournalInterpretationDto updatedDto) throws Exception {
        // 정렬 순서 재조정
        if (updatedDto.getIsSortOrderChanged()) this.getSelf().reorderSortOrder(updatedDto);

        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_INTERPRETATION);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JournalInterpretationDto} -- 조회된 객체
     */
    @Cacheable(value="journalInterpretationDtlDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #key)")
    public JournalInterpretationDto getDtlDtoWithCacheByUser(final String username, final Integer key) throws Exception {
        final JournalInterpretationEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JournalInterpretationDto retrieved = mapstruct.toDto(retrievedEntity);
        // 권한 체크
        if (!retrieved.getIsCreatedBy(AuthUtils.requireUsername(username))) throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        return retrieved;
    }

    @Transactional
    public JournalInterpretationDto updtContent(
            final Integer key,
            final String updatedCn,
            final HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        final JournalInterpretationEntity restoreEntity = this.getSelf().getDtlEntity(key);
        final JournalInterpretationEntity historySnapshot = BaseAttachableHistoryHelper.isHistoryModule(restoreEntity)
                ? restoreEntity.toBuilder().build()
                : null;

        restoreEntity.setContent(updatedCn);
        BaseAttachableHistoryHelper.applyModifyHistory(historySnapshot, restoreEntity);

        final JournalInterpretationEntity updatedEntity = getRepository().saveAndFlush(restoreEntity);
        BaseAttachableHistoryHelper.publishHistoryEventIfSupported(this, historySnapshot, updatedEntity, historyType, fromHistoryId);

        final JournalInterpretationDto updatedDto = getReadMapstruct().toDto(updatedEntity);
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_INTERPRETATION);
        return updatedDto;
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JournalInterpretationDto deletedDto) throws Exception {
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
    public void postDelete(final JournalInterpretationDto deletedDto) throws Exception {
        // 정렬 순서 재조정
        this.getSelf().reorderSortOrder(deletedDto);

        // 태그 처리 :: 메인 로직과 분리
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_INTERPRETATION);
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JournalInterpretationDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JournalInterpretationDto getDeletedDtlDto(final Integer key) throws Exception {
        final JournalInterpretationDto deleted = mapper.getDeletedById(key);
        if (deleted == null) return null;
        if (!AuthUtils.isCreatedBy(deleted.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return deleted;
    }
    
    /**
     * 해당 ref 그룹 전체를 sortOrder = 1부터 다시 정렬한다.
     *
     * @param refId 참조 엔티티 번호
     * @param refContentType 참조 컨텐츠 타입
     */
    @Transactional
    public void normalizeSortOrder(final Integer refId, final ContentType refContentType) {
        final List<JournalInterpretationDto> list = mapper.findAllForReorder(refId, refContentType);
        if (CollectionUtils.isEmpty(list)) return;

        int sortOrder = 1;
        for (final JournalInterpretationDto e : list) {
            e.setSortOrder(sortOrder++);
            EhCacheUtils.evictUserCacheByKey("journalInterpretationDtlDtoByUser", e.getCreatedBy(), e.getId());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * 대상 ref 그룹에 엔티티를 특정 위치에 삽입 후 재정렬한다.
     *
     * @param refId 참조 엔티티 번호
     * @param refContentType 참조 컨텐츠 타입
     * @param id 게시물 PK
     * @param targetSortOrder 삽입할 목표 위치(1-based). null이면 맨 뒤에 삽입됨
     */
    @Transactional
    public void insert(final Integer refId, final ContentType refContentType, final Integer id, Integer targetSortOrder) throws Exception {
        final List<JournalInterpretationDto> list = mapper.findAllForReorder(refId, refContentType);

        // target 조회
        final JournalInterpretationEntity targetEntity = findDtlEntity(id);
        if (targetEntity == null) return;
        final JournalInterpretationDto target = mapstruct.toDto(targetEntity);

        // 혹시 이미 포함되어 있으면 제거
        list.removeIf(e -> Objects.equals(e.getId(), id));

        // ref 변경
        target.setRefId(refId);
        target.setRefContentType(refContentType);

        // targetSortOrder 보정 (upper bound)
        final int maxIdx = list.size() + 1;
        final int normalizedIdx = Math.min(targetSortOrder == null ? maxIdx : targetSortOrder, maxIdx);
        // 삽입 위치 계산
        int pos = normalizedIdx - 1;
        pos = Math.min(pos, list.size());
        list.add(pos, target);

        // sortOrder 재정렬
        int sortOrder = 1;
        for (final JournalInterpretationDto e : list) {
            e.setSortOrder(sortOrder++);
            EhCacheUtils.evictUserCacheByKey("journalInterpretationDtlDtoByUser", e.getCreatedBy(), e.getId());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * 정렬 순서 변경 시 관련 순서를 업데이트
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderSortOrder(final JournalInterpretationDto updatedDto) throws Exception {
        // 1단계: 현재 ref 그룹 정리 (기존 sortOrder 값을 normalize하여 안정화)
        normalizeSortOrder(updatedDto.getRefId(), updatedDto.getRefContentType());
        // 2단계: 해당 group에 새 위치로 target 삽입
        insert(updatedDto.getRefId(), updatedDto.getRefContentType(), updatedDto.getId(), updatedDto.getSortOrder());
    }

    /**
     * 주어진 {@link JournalDayDto} 객체에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param journalInterpretation 공휴일 및 주말 정보를 설정할 대상 DTO
     * @param holydayMap 날짜(String: yyyy-MM-dd) → 공휴일 이름 목록 매핑 정보
     */
    private void setHolydayInfo(final JournalInterpretationDto journalInterpretation, final Map<String, List<String>> holydayMap) throws Exception {
        if (journalInterpretation == null || holydayMap == null) return;

        final String stdrdDt = journalInterpretation.getStdrdDt();
        final boolean isHolyday = holydayMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        journalInterpretation.setIsHolyday(isHolyday || isWeekend);
        if (isHolyday) {
            final String concatHolydayNm = String.join(", ", holydayMap.get(stdrdDt));
            journalInterpretation.setHolydayNm(concatHolydayNm);
        }
    }
}


