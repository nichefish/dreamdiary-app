package io.nicheblog.dreamdiary.feature.clsf.related.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.related.entity.RelatedContentEntity;
import io.nicheblog.dreamdiary.feature.clsf.related.mapstruct.RelatedContentMapstruct;
import io.nicheblog.dreamdiary.feature.clsf.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.clsf.related.repository.jpa.RelatedContentRepository;
import io.nicheblog.dreamdiary.feature.clsf.related.type.RelationOriginType;
import io.nicheblog.dreamdiary.feature.clsf.related.type.RelationType;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.repository.jpa.JournalDiaryRepository;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.feature.journal.dream.repository.jpa.JournalDreamRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
@RequiredArgsConstructor
public class RelatedContentService {

    private static final EnumSet<ContentType> SUPPORTED_TYPES = EnumSet.of(
            ContentType.JOURNAL_DIARY,
            ContentType.JOURNAL_DREAM
    );

    @Getter
    private final RelatedContentRepository repository;
    @Getter
    private final RelatedContentMapstruct mapstruct;

    private final JournalDiaryRepository journalDiaryRepository;
    private final JournalDreamRepository journalDreamRepository;

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
                new BaseClsfKey(firstId, firstContentType),
                new BaseClsfKey(secondId, secondContentType),
                relationType,
                StringUtils.trimToNull(reason),
                RelationOriginType.MANUAL
        );
    }

    @Transactional
    public RelatedContentDto saveRelation(
            final BaseClsfKey firstKey,
            final BaseClsfKey secondKey,
            final RelationType relationType,
            final String reason,
            final RelationOriginType originType
    ) throws Exception {
        if (relationType == null) {
            throw new IllegalArgumentException("relationType is required.");
        }

        final String createdBy = AuthUtils.requireLgnUsername();
        this.validateWritablePair(firstKey, secondKey);

        final BaseClsfKey[] normalizedKeys = this.normalizePair(firstKey, secondKey);
        final BaseClsfKey leftKey = normalizedKeys[0];
        final BaseClsfKey rightKey = normalizedKeys[1];

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
        return this.getListDtoByRef(new BaseClsfKey(id, contentType));
    }

    @Transactional(readOnly = true)
    public List<RelatedContentDto> getListDtoByRef(final BaseClsfKey refKey) throws Exception {
        this.validateReadableKey(refKey);
        this.requireOwnedContent(refKey);

        final String createdBy = AuthUtils.requireLgnUsername();
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
        this.deleteAllByRef(new BaseClsfKey(id, contentType));
    }

    @Transactional
    public void deleteAllByRef(final BaseClsfKey refKey) throws Exception {
        this.validateReadableKey(refKey);
        this.requireOwnedContent(refKey);

        final String createdBy = AuthUtils.requireLgnUsername();
        repository.softDeleteAllByRef(refKey.getId(), refKey.getContentType(), createdBy);
    }

    @Transactional
    public void deleteAllByRef(final BaseClsfKey refKey, final String createdBy) {
        this.validateReadableKey(refKey);

        final String requiredCreatedBy = AuthUtils.requireUsername(createdBy);
        if (!AuthUtils.isCreatedBy(requiredCreatedBy)) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }

        repository.softDeleteAllByRef(refKey.getId(), refKey.getContentType(), requiredCreatedBy);
    }

    @Transactional
    public boolean delete(final Integer relatedContentId) {
        final RelatedContentEntity entity = repository.findById(relatedContentId)
                .orElseThrow(() -> new EntityNotFoundException("exception.EntityNotFoundException.to-delete"));

        if (!AuthUtils.isCreatedBy(entity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }

        repository.delete(entity);
        return true;
    }

    private RelatedContentDto toDto(final RelatedContentEntity entity, final BaseClsfKey refKey) throws Exception {
        final RelatedContentDto dto = mapstruct.toDto(entity);
        final BaseClsfKey targetKey = this.resolveTargetKey(entity, refKey);
        dto.setTargetId(targetKey.getId());
        dto.setTargetContentType(targetKey.getContentType());
        dto.setTargetTitle(this.resolveTitle(targetKey));
        return dto;
    }

    private BaseClsfKey resolveTargetKey(final RelatedContentEntity entity, final BaseClsfKey refKey) {
        final boolean isLeft = Objects.equals(entity.getLeftId(), refKey.getId())
                && Objects.equals(entity.getLeftContentType(), refKey.getContentType());

        if (isLeft) return new BaseClsfKey(entity.getRightId(), entity.getRightContentType());
        return new BaseClsfKey(entity.getLeftId(), entity.getLeftContentType());
    }

    private void validateWritablePair(final BaseClsfKey firstKey, final BaseClsfKey secondKey) {
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
    }

    private void validateReadableKey(final BaseClsfKey refKey) {
        if (refKey == null || refKey.getId() == null || StringUtils.isBlank(refKey.getContentType())) {
            throw new IllegalArgumentException("related content key is required.");
        }

        final ContentType contentType = refKey.getContentTypeEnum();
        if (!SUPPORTED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("unsupported contentType: " + refKey.getContentType());
        }
    }

    private String requireOwnedContent(final BaseClsfKey refKey) {
        final String createdBy = this.resolveCreatedBy(refKey);
        if (StringUtils.isBlank(createdBy)) {
            throw new EntityNotFoundException("exception.EntityNotFoundException.to-read");
        }
        if (!AuthUtils.isCreatedBy(createdBy)) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return createdBy;
    }

    private String resolveCreatedBy(final BaseClsfKey refKey) {
        return switch (refKey.getContentTypeEnum()) {
            case JOURNAL_DIARY -> journalDiaryRepository.findById(refKey.getId())
                    .map(JournalDiaryEntity::getCreatedBy)
                    .orElse(null);
            case JOURNAL_DREAM -> journalDreamRepository.findById(refKey.getId())
                    .map(JournalDreamEntity::getCreatedBy)
                    .orElse(null);
            default -> null;
        };
    }

    private String resolveTitle(final BaseClsfKey refKey) {
        return switch (refKey.getContentTypeEnum()) {
            case JOURNAL_DIARY -> journalDiaryRepository.findById(refKey.getId())
                    .map(JournalDiaryEntity::getTitle)
                    .orElse(null);
            case JOURNAL_DREAM -> journalDreamRepository.findById(refKey.getId())
                    .map(JournalDreamEntity::getTitle)
                    .orElse(null);
            default -> null;
        };
    }

    private BaseClsfKey[] normalizePair(final BaseClsfKey firstKey, final BaseClsfKey secondKey) {
        if (this.compareKey(firstKey, secondKey) <= 0) {
            return new BaseClsfKey[]{firstKey, secondKey};
        }
        return new BaseClsfKey[]{secondKey, firstKey};
    }

    private int compareKey(final BaseClsfKey firstKey, final BaseClsfKey secondKey) {
        final int contentTypeResult = firstKey.getContentType().compareTo(secondKey.getContentType());
        if (contentTypeResult != 0) return contentTypeResult;
        return Integer.compare(firstKey.getId(), secondKey.getId());
    }
}

