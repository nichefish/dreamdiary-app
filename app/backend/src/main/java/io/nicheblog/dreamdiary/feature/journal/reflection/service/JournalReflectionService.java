package io.nicheblog.dreamdiary.feature.journal.reflection.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.model.AttachableCacheContext;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalReflectionLifecycleCascade;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
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
    private final JournalEntryService journalEntryService;
    private final JournalDayRepository journalDayRepository;

    public JournalReflectionMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalReflectionMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /**
     * Reflection 단건 상세를 작성자 확인과 함께 조회한다(수정 모달 로드용).
     *
     * @param id Reflection ID
     * @return 표시 DTO. 없으면 {@code null}
     * @throws Exception 조회 중 예외
     */
    @Transactional(readOnly = true)
    public JournalEntryDto getDtlDtoByUser(final Integer id) throws Exception {
        final JournalReflectionEntity entity = this.findDtlEntity(id);
        if (entity == null) return null;
        if (!AuthUtils.isCreatedBy(entity.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
        return mapstruct.toDto(entity);
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
     * 수정 전처리. 작성자 본인만 수정할 수 있다.
     *
     * @param postDto 수정 DTO
     * @param modifyEntity 수정 대상 엔티티
     */
    @Override
    public void preModify(final JournalReflectionPostDto postDto, final JournalReflectionEntity modifyEntity) {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
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
        final JournalDayEntity targetDay = resolveTargetDay(updatedDto.getRefId(), updatedDto.getRefContentType());
        journalCacheEvictWorker.evictAfterCommit(buildEvictParam(updatedDto, targetDay), ContentType.JOURNAL_REFLECTION);
    }

    /**
     * 삭제 전처리. 작성자 본인만 삭제할 수 있다.
     * 이 Reflection 을 대상으로 둔 하위 Reflection 이 있으면 삭제를 거부한다(R→R Block).
     *
     * @param deletedDto 삭제 대상 DTO
     */
    @Override
    public void preDelete(final JournalEntryDto deletedDto) {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
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
        final JournalDayEntity targetDay = resolveTargetDay(deletedDto.getRefId(), deletedDto.getRefContentType());
        journalCacheEvictWorker.evictAfterCommit(buildEvictParam(deletedDto, targetDay), ContentType.JOURNAL_REFLECTION);
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

        Integer dayId = journalEntryService.resolveJournalDayId(refId, refContentType);
        if (dayId == null && refContentType == ContentType.JOURNAL_REFLECTION) {
            // R→R: 부모 Reflection 의 대상 일자로 한 단계 거슬러 올라간다.
            final JournalReflectionEntity parent = repository.findById(refId).orElse(null);
            if (parent != null) {
                dayId = journalEntryService.resolveJournalDayId(parent.getRefId(), parent.getRefContentType());
            }
        }
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
}
