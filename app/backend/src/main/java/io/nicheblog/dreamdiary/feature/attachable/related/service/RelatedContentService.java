package io.nicheblog.dreamdiary.feature.attachable.related.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.related.entity.RelatedContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.related.mapstruct.RelatedContentMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.related.repository.jpa.RelatedContentRepository;
import io.nicheblog.dreamdiary.feature.attachable.related.type.RelationOriginType;
import io.nicheblog.dreamdiary.feature.attachable.related.type.RelationType;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayResolvedGuard;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryService;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * RelatedContentService
 * <pre>
 *  관련글 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
public class RelatedContentService {

    private static final EnumSet<ContentType> SUPPORTED_TYPES = EnumSet.of(
            ContentType.JOURNAL_DIARY,
            ContentType.JOURNAL_DREAM
    );

    @Getter
    private final RelatedContentRepository repository;
    @Getter
    private final RelatedContentMapstruct mapstruct;

    private final JournalEntryService journalEntryService;
    private final JournalDayResolvedGuard journalDayResolvedGuard;

    public RelatedContentService(
            final RelatedContentRepository repository,
            final RelatedContentMapstruct mapstruct,
            final @Lazy JournalEntryService journalEntryService,
            final JournalDayResolvedGuard journalDayResolvedGuard
    ) {
        this.repository = repository;
        this.mapstruct = mapstruct;
        this.journalEntryService = journalEntryService;
        this.journalDayResolvedGuard = journalDayResolvedGuard;
    }

    @Transactional
    public RelatedContentDto saveManualRelation(
            final Integer firstId,
            final ContentType firstContentType,
            final Integer secondId,
            final ContentType secondContentType,
            final RelationType relationType,
            final String reason
    ) throws Exception {
        return this.saveRelation(
                new BaseAttachableKey(firstId, firstContentType),
                new BaseAttachableKey(secondId, secondContentType),
                relationType,
                StringUtils.trimToNull(reason),
                RelationOriginType.MANUAL
        );
    }

    @Transactional
    public RelatedContentDto saveRelation(
            final BaseAttachableKey firstKey,
            final BaseAttachableKey secondKey,
            final RelationType relationType,
            final String reason,
            final RelationOriginType originType
    ) throws Exception {
        if (relationType == null) {
            throw new IllegalArgumentException("relationType is required.");
        }

        final String createdBy = AuthUtils.requireLoginUsername();
        this.validateWritablePair(firstKey, secondKey);

        final BaseAttachableKey[] normalizedKeys = this.normalizePair(firstKey, secondKey);
        final BaseAttachableKey leftKey = normalizedKeys[0];
        final BaseAttachableKey rightKey = normalizedKeys[1];

        final RelatedContentEntity entity = repository.findAnyByPair(
                        leftKey.getId(),
                        leftKey.getContentType(),
                        rightKey.getId(),
                        rightKey.getContentType(),
                        createdBy
                )
                .map(found -> {
                    found.setDeletedAt(null);
                    found.setRelationType(relationType.key);
                    found.setReason(reason);
                    found.setOriginType(originType != null ? originType.key : RelationOriginType.MANUAL.key);
                    return found;
                })
                .orElseGet(() -> new RelatedContentEntity(leftKey, rightKey, relationType, reason, originType));

        final RelatedContentEntity savedEntity = repository.saveAndFlush(entity);
        return this.toDto(savedEntity, firstKey);
    }

    @Transactional(readOnly = true)
    public List<RelatedContentDto> getListDtoByRef(final Integer id, final ContentType contentType) throws Exception {
        return this.getListDtoByRef(new BaseAttachableKey(id, contentType));
    }

    @Transactional(readOnly = true)
    public List<RelatedContentDto> getListDtoByRef(final BaseAttachableKey refKey) throws Exception {
        this.validateReadableKey(refKey);
        this.requireOwnedContent(refKey);

        final String createdBy = AuthUtils.requireLoginUsername();
        final List<RelatedContentEntity> entityList = repository.findAllByRef(refKey.getId(), refKey.getContentType(), createdBy);

        return entityList.stream()
                .map(entity -> {
                    try {
                        return this.toDto(entity, refKey);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    @Transactional
    public void deleteAllByRef(final Integer id, final ContentType contentType) throws Exception {
        this.deleteAllByRef(new BaseAttachableKey(id, contentType));
    }

    @Transactional
    public void deleteAllByRef(final BaseAttachableKey refKey) throws Exception {
        this.validateReadableKey(refKey);
        this.requireOwnedContent(refKey);
        journalDayResolvedGuard.assertWritableForRef(refKey.getId(), refKey.getContentType());

        final String createdBy = AuthUtils.requireLoginUsername();
        repository.softDeleteAllByRef(refKey.getId(), refKey.getContentType(), createdBy);
    }

    /**
     * 엔트리 삭제 후처리용 관련글 정리.
     * 관련글 도메인 밖 타입({@code JOURNAL_REFLECTION} 등)은 연결을 만들지 않으므로 no-op 한다.
     * 공개 API {@link #deleteAllByRef(BaseAttachableKey)} 의 지원 타입 검증과 달리, 삭제 경로를 막지 않는다.
     *
     * @param refKey 삭제된 콘텐츠 키
     * @param createdBy 등록자 아이디
     */
    @Transactional
    public void deleteAllByRef(final BaseAttachableKey refKey, final String createdBy) {
        if (refKey == null || refKey.getId() == null || StringUtils.isBlank(refKey.getContentType())) {
            throw new IllegalArgumentException("related content key is required.");
        }
        final ContentType contentType = refKey.getContentTypeEnum();
        if (!SUPPORTED_TYPES.contains(contentType)) {
            return;
        }

        this.validateReadableKey(refKey);
        journalDayResolvedGuard.assertWritableForRef(refKey.getId(), refKey.getContentType());

        final String requiredCreatedBy = AuthUtils.requireUsername(createdBy);
        if (!AuthUtils.isCreatedBy(requiredCreatedBy)) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }

        repository.softDeleteAllByRef(refKey.getId(), refKey.getContentType(), requiredCreatedBy);
    }

    @Transactional
    public boolean delete(final Integer relatedContentId) {
        final RelatedContentEntity entity = repository.findById(relatedContentId)
                .orElseThrow(() -> new EntityNotFoundException("this.to-delete"));

        if (!AuthUtils.isCreatedBy(entity.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }

        journalDayResolvedGuard.assertWritableForRef(entity.getLeftId(), entity.getLeftContentType());
        journalDayResolvedGuard.assertWritableForRef(entity.getRightId(), entity.getRightContentType());

        repository.delete(entity);
        return true;
    }

    private RelatedContentDto toDto(final RelatedContentEntity entity, final BaseAttachableKey refKey) throws Exception {
        final RelatedContentDto dto = mapstruct.toDto(entity);
        final BaseAttachableKey targetKey = this.resolveTargetKey(entity, refKey);
        dto.setTargetId(targetKey.getId());
        dto.setTargetContentType(targetKey.getContentType());
        dto.setTargetTitle(this.resolveTitle(targetKey));
        return dto;
    }

    private BaseAttachableKey resolveTargetKey(final RelatedContentEntity entity, final BaseAttachableKey refKey) {
        final boolean isLeft = Objects.equals(entity.getLeftId(), refKey.getId())
                && Objects.equals(entity.getLeftContentType(), refKey.getContentType());

        if (isLeft) return new BaseAttachableKey(entity.getRightId(), entity.getRightContentType());
        return new BaseAttachableKey(entity.getLeftId(), entity.getLeftContentType());
    }

    private void validateWritablePair(final BaseAttachableKey firstKey, final BaseAttachableKey secondKey) {
        this.validateReadableKey(firstKey);
        this.validateReadableKey(secondKey);

        if (Objects.equals(firstKey.getId(), secondKey.getId())
                && Objects.equals(firstKey.getContentType(), secondKey.getContentType())) {
            throw new IllegalArgumentException("self relation is not allowed.");
        }

        final String firstCreatedBy = this.requireOwnedContent(firstKey);
        final String secondCreatedBy = this.requireOwnedContent(secondKey);
        if (!Objects.equals(firstCreatedBy, secondCreatedBy)) {
            throw new IllegalArgumentException("related contents must have same owner.");
        }

        journalDayResolvedGuard.assertWritableForRef(firstKey.getId(), firstKey.getContentType());
        journalDayResolvedGuard.assertWritableForRef(secondKey.getId(), secondKey.getContentType());
    }

    private void validateReadableKey(final BaseAttachableKey refKey) {
        if (refKey == null || refKey.getId() == null || StringUtils.isBlank(refKey.getContentType())) {
            throw new IllegalArgumentException("related content key is required.");
        }

        final ContentType contentType = refKey.getContentTypeEnum();
        if (!SUPPORTED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("unsupported contentType: " + refKey.getContentType());
        }
    }

    /**
     * 지원 콘텐츠 타입과 로그인 사용자 소유권을 검증한다.
     *
     * @param refKey 검증할 콘텐츠 키
     * @return 콘텐츠 등록자 아이디
     */
    String requireOwnedContent(final BaseAttachableKey refKey) {
        this.validateReadableKey(refKey);
        final String createdBy = this.resolveCreatedBy(refKey);
        if (StringUtils.isBlank(createdBy)) {
            throw new EntityNotFoundException("this.to-read");
        }
        if (!AuthUtils.isCreatedBy(createdBy)) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
        return createdBy;
    }

    private String resolveCreatedBy(final BaseAttachableKey refKey) {
        return journalEntryService.resolveCreatedBy(refKey);
    }

    private String resolveTitle(final BaseAttachableKey refKey) {
        return journalEntryService.resolveTitle(refKey);
    }

    private BaseAttachableKey[] normalizePair(final BaseAttachableKey firstKey, final BaseAttachableKey secondKey) {
        if (this.compareKey(firstKey, secondKey) <= 0) {
            return new BaseAttachableKey[]{firstKey, secondKey};
        }
        return new BaseAttachableKey[]{secondKey, firstKey};
    }

    private int compareKey(final BaseAttachableKey firstKey, final BaseAttachableKey secondKey) {
        final int contentTypeResult = firstKey.getContentType().compareTo(secondKey.getContentType());
        if (contentTypeResult != 0) return contentTypeResult;
        return Integer.compare(firstKey.getId(), secondKey.getId());
    }
}
