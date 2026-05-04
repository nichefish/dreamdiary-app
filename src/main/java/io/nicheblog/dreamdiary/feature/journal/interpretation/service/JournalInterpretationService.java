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
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
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
 *   Journal interpretation management service.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalInterpretationService
        implements BaseAttachableService<JournalInterpretationDto, JournalInterpretationDto, Integer, JournalInterpretationEntity>,
        BaseMultipartWritableService<JournalInterpretationDto, JournalInterpretationDto, Integer, JournalInterpretationEntity> {

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

    private final JournalEntryService journalEntryService;
    private final JournalChapterRepository journalChapterRepository;
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
     * Registration pre-processing.
     *
     * @param registDto registration dto
     */
    @Override
    public void preRegist(final JournalInterpretationDto registDto) throws Exception {
        this.applyJournalDayIdFromRef(registDto);

        final Integer lastSortOrder = repository.findLastIndexByRef(registDto.getRefId(), registDto.getRefContentType()).orElse(0);
        registDto.setSortOrder(lastSortOrder + 1);
    }

    /**
     * Registration post-processing.
     *
     * @param updatedDto saved dto
     */
    @Override
    public void postRegist(final JournalInterpretationDto updatedDto) throws Exception {
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_INTERPRETATION);
    }

    /**
     * Modify pre-processing.
     *
     * @param modifyDto modify dto
     * @param modifyEntity existing entity
     */
    @Override
    public void preModify(final JournalInterpretationDto modifyDto, final JournalInterpretationEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }

        this.applyJournalDayIdFromRef(modifyDto);

        final boolean isSortOrderChanged = !Objects.equals(modifyDto.getSortOrder(), modifyEntity.getSortOrder());
        modifyDto.setIsSortOrderChanged(isSortOrderChanged);
    }

    /**
     * Modify post-processing.
     *
     * @param postDto request dto
     * @param updatedDto updated dto
     */
    @Override
    public void postModify(final JournalInterpretationDto postDto, final JournalInterpretationDto updatedDto) throws Exception {
        if (updatedDto.getIsSortOrderChanged()) this.getSelf().reorderSortOrder(updatedDto);

        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_INTERPRETATION);
    }

    /**
     * Cached detail dto lookup by user.
     *
     * @param username username
     * @param key interpretation id
     * @return detail dto
     */
    @Cacheable(value = "journalInterpretationDtlDtoByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #key)")
    public JournalInterpretationDto getDtlDtoWithCacheByUser(final String username, final Integer key) throws Exception {
        final JournalInterpretationEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JournalInterpretationDto retrieved = mapstruct.toDto(retrievedEntity);
        if (!retrieved.getIsCreatedBy(AuthUtils.requireUsername(username))) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
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
     * Delete pre-processing.
     *
     * @param deletedDto delete target dto
     */
    @Override
    public void preDelete(final JournalInterpretationDto deletedDto) throws Exception {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * Delete post-processing.
     *
     * @param deletedDto deleted dto
     */
    @Override
    public void postDelete(final JournalInterpretationDto deletedDto) throws Exception {
        this.getSelf().reorderSortOrder(deletedDto);
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_INTERPRETATION);
    }

    /**
     * Retrieve deleted detail dto.
     *
     * @param key deleted id
     * @return deleted dto
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
     * Normalize sort order within a ref group.
     *
     * @param refId ref id
     * @param refContentType ref content type
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
     * Insert target into a specific position within a ref group.
     *
     * @param refId ref id
     * @param refContentType ref content type
     * @param id target interpretation id
     * @param targetSortOrder 1-based target position
     */
    @Transactional
    public void insert(final Integer refId, final ContentType refContentType, final Integer id, Integer targetSortOrder) throws Exception {
        final List<JournalInterpretationDto> list = mapper.findAllForReorder(refId, refContentType);

        final JournalInterpretationEntity targetEntity = findDtlEntity(id);
        if (targetEntity == null) return;
        final JournalInterpretationDto target = mapstruct.toDto(targetEntity);

        list.removeIf(e -> Objects.equals(e.getId(), id));

        target.setRefId(refId);
        target.setRefContentType(refContentType);

        final int maxIdx = list.size() + 1;
        final int normalizedIdx = Math.min(targetSortOrder == null ? maxIdx : targetSortOrder, maxIdx);
        int pos = normalizedIdx - 1;
        pos = Math.min(pos, list.size());
        list.add(pos, target);

        int sortOrder = 1;
        for (final JournalInterpretationDto e : list) {
            e.setSortOrder(sortOrder++);
            EhCacheUtils.evictUserCacheByKey("journalInterpretationDtlDtoByUser", e.getCreatedBy(), e.getId());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * Reorder sort order for updated dto.
     *
     * @param updatedDto updated dto
     */
    @Transactional
    public void reorderSortOrder(final JournalInterpretationDto updatedDto) throws Exception {
        normalizeSortOrder(updatedDto.getRefId(), updatedDto.getRefContentType());
        insert(updatedDto.getRefId(), updatedDto.getRefContentType(), updatedDto.getId(), updatedDto.getSortOrder());
    }

    /**
     * Apply holiday/weekend info.
     *
     * @param journalInterpretation target dto
     * @param holydayMap holiday map
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

    private void applyJournalDayIdFromRef(final JournalInterpretationDto dto) {
        if (dto == null || dto.getRefId() == null || dto.getRefContentType() == null) return;
        dto.setJournalDayId(this.resolveJournalDayIdFromRef(dto.getRefId(), dto.getRefContentType()));
    }

    private Integer resolveJournalDayIdFromRef(final Integer refId, final ContentType refContentType) {
        return journalEntryService.resolveJournalDayId(refId, refContentType);
    }
}
