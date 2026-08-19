package io.nicheblog.dreamdiary.feature.journal.reflection.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.AttachableCacheContext;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalReflectionLifecycleCascade;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal._shared.security.JournalContentOwnershipGuard;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.mapstruct.JournalReflectionMapstruct;
import io.nicheblog.dreamdiary.feature.journal.reflection.model.JournalReflectionPostDto;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import io.nicheblog.dreamdiary.feature.journal.reflection.spec.JournalReflectionSpec;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Reflection(Commentary) 쓰기 서비스.
 *
 * <p>Reflection 은 별도 Aggregate({@code journal_reflection})이라 Entry 와 분리된 쓰기 경로를 갖는다.
 * 대상 필수(About-A) 검증, 대상 RESOLVED 재개 연쇄, 대상 일자 기준 캐시 무효화를 담당한다. 표시는
 * 대상 엔트리 embed 로 실린다(읽기 경로는 {@code JournalEntryReflectionEnricher}).</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalReflectionService
        implements BaseAttachableService<JournalReflectionPostDto, JournalEntryDto, Integer, JournalReflectionEntity>,
        BaseMultipartWritableService<JournalReflectionPostDto, JournalEntryDto, Integer, JournalReflectionEntity> {

    @Getter
    private final JournalReflectionRepository repository;
    @Getter
    private final JournalReflectionSpec spec = new JournalReflectionSpec();

    private final JournalReflectionMapstruct mapstruct;
    private final JournalCacheEvictWorker journalCacheEvictWorker;
    private final JournalReflectionLifecycleCascade journalReflectionLifecycleCascade;
    private final JournalDayRepository journalDayRepository;
    private final JournalContentOwnershipGuard journalContentOwnershipGuard;
    private final io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService lifecycleService;
    private final JournalReflectionOrderService journalReflectionOrderService;

    public JournalReflectionMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalReflectionMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /**
     * Reflection 단건 상세를 대상 일자 소유 확인과 함께 조회한다(수정 모달 로드용).
     *
     * @param id Reflection ID
     * @return 표시 DTO. 없으면 {@code null}
     * @throws Exception 조회 중 예외
     */
    @Transactional(readOnly = true)
    public JournalEntryDto getDtlDtoByUser(final Integer id) throws Exception {
        final JournalReflectionEntity entity = this.findDtlEntity(id);
        if (entity == null) return null;
        journalContentOwnershipGuard.assertOwned(id, ContentType.JOURNAL_REFLECTION);
        final JournalEntryDto dto = mapstruct.toDto(entity);
        journalContentOwnershipGuard.applyReflectionViewerOwnership(List.of(dto));
        return dto;
    }

    /**
     * 등록 전처리. Reflection 은 대상 필수(About-A)이므로 refId·refContentType 을 검증한다.
     *
     * @param registDto 등록 DTO
     */
    @Override
    public void preRegist(final JournalReflectionPostDto registDto) {
        if (registDto.getRefId() == null || registDto.getRefContentType() == null) {
            throw new BusinessException("journal.reflection.target-required");
        }
        registDto.setSortOrder(journalReflectionOrderService.getNextSortOrder(
                registDto.getRefId(), registDto.getRefContentType()));
    }

    /**
     * 등록 후처리. 대상 RESOLVED 재개 연쇄와 대상 일자 기준 캐시 무효화를 수행한다.
     *
     * @param updatedDto 등록 결과 DTO
     * @throws Exception 후처리 중 예외
     */
    @Override
    public void postRegist(final JournalEntryDto updatedDto) throws Exception {
        final JournalDayEntity targetDay = resolveTargetDay(updatedDto.getRefId(), updatedDto.getRefContentType());
        journalCacheEvictWorker.evictAfterCommit(buildEvictParam(updatedDto, targetDay), ContentType.JOURNAL_REFLECTION);

        // 등록된 Reflection 은 기본 PENDING(접힘) 상태로 시작한다.
        lifecycleService.set(io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.LifecycleSetDto.builder()
                .id(updatedDto.getId())
                .contentType(ContentType.JOURNAL_REFLECTION)
                .lifecycleKey(io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey.PENDING)
                .build());

        // RESOLVED primary 에 Reflection 을 묶으면 primary 를 OPEN 으로 재개한다. (REFLECTION_ONE_TYPE §5)
        if (updatedDto.getRefId() != null && updatedDto.getRefContentType() != null) {
            final AttachableCacheContext cacheContext = AttachableCacheContext.builder()
                    .yy(targetDay != null ? targetDay.getYy() : null)
                    .mnth(targetDay != null ? targetDay.getMnth() : null)
                    .build();
            journalReflectionLifecycleCascade.reopenPrimaryTargetIfResolved(
                    updatedDto.getRefId(),
                    updatedDto.getRefContentType(),
                    cacheContext
            );
        }
    }

    /**
     * 수정 전처리. 대상 일자 소유자만 수정할 수 있다.
     *
     * @param postDto 수정 DTO
     * @param modifyEntity 수정 대상 엔티티
     */
    @Override
    public void preModify(final JournalReflectionPostDto postDto, final JournalReflectionEntity modifyEntity) {
        journalContentOwnershipGuard.assertOwned(modifyEntity.getId(), ContentType.JOURNAL_REFLECTION);
        postDto.setIsSortOrderChanged(isSortOrderChanged(postDto, modifyEntity));
    }

    /**
     * 수정 후처리. 대상 일자 기준 캐시를 무효화한다.
     *
     * @param postDto 수정 요청 DTO
     * @param updatedDto 수정 결과 DTO
     * @throws Exception 후처리 중 예외
     */
    @Override
    public void postModify(final JournalReflectionPostDto postDto, final JournalEntryDto updatedDto) throws Exception {
        if (Boolean.TRUE.equals(postDto.getIsSortOrderChanged())) {
            journalReflectionOrderService.reorderSortOrder(
                    updatedDto.getRefId(),
                    updatedDto.getId(),
                    postDto.getSortOrder(),
                    updatedDto.getRefContentType()
            );
        }
        final JournalDayEntity targetDay = resolveTargetDay(updatedDto.getRefId(), updatedDto.getRefContentType());
        journalCacheEvictWorker.evictAfterCommit(buildEvictParam(updatedDto, targetDay), ContentType.JOURNAL_REFLECTION);
    }

    /**
     * 삭제 전처리. 대상 일자 소유자만 삭제할 수 있다.
     * 이 Reflection 을 대상으로 둔 하위 Reflection 이 있으면 삭제를 거부한다(R→R Block).
     *
     * @param deletedDto 삭제 대상 DTO
     */
    @Override
    public void preDelete(final JournalEntryDto deletedDto) {
        journalContentOwnershipGuard.assertOwned(deletedDto.getId(), ContentType.JOURNAL_REFLECTION);
        if (repository.existsByRefIdAndRefContentType(deletedDto.getId(), ContentType.JOURNAL_REFLECTION)) {
            log.warn("[JournalReflection] 삭제 Block — 하위 Reflection 존재. id={}", deletedDto.getId());
            throw new BusinessException("journal.reflection.delete.blocked-by-child");
        }
    }

    /**
     * 삭제 후처리. 대상 일자 기준 캐시를 무효화한다.
     *
     * @param deletedDto 삭제 결과 DTO
     * @throws Exception 후처리 중 예외
     */
    @Override
    public void postDelete(final JournalEntryDto deletedDto) throws Exception {
        journalReflectionOrderService.normalizeSortOrder(deletedDto.getRefId(), deletedDto.getRefContentType());
        final JournalDayEntity targetDay = resolveTargetDay(deletedDto.getRefId(), deletedDto.getRefContentType());
        journalCacheEvictWorker.evictAfterCommit(buildEvictParam(deletedDto, targetDay), ContentType.JOURNAL_REFLECTION);
    }

    /**
     * 수정 요청 순번이 저장된 순번과 다른지 판별한다. 요청 순번이 없으면 변경이 아니다.
     *
     * @param modifyDto 수정 DTO
     * @param modifyEntity 수정 대상 엔티티
     * @return 변경 여부
     */
    private boolean isSortOrderChanged(final JournalReflectionPostDto modifyDto, final JournalReflectionEntity modifyEntity) {
        if (modifyDto.getSortOrder() == null) return false;
        return !Objects.equals(modifyDto.getSortOrder(), modifyEntity.getSortOrder());
    }

    /**
     * 대상(About-A) 엔트리가 속한 일자 엔티티를 해석한다. R→R 은 한 단계 위 대상까지 따라간다.
     *
     * @param refId 대상 엔티티 번호
     * @param refContentType 대상 콘텐츠 타입
     * @return 대상 일자 엔티티. 해석 불가 시 {@code null}
     */
    private JournalDayEntity resolveTargetDay(final Integer refId, final ContentType refContentType) {
        if (refId == null || refContentType == null) return null;

        final Integer dayId = journalContentOwnershipGuard.resolveTargetJournalDayId(refId, refContentType);
        if (dayId == null) return null;
        return journalDayRepository.findById(dayId).orElse(null);
    }

    /**
     * 대상 일자 기준 Reflection 캐시 무효화 파라미터를 구성한다.
     *
     * @param reflectionDto Reflection 표시 DTO
     * @param targetDay 대상 일자 엔티티(없으면 일자 키 없이 무효화)
     * @return 캐시 무효화 파라미터
     * @throws Exception 일자 문자열 변환 중 예외
     */
    private JournalCacheEvictParam buildEvictParam(final JournalEntryDto reflectionDto, final JournalDayEntity targetDay) throws Exception {
        return JournalCacheEvictParam.builder()
                .createdBy(reflectionDto.getCreatedBy())
                .contentType(ContentType.JOURNAL_REFLECTION.key)
                .id(reflectionDto.getId())
                .journalDayId(targetDay != null ? targetDay.getId() : null)
                .yy(targetDay != null ? targetDay.getYy() : null)
                .mnth(targetDay != null ? targetDay.getMnth() : null)
                .weekStartDt(targetDay != null ? DateUtils.asStr(targetDay.getWeekStartDt(), DatePtn.DATE) : null)
                .build();
    }

    /**
     * 이력 복원용 본문 수정. 이력 조회·복원 전략({@code JournalReflectionHistoryStrategy})이 호출한다.
     *
     * @param key Reflection ID
     * @param content 복원할 본문
     * @param historyType 이력 타입
     * @param fromHistoryId 원본 이력 ID
     * @return 수정된 Reflection DTO
     * @throws Exception 수정 중 예외
     */
    @Transactional
    public JournalEntryDto updtContent(
            final Integer key,
            final String content,
            final io.nicheblog.dreamdiary.feature.attachable.history.HistoryType historyType,
            final Integer fromHistoryId
    ) throws Exception {
        final JournalReflectionEntity entity = this.getDtlEntity(key);
        journalContentOwnershipGuard.assertOwned(key, ContentType.JOURNAL_REFLECTION);
        final JournalReflectionEntity historySnapshot = entity.toBuilder().build();

        entity.setContent(content);
        io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableHistoryHelper.applyModifyHistory(historySnapshot, entity);

        final JournalReflectionEntity updatedEntity = repository.saveAndFlush(entity);
        io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableHistoryHelper.publishHistoryEventIfSupported(
                this, historySnapshot, updatedEntity, historyType, fromHistoryId
        );

        final JournalEntryDto updatedDto = mapstruct.toDto(updatedEntity);
        journalContentOwnershipGuard.applyReflectionViewerOwnership(List.of(updatedDto));
        final JournalDayEntity targetDay = resolveTargetDay(updatedDto.getRefId(), updatedDto.getRefContentType());
        journalCacheEvictWorker.evictAfterCommit(buildEvictParam(updatedDto, targetDay), ContentType.JOURNAL_REFLECTION);
        return updatedDto;
    }
}
