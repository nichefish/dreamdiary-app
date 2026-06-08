package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableHistoryHelper;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableManagtHelper;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.helper.BaseAttachableProcPostProcessor;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.HistoryType;
import io.nicheblog.dreamdiary.feature.attachable.related.service.RelatedContentService;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.mapstruct.JournalEntryMapstruct;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntrySearchParam;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.mybatis.JournalEntryMapper;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalDreamerFieldHelper;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryPolicyResolver;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypeResolver;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypePolicy;
import io.nicheblog.dreamdiary.feature.journal.entry.spec.JournalEntrySpec;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingQueueService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntryEntityQueueService;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntryEntityRefSyncService;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryService
        implements BaseAttachableService<JournalEntryPostDto, JournalEntryDto, Integer, JournalEntryEntity>,
        BaseMultipartWritableService<JournalEntryPostDto, JournalEntryDto, Integer, JournalEntryEntity> {

    public static final String DTL_CACHE_NAME = "journalEntryDtlDtoByUser";
    public static final String ANNUAL_STATED_LIST_CACHE_NAME = "journalEntryYyAnnualStatedListByUser";

    @Getter
    private final Class<JournalEntryEntity> entityClass = JournalEntryEntity.class;
    @Getter
    private final JournalEntryRepository repository;

    private final JournalEntryMapstruct mapstruct;
    private final JournalEntryMapper journalEntryMapper;
    private final JournalCacheEvictWorker journalCacheEvictWorker;
    private final RelatedContentService relatedContentService;
    private final JournalChapterRepository journalChapterRepository;
    private final JournalEntryOrderService journalEntryOrderService;
    private final JournalEntryPolicyResolver policyResolver;
    private final JournalEntryTypeResolver typeResolver;
    private final JournalEntryEmbeddingQueueService journalEntryEmbeddingQueueService;
    private final JournalEntryEntityQueueService journalEntryEntityQueueService;
    private final JournalEntryEntityRefSyncService journalEntryEntityRefSyncService;

    /**
     * ref(id + contentType) 기반으로 엔트리를 안전 조회한다.
     *
     * @param id 엔트리 ID
     * @param contentType 콘텐츠 타입 문자열
     * @return 조회 결과
     */
    public Optional<JournalEntryEntity> findByRef(final Integer id, final String contentType) {
        if (id == null || StringUtils.isBlank(contentType) || !policyResolver.isEntryType(contentType)) {
            return Optional.empty();
        }
        return repository.findByIdAndContentType(id, contentType);
    }

    /**
     * enum 타입 콘텐츠를 문자열 키로 변환해 ref 조회한다.
     *
     * @param id 엔트리 ID
     * @param contentType 콘텐츠 타입 enum
     * @return 조회 결과
     */
    public Optional<JournalEntryEntity> findByRef(final Integer id, final ContentType contentType) {
        return this.findByRef(id, contentType != null ? contentType.key : null);
    }

    /**
     * 복합 ref key를 사용해 엔트리를 조회한다.
     *
     * @param refKey 복합 키
     * @return 조회 결과
     */
    public Optional<JournalEntryEntity> findByRef(final BaseAttachableKey refKey) {
        if (refKey == null) return Optional.empty();
        return this.findByRef(refKey.getId(), refKey.getContentType());
    }

    /**
     * ref key에 해당하는 엔트리 제목을 반환한다.
     *
     * @param refKey 복합 키
     * @return 제목
     */
    public String resolveTitle(final BaseAttachableKey refKey) {
        return this.findByRef(refKey)
                .map(JournalEntryEntity::getTitle)
                .orElse(null);
    }

    /**
     * ref key에 해당하는 작성자 아이디를 반환한다.
     *
     * @param refKey 복합 키
     * @return 작성자 아이디
     */
    public String resolveCreatedBy(final BaseAttachableKey refKey) {
        return this.findByRef(refKey)
                .map(JournalEntryEntity::getCreatedBy)
                .orElse(null);
    }

    /**
     * ref key 목록을 제목 맵(contentType:id -> title)으로 변환한다.
     *
     * @param refKeyList 복합 키 목록
     * @return 제목 맵
     */
    public Map<String, String> resolveTitleMap(final Collection<BaseAttachableKey> refKeyList) {
        final Map<String, String> titleMap = new LinkedHashMap<>();
        if (refKeyList == null || refKeyList.isEmpty()) return titleMap;

        final Set<Integer> idSet = new LinkedHashSet<>();
        final Set<String> contentTypeSet = new LinkedHashSet<>();
        for (final BaseAttachableKey refKey : refKeyList) {
            if (refKey == null || refKey.getId() == null || !this.isJournalEntryType(refKey.getContentType())) continue;
            idSet.add(refKey.getId());
            contentTypeSet.add(refKey.getContentType());
        }
        if (idSet.isEmpty() || contentTypeSet.isEmpty()) return titleMap;

        repository.findAllByIdInAndContentTypeIn(idSet, contentTypeSet).forEach(entity ->
                titleMap.put(this.toKey(entity.getContentType(), entity.getId()), entity.getTitle())
        );
        return titleMap;
    }

    /**
     * ref 타입(일/챕터/엔트리)에 따라 소속 journalDayId를 해석한다.
     *
     * @param refId 참조 ID
     * @param refContentType 참조 콘텐츠 타입
     * @return journalDayId
     */
    public Integer resolveJournalDayId(final Integer refId, final ContentType refContentType) {
        if (refId == null || refContentType == null) return null;

        return switch (refContentType) {
            case JOURNAL_DAY -> refId;
            case JOURNAL_CHAPTER -> journalChapterRepository.findById(refId)
                    .map(JournalChapterEntity::getJournalDayId)
                    .orElse(null);
            case JOURNAL_DIARY, JOURNAL_DREAM -> this.findByRef(refId, refContentType)
                    .map(entity -> entity.getJournalChapter() != null ? entity.getJournalChapter().getJournalDayId() : null)
                    .orElse(null);
            default -> null;
        };
    }

    /**
     * 문자열 콘텐츠 타입이 엔트리 타입인지 검사한다.
     *
     * @param contentType 콘텐츠 타입 문자열
     * @return 엔트리 타입 여부
     */
    public boolean isJournalEntryType(final String contentType) {
        return policyResolver.isEntryType(contentType);
    }

    /**
     * 콘텐츠 타입 정책을 반영해 엔트리를 등록한다.
     *
     * @param contentType 콘텐츠 타입
     * @param dto 저장 DTO
     * @param request 멀티파트 요청
     * @return 저장 결과
     * @throws Exception 저장 중 예외
     */
    @Transactional
    public ServiceResponse regist(final ContentType contentType, final JournalEntryPostDto dto, final MultipartHttpServletRequest request) throws Exception {
        dto.setContentType(policyResolver.resolve(contentType).contentType.key);
        return BaseMultipartWritableService.super.regist(dto, request);
    }

    /**
     * 콘텐츠 타입 정책을 반영해 엔트리를 수정한다.
     *
     * @param contentType 콘텐츠 타입
     * @param dto 저장 DTO
     * @param request 멀티파트 요청
     * @return 수정 결과
     * @throws Exception 수정 중 예외
     */
    @Transactional
    public ServiceResponse modify(final ContentType contentType, final JournalEntryPostDto dto, final MultipartHttpServletRequest request) throws Exception {
        dto.setContentType(policyResolver.resolve(contentType).contentType.key);
        return BaseMultipartWritableService.super.modify(dto, request);
    }

    /**
     * 챕터 타입 기반으로 엔트리를 등록한다.
     *
     * @param dto 저장 DTO
     * @param request 멀티파트 요청
     * @return 저장 결과
     * @throws Exception 저장 중 예외
     */
    @Transactional
    public ServiceResponse regist(final JournalEntryPostDto dto, final MultipartHttpServletRequest request) throws Exception {
        final ContentType contentType = typeResolver.resolveByChapterId(dto.getJournalChapterId());
        return this.regist(contentType, dto, request);
    }

    /**
     * 엔트리 ID 기반으로 타입을 해석해 수정한다.
     *
     * @param dto 저장 DTO
     * @param request 멀티파트 요청
     * @return 수정 결과
     * @throws Exception 수정 중 예외
     */
    @Transactional
    public ServiceResponse modify(final JournalEntryPostDto dto, final MultipartHttpServletRequest request) throws Exception {
        final ContentType contentType = typeResolver.resolveByEntryId(dto.getId());
        return this.modify(contentType, dto, request);
    }

    /**
     * 사용자 기준 일반 목록을 조회한다.
     *
     * @param username 사용자 아이디
     * @param searchParam 검색 조건
     * @param contentType 콘텐츠 타입
     * @return 목록 DTO
     * @throws Exception 조회 중 예외
     */
    public List<JournalEntryDto> getListDtoByUser(final String username, final JournalEntrySearchParam searchParam, final ContentType contentType) throws Exception {
        final JournalEntryTypePolicy policy = policyResolver.resolve(contentType);
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        searchParam.setContentType(policy.contentType.key);
        return this.getListDto(searchParam);
    }

    /**
     * 사용자 기준 연간 목록을 조회하고 정렬해 반환한다.
     *
     * @param username 사용자 아이디
     * @param searchParam 검색 조건
     * @param contentType 콘텐츠 타입
     * @return 정렬된 연간 목록 DTO
     * @throws Exception 조회 중 예외
     */
    @Cacheable(value = ANNUAL_STATED_LIST_CACHE_NAME, key = "new org.springframework.cache.interceptor.SimpleKey(#username, #contentType.key + '_' + #searchParam.toSummaryCacheKey())")
    public List<JournalEntryDto> getAnnualListDtoByUser(final String username, final JournalEntrySearchParam searchParam, final ContentType contentType) throws Exception {
        final List<JournalEntryDto> list = getListDtoByUser(username, searchParam, contentType);
        Collections.sort(list);
        return list;
    }

    /**
     * 상세 캐시를 사용해 단건을 조회하고 소유권을 검증한다.
     *
     * @param username 사용자 아이디
     * @param key 엔트리 ID
     * @param contentType 콘텐츠 타입
     * @return 상세 DTO
     * @throws Exception 조회 중 예외
     */
    @Cacheable(value = DTL_CACHE_NAME, key = "new org.springframework.cache.interceptor.SimpleKey(#username, #contentType.key + '_' + #key)")
    public JournalEntryDto getDtlDtoWithCacheByUser(final String username, final Integer key, final ContentType contentType) throws Exception {
        final JournalEntryEntity retrievedEntity = this.getDtlEntity(key);
        policyResolver.assertMatches(retrievedEntity, policyResolver.resolve(contentType));
        final JournalEntryDto retrieved = mapstruct.toDto(retrievedEntity);
        if (!retrieved.getIsCreatedBy(AuthUtils.requireUsername(username))) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return retrieved;
    }

    /**
     * 읽기 변환용 mapstruct를 반환한다.
     *
     * @return 읽기 mapstruct
     */
    @Override
    public BaseAttachableMapstruct<JournalEntryDto, JournalEntryEntity> getReadMapstruct() {
        return mapstruct;
    }

    /**
     * 쓰기 변환용 mapstruct를 반환한다.
     *
     * @return 쓰기 mapstruct
     */
    @Override
    public BaseWriteMapstruct<JournalEntryPostDto, JournalEntryEntity> getWriteMapstruct() {
        return mapstruct;
    }

    /**
     * 엔트리 검색용 specification 객체를 생성한다.
     *
     * @return specification 객체
     */
    @Override
    public JournalEntrySpec getSpec() {
        return new JournalEntrySpec();
    }

    /**
     * 등록 전 챕터/정렬 순번 유효성을 설정한다.
     *
     * @param registDto 등록 DTO
     */
    @Override
    public void preRegist(final JournalEntryPostDto registDto) {
        final JournalEntryTypePolicy policy = policyResolver.resolve(registDto);
        assertChapterForEntry(policy, registDto.getJournalChapterId());
        JournalDreamerFieldHelper.applyDreamerFieldsFromPost(registDto, policy.contentType);
        registDto.setSortOrder(journalEntryOrderService.getNextSortOrder(registDto.getJournalChapterId(), policy.contentType));
    }

    /**
     * 등록 직전 엔티티에 꿈꾼 이름·else_dream_yn 을 반영한다.
     *
     * @param registEntity 등록 엔티티
     */
    @Override
    public void preRegist(final JournalEntryEntity registEntity) throws Exception {
        JournalDreamerFieldHelper.applyDreamerFieldsToEntity(registEntity);
    }

    /**
     * 등록 후 관련 캐시를 비동기 무효화한다.
     *
     * @param updatedDto 등록 결과 DTO
     * @throws Exception 후처리 중 예외
     */
    @Override
    public void postRegist(final JournalEntryDto updatedDto) throws Exception {
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), policyResolver.resolve(updatedDto).contentType);
        journalEntryEntityQueueService.queueForEntryId(updatedDto.getKey());
        journalEntryEmbeddingQueueService.queueForEntryId(updatedDto.getKey());
    }

    /**
     * 수정 전 정책/권한/챕터/정렬 변경 여부를 검증한다.
     *
     * @param modifyDto 수정 DTO
     * @param modifyEntity 수정 대상 엔티티
     */
    @Override
    public void preModify(final JournalEntryPostDto modifyDto, final JournalEntryEntity modifyEntity) {
        final JournalEntryTypePolicy policy = policyResolver.resolve(modifyDto);
        JournalDreamerFieldHelper.applyDreamerFieldsFromPost(modifyDto, policy.contentType);
        policyResolver.assertMatches(modifyEntity, policy);
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }

        final Integer journalChapterId = policy.resolveModifiedChapterId(modifyDto.getJournalChapterId(), modifyEntity.getJournalChapter().getId());
        assertChapterForEntry(policy, journalChapterId);
        modifyDto.setIsSortOrderChanged(isSortOrderChanged(modifyDto, modifyEntity));
        if (policy.supportsChapterChange()) {
            final boolean isChapterChanged = isChapterChanged(modifyDto, modifyEntity);
            modifyDto.setIsChapterChanged(isChapterChanged);
            if (isChapterChanged) {
                modifyDto.setPrevJournalChapterId(modifyEntity.getJournalChapter().getId());
            }
        }
    }

    /**
     * 삭제 전 작성자 권한을 검증한다.
     *
     * @param deletedDto 삭제 대상 DTO
     */
    @Override
    public void preDelete(final JournalEntryDto deletedDto) {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 삭제 후 정렬/연관콘텐츠/캐시 후처리를 수행한다.
     *
     * @param deletedDto 삭제 결과 DTO
     * @throws Exception 후처리 중 예외
     */
    @Override
    public void postDelete(final JournalEntryDto deletedDto) throws Exception {
        final JournalEntryTypePolicy policy = policyResolver.resolve(deletedDto);
        journalEntryOrderService.normalizeSortOrder(deletedDto.getJournalChapterId(), policy.contentType, DTL_CACHE_NAME);
        relatedContentService.deleteAllByRef(new BaseAttachableKey(deletedDto.getKey(), policy.contentType), deletedDto.getCreatedBy());
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), policy.contentType);
        journalEntryEntityRefSyncService.removeByJournalEntryId(deletedDto.getKey());
        journalEntryEntityQueueService.removeByJournalEntryId(deletedDto.getKey());
        journalEntryEmbeddingQueueService.removeByJournalEntryId(deletedDto.getKey());
    }

    /**
     * 이력 스냅샷을 포함한 수정 플로우를 직접 수행한다.
     *
     * @param postDto 수정 DTO
     * @return 수정 결과
     * @throws Exception 수정 중 예외
     */
    @Override
    @Transactional
    public ServiceResponse modify(final JournalEntryPostDto postDto) throws Exception {
        final JournalEntryEntity modifyEntity = getDtlEntity(postDto.getKey());
        final JournalEntryEntity historySnapshot = BaseAttachableHistoryHelper.isHistoryModule(modifyEntity)
                ? modifyEntity.toBuilder().build()
                : null;

        this.preModify(postDto);
        this.preModify(postDto, modifyEntity);

        mapstruct.updateFromDto(postDto, modifyEntity);
        JournalDreamerFieldHelper.applyDreamerFieldsToEntity(modifyEntity);
        BaseAttachableManagtHelper.applyModifyManagt(postDto, modifyEntity);
        BaseAttachableHistoryHelper.applyModifyHistory(historySnapshot, modifyEntity);

        final JournalEntryEntity updatedEntity = repository.saveAndFlush(modifyEntity);
        final JournalEntryDto updatedDto = mapstruct.toDto(updatedEntity);

        BaseAttachableProcPostProcessor.afterWrite(postDto, updatedDto);
        final HistoryType historyType = postDto.resolveHistoryType();
        final Integer fromHistoryId = postDto.getFromHistoryId();
        BaseAttachableHistoryHelper.publishHistoryEventIfSupported(this, historySnapshot, updatedEntity, historyType, fromHistoryId);

        this.postModify(postDto, updatedDto);

        final ServiceResponse response = new ServiceResponse();
        response.setRslt(updatedDto != null && updatedDto.getKey() != null);
        response.setRsltObj(updatedDto);
        return response;
    }

    /**
     * 수정 후 챕터/순번 이동 반영 및 캐시 무효화를 수행한다.
     *
     * @param postDto 수정 DTO
     * @param updatedDto 수정 결과 DTO
     * @throws Exception 후처리 중 예외
     */
    @Override
    public void postModify(final JournalEntryPostDto postDto, final JournalEntryDto updatedDto) throws Exception {
        final ContentType contentType = policyResolver.resolve(updatedDto).contentType;
        if (postDto.getIsChapterChanged()) {
            journalEntryOrderService.reorderWhenChapterChanged(
                    postDto.getPrevJournalChapterId(),
                    postDto.getJournalChapterId(),
                    postDto.getId(),
                    postDto.getSortOrder(),
                    contentType,
                    DTL_CACHE_NAME
            );
        } else if (postDto.getIsSortOrderChanged()) {
            journalEntryOrderService.reorderSortOrder(
                    postDto.getJournalChapterId(),
                    postDto.getId(),
                    postDto.getSortOrder(),
                    contentType,
                    DTL_CACHE_NAME
            );
        }
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(postDto, updatedDto), contentType);
        journalEntryEntityQueueService.queueForEntryId(updatedDto.getKey());
        journalEntryEmbeddingQueueService.queueForEntryId(updatedDto.getKey());
    }

    /**
     * 본문만 갱신하는 복구/치환 시나리오를 처리한다.
     *
     * @param key 엔트리 ID
     * @param updatedCn 변경 본문
     * @param historyType 이력 타입
     * @param fromHistoryId 이력 원본 ID
     * @param contentType 콘텐츠 타입
     * @return 수정 결과 DTO
     * @throws Exception 수정 중 예외
     */
    @Transactional
    public JournalEntryDto updtContent(
            final Integer key,
            final String updatedCn,
            final HistoryType historyType,
            final Integer fromHistoryId,
            final ContentType contentType
    ) throws Exception {
        final JournalEntryTypePolicy policy = policyResolver.resolve(contentType);
        final JournalEntryEntity restoreEntity = this.getDtlEntity(key);
        policyResolver.assertMatches(restoreEntity, policy);
        final JournalEntryEntity historySnapshot = BaseAttachableHistoryHelper.isHistoryModule(restoreEntity)
                ? restoreEntity.toBuilder().build()
                : null;

        restoreEntity.setContent(updatedCn);
        BaseAttachableHistoryHelper.applyModifyHistory(historySnapshot, restoreEntity);

        final JournalEntryEntity updatedEntity = repository.saveAndFlush(restoreEntity);
        BaseAttachableHistoryHelper.publishHistoryEventIfSupported(this, historySnapshot, updatedEntity, historyType, fromHistoryId);

        final JournalEntryDto updatedDto = mapstruct.toDto(updatedEntity);
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), policy.contentType);
        journalEntryEntityQueueService.queueForEntryId(updatedDto.getKey());
        journalEntryEmbeddingQueueService.queueForEntryId(updatedDto.getKey());
        return updatedDto;
    }

    /**
     * 소프트 삭제된 엔트리 상세를 조회하고 권한을 검증한다.
     *
     * @param key 엔트리 ID
     * @param contentType 콘텐츠 타입
     * @return 삭제 상세 DTO
     * @throws Exception 조회 중 예외
     */
    @Transactional(readOnly = true)
    public JournalEntryDto getDeletedDtlDto(final Integer key, final ContentType contentType) throws Exception {
        final JournalEntryDto deleted = journalEntryMapper.getDeletedByIdAndContentType(key, policyResolver.resolve(contentType).contentType.key);
        if (deleted == null) return null;
        if (!AuthUtils.isCreatedBy(deleted.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return deleted;
    }

    /**
     * 콘텐츠 타입을 검증한 뒤 엔트리를 삭제한다.
     *
     * @param id 엔트리 ID
     * @param contentType 콘텐츠 타입
     * @return 삭제 결과
     * @throws Exception 삭제 중 예외
     */
    @Transactional
    public ServiceResponse delete(final Integer id, final ContentType contentType) throws Exception {
        final JournalEntryEntity deleteEntity = this.getDtlEntity(id);
        policyResolver.assertMatches(deleteEntity, policyResolver.resolve(contentType));
        if (deleteEntity == null) {
            throw new EntityNotFoundException("exception.EntityNotFoundException.to-delete");
        }

        final JournalEntryDto deletedDto = mapstruct.toDto(deleteEntity);
        this.preDelete(deletedDto);
        this.remove(deleteEntity);
        BaseAttachableProcPostProcessor.afterDelete(deletedDto);
        this.postDelete(deletedDto);

        final ServiceResponse response = new ServiceResponse();
        response.setRslt(true);
        response.setRsltObj(deletedDto);
        return response;
    }

    /**
     * 엔트리 ID 기반으로 타입을 해석해 삭제한다.
     *
     * @param id 엔트리 ID
     * @return 삭제 결과
     * @throws Exception 삭제 중 예외
     */
    @Transactional
    public ServiceResponse delete(final Integer id) throws Exception {
        final ContentType contentType = typeResolver.resolveByEntryId(id);
        return this.delete(id, contentType);
    }

    /**
     * 정책에 맞는 챕터 타입인지 확인한다.
     *
     * @param policy 엔트리 정책
     * @param journalChapterId 챕터 ID
     */
    private void assertChapterForEntry(final JournalEntryTypePolicy policy, final Integer journalChapterId) {
        final JournalChapterEntity chapter = journalChapterRepository.findById(journalChapterId)
                .orElseThrow(() -> new BusinessException("msg.journal.chapter.not-found"));
        if (chapter.getChapterType() == ChapterType.NOTE
                && policy.contentType == ContentType.JOURNAL_DIARY) {
            return;
        }
        if (chapter.getChapterType() != policy.expectedChapterType) {
            throw new BusinessException("msg.journal.entry.invalid-chapter-type");
        }
    }

    /**
     * 정렬 순번이 실제로 변경되었는지 판별한다.
     *
     * @param modifyDto 수정 DTO
     * @param modifyEntity 수정 대상 엔티티
     * @return 변경 여부
     */
    private boolean isSortOrderChanged(final JournalEntryPostDto modifyDto, final JournalEntryEntity modifyEntity) {
        return !Objects.equals(modifyDto.getSortOrder(), modifyEntity.getSortOrder());
    }

    /**
     * 챕터 이동 여부를 판별한다.
     *
     * @param modifyDto 수정 DTO
     * @param modifyEntity 수정 대상 엔티티
     * @return 이동 여부
     */
    private boolean isChapterChanged(final JournalEntryPostDto modifyDto, final JournalEntryEntity modifyEntity) {
        return !Objects.equals(modifyDto.getJournalChapterId(), modifyEntity.getJournalChapter().getId());
    }

    /**
     * 캐시 키/맵 키 공통 포맷을 생성한다.
     *
     * @param contentType 콘텐츠 타입 문자열
     * @param id 엔트리 ID
     * @return 조합 키 문자열
     */
    private String toKey(final String contentType, final Integer id) {
        return String.format("%s:%d", StringUtils.defaultString(contentType), id);
    }
}
