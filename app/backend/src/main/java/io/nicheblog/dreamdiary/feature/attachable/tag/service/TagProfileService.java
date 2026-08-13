package io.nicheblog.dreamdiary.feature.attachable.tag.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagCategoryProfileEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagProfileEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.mapstruct.TagProfileMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagProfileDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.repository.jpa.TagCategoryProfileRepository;
import io.nicheblog.dreamdiary.feature.attachable.tag.repository.jpa.TagProfileRepository;
import io.nicheblog.dreamdiary.feature.attachable.tag.repository.jpa.TagRepository;
import io.nicheblog.dreamdiary.feature.attachable.tag.spec.TagProfileSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.type.CloudSizeLock;
import io.nicheblog.dreamdiary.global.type.TextClass;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TagProfileService
 * <pre>
 *  태그 프로필(해석) 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class TagProfileService
        implements BaseDtoWritableService<TagProfileDto, TagProfileDto, Integer, TagProfileEntity> {

    @Getter
    private final TagProfileRepository repository;
    @Getter
    private final TagProfileSpec spec;
    @Getter
    private final TagProfileMapstruct mapstruct = TagProfileMapstruct.INSTANCE;
    private final TagCategoryProfileRepository tagCategoryProfileRepository;
    private final TagRepository tagRepository;

    public TagProfileMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public TagProfileMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /* ----- */

    /**
     * 태그 ID + 컨텐츠 타입으로 프로필 단건 조회.
     *
     * @param tagId 태그 ID
     * @param contentType 컨텐츠 타입
     * @return {@link Optional} -- 조회된 TagProfileDto
     */
    public Optional<TagProfileDto> getDtoByTagIdAndContentType(final Integer tagId, final String contentType) throws Exception {
        final String createdBy = AuthUtils.requireLoginUsername();
        final Optional<TagProfileEntity> entityOpt = repository.findByTagIdAndContentTypeAndCreatedBy(tagId, contentType, createdBy);
        if (entityOpt.isEmpty()) return Optional.empty();

        final TagProfileDto dto = mapstruct.toDto(entityOpt.get());
        return Optional.of(this.normalizeTagTextForRead(dto));
    }

    public TagProfileDto getDtoByRefOrNew(final Integer tagId, final String contentType) throws Exception {
        final TagProfileDto tagProfile = this.getDtoByTagIdAndContentType(tagId, contentType)
                .orElseGet(() -> TagProfileDto.builder()
                        .tagId(tagId)
                        .contentType(contentType)
                        .build());
        return this.populateCategoryProfile(tagProfile);
    }

    @Transactional(readOnly = true)
    public void applyVisualSemantic(final List<TagDto> tagList, final ContentType contentType) {
        if (contentType == null) return;
        this.applyVisualSemantic(tagList, contentType.key);
    }

    /**
     * 태그 목록에 사용자별 시각 의미(색)·클라우드 크기 고정(cloudSizeLock)을 병합한다.
     * <p>변경 전: textClass(색)만 병합했다. sizeBoost·emphasized 는 폐기했다.</p>
     * <p>변경 후: 프로필 크기 고정({@code cloudSizeLock})이 {@code MAX}면 {@code tagClass}를 {@code ts-9}로, {@code MIN}이면 {@code ts-1}로 고정한다.
     * 엔트리 본문 태그줄에는 적용하지 않으며, sized 태그클라우드 경로에서만 호출된다.</p>
     *
     * @param tagList 태그 목록
     * @param contentType 컨텐츠 타입
     */
    @Transactional(readOnly = true)
    public void applyVisualSemantic(final List<TagDto> tagList, final String contentType) {
        if (CollectionUtils.isEmpty(tagList) || StringUtils.isBlank(contentType)) return;
        final String createdBy = AuthUtils.getLoginUsername();
        if (StringUtils.isBlank(createdBy)) return;

        final List<Integer> tagIdList = tagList.stream()
                .map(TagDto::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(tagIdList)) return;

        final Map<Integer, Integer> tagCategoryIdMap = new HashMap<>();
        tagList.forEach(tag -> {
            if (tag.getId() != null && tag.getTagCategoryId() != null) {
                tagCategoryIdMap.put(tag.getId(), tag.getTagCategoryId());
            }
        });

        if (tagCategoryIdMap.size() < tagIdList.size()) {
            tagRepository.findAllById(tagIdList).forEach(tagEntity -> {
                if (tagEntity.getId() != null && tagEntity.getTagCategoryId() != null) {
                    tagCategoryIdMap.putIfAbsent(tagEntity.getId(), tagEntity.getTagCategoryId());
                }
            });
        }

        final List<Integer> tagCategoryIdList = tagCategoryIdMap.values().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        final Map<Integer, TextClass> categorySemanticMap = CollectionUtils.isEmpty(tagCategoryIdList)
                ? Collections.emptyMap()
                : tagCategoryProfileRepository
                        .findAllByTagCategoryIdInAndContentTypeAndCreatedBy(tagCategoryIdList, contentType, createdBy)
                        .stream()
                        .collect(Collectors.toMap(
                                TagCategoryProfileEntity::getTagCategoryId,
                                profile -> TextClass.getOrDefault(profile.getTextClass()),
                                (left, right) -> left
                        ));

        final List<TagProfileEntity> profiles = repository.findAllByTagIdInAndContentTypeAndCreatedBy(tagIdList, contentType, createdBy);

        final Map<Integer, TextClass> semanticMap = profiles.stream()
                .filter(profile -> profile.getTextClass() != null)
                .collect(Collectors.toMap(
                        TagProfileEntity::getTagId,
                        profile -> TextClass.getOrDefault(profile.getTextClass()),
                        (left, right) -> left
                ));

        final Map<Integer, CloudSizeLock> sizeLockMap = profiles.stream()
                .collect(Collectors.toMap(
                        TagProfileEntity::getTagId,
                        profile -> CloudSizeLock.getOrDefault(profile.getCloudSizeLock()),
                        (left, right) -> left
                ));


        tagList.forEach(tag -> {
            final Integer effectiveTagCategoryId = tag.getTagCategoryId() != null
                    ? tag.getTagCategoryId()
                    : tagCategoryIdMap.get(tag.getId());
            TextClass semantic = categorySemanticMap.getOrDefault(effectiveTagCategoryId, TextClass.DEFAULT);
            semantic = semanticMap.getOrDefault(tag.getId(), semantic);
            tag.setTextSemantic(semantic);
            tag.setTextClassCd(semantic.getKey());
            tag.setTextClass(toCssTextClass(tag.getTextClassCd()));
            /* 크기 고정(MIN/MAX)이면 빈도 산출을 덮어 ts-1/ts-9. 클라우드 전용. */
            tag.setTagClass(TagCloudSizeSupport.applyCloudSizeLock(
                    tag.getTagClass(),
                    sizeLockMap.getOrDefault(tag.getId(), CloudSizeLock.AUTO)
            ));
        });
    }

    /**
     * 태그-컨텐츠 목록에 사용자별 태그 프로필 본문을 병합한다.
     * <p>변경 전: 태그 프로필 본문은 설정 모달에서만 조회되어 목록 엔트리 하단에 표시할 수 없었다.</p>
     * <p>변경 후: 호출자가 지정한 컨텐츠 타입의 태그 프로필 본문을 같은 DTO 트리에 싣는다.</p>
     *
     * @param tagList 태그-컨텐츠 목록
     * @param contentType 컨텐츠 타입
     */
    @Transactional(readOnly = true)
    public void applyProfileContent(final List<TagContentDto> tagList, final String contentType) {
        if (CollectionUtils.isEmpty(tagList) || StringUtils.isBlank(contentType)) return;
        final String createdBy = AuthUtils.getLoginUsername();
        if (StringUtils.isBlank(createdBy)) {
            log.warn("[TagProfileService] 태그 프로필 본문 병합 생략: 로그인 사용자 없음");
            return;
        }

        final List<Integer> tagIdList = tagList.stream()
                .map(TagContentDto::getTagId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(tagIdList)) return;

        final Map<Integer, String> profileContentMap = repository.findAllByTagIdInAndContentTypeAndCreatedBy(tagIdList, contentType, createdBy)
                .stream()
                .filter(profile -> StringUtils.isNotBlank(profile.getContent()))
                .collect(Collectors.toMap(
                        TagProfileEntity::getTagId,
                        TagProfileEntity::getContent,
                        (left, right) -> left
                ));
        if (profileContentMap.isEmpty()) return;

        tagList.forEach(tag -> tag.setProfileContent(profileContentMap.get(tag.getTagId())));
    }

    @Transactional
    public ServiceResponse upsert(final TagProfileDto tagProfile) throws Exception {
        this.populateTagCategoryInfo(tagProfile);
        this.normalizeTagTextForUpsert(tagProfile);
        this.normalizeCategoryTextSemantic(tagProfile);
        this.normalizeCloudSizeLockForUpsert(tagProfile);

        if (tagProfile.getId() == null) {
            this.getDtoByTagIdAndContentType(tagProfile.getTagId(), tagProfile.getContentType())
                    .ifPresent(existing -> tagProfile.setId(existing.getId()));
        }

        final ServiceResponse result = tagProfile.getId() == null ? this.regist(tagProfile) : this.modify(tagProfile);
        this.upsertCategoryProfile(tagProfile);
        return result;
    }

    @Override
    public TagProfileEntity getDtlEntity(final Integer key) throws Exception {
        final String createdBy = AuthUtils.requireLoginUsername();
        return repository.findByIdAndCreatedBy(key, createdBy)
                .orElseThrow(() -> new EntityNotFoundException());
    }

    @Override
    public TagProfileEntity findDtlEntity(final Integer key) throws Exception {
        final String createdBy = AuthUtils.getLoginUsername();
        if (StringUtils.isBlank(createdBy)) return null;

        return repository.findByIdAndCreatedBy(key, createdBy).orElse(null);
    }

    @Override
    public TagProfileDto getDtlDto(final Integer key) throws Exception {
        final TagProfileEntity entity = this.getDtlEntity(key);
        return this.normalizeTagTextForRead(mapstruct.toDto(entity));
    }

    public void evictTagCloudCaches(final String contentType) {
        final String username = AuthUtils.getLoginUsername();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(contentType)) return;

        if (ContentType.JOURNAL_DAY.key.equals(contentType)) {
            EhCacheUtils.clearUserCache("journalDayPeriodTagListByUser", username);
            return;
        }
        if (ContentType.JOURNAL_DIARY.key.equals(contentType)
                || ContentType.JOURNAL_DREAM.key.equals(contentType)) {
            EhCacheUtils.clearUserCache("journalEntryTagListByUser", username);
            EhCacheUtils.clearUserCache("journalEntryPeriodTagListByUser", username);
        }
    }

    /**
     * 저장용: cloudSizeLock 을 정규화. null 은 AUTO.
     */
    private void normalizeCloudSizeLockForUpsert(final TagProfileDto tagProfile) {
        if (tagProfile == null) return;
        tagProfile.setCloudSizeLock(CloudSizeLock.getOrDefault(tagProfile.getCloudSizeLock()));
    }


    /**
     * 조회용: DB에 저장된 개별 색만 반영. {@code text_class}가 NULL이면 상속(폼에는 코드 미전달).
     */
    private TagProfileDto normalizeTagTextForRead(final TagProfileDto tagProfile) {
        if (tagProfile == null) return null;
        tagProfile.setCloudSizeLock(CloudSizeLock.getOrDefault(tagProfile.getCloudSizeLock()));
        if (tagProfile.getTextClass() == null) {
            tagProfile.setTextClass(null);
            tagProfile.setTextClassCd(null);
            return tagProfile;
        }
        final TextClass semantic = TextClass.getOrDefault(tagProfile.getTextClass());
        tagProfile.setTextClass(semantic);
        tagProfile.setTextClassCd(semantic.getKey());
        return tagProfile;
    }

    /**
     * 저장용: 폼에서 빈 값이면 개별 색 상속({@code null})으로 정규화.
     */
    private void normalizeTagTextForUpsert(final TagProfileDto tagProfile) {
        if (tagProfile == null) return;
        final String cd = StringUtils.trimToEmpty(tagProfile.getTextClassCd());
        if (StringUtils.isBlank(cd)) {
            tagProfile.setTextClass(null);
            tagProfile.setTextClassCd(null);
            return;
        }
        final TextClass semantic = TextClass.fromCode(cd);
        tagProfile.setTextClass(semantic);
        tagProfile.setTextClassCd(semantic.getKey());
    }

    private TagProfileDto normalizeCategoryTextSemantic(final TagProfileDto tagProfile) {
        if (tagProfile == null) return null;

        final TextClass semantic = this.resolveSemantic(tagProfile.getCategoryTextClass(), tagProfile.getCategoryTextClassCd());
        tagProfile.setCategoryTextClass(semantic);
        tagProfile.setCategoryTextClassCd(semantic.getKey());
        return tagProfile;
    }

    private TextClass resolveSemantic(final TextClass semanticFromEntity, final String textClassCd) {
        final TextClass fallbackSemantic = TextClass.getOrDefault(semanticFromEntity);
        final String normalizedTextClassCd = StringUtils.trimToEmpty(textClassCd);
        final boolean hasExplicitTextClassCd = StringUtils.isNotBlank(normalizedTextClassCd)
                && !StringUtils.equalsIgnoreCase(normalizedTextClassCd, TextClass.DEFAULT.getKey());

        return hasExplicitTextClassCd ? TextClass.fromCode(normalizedTextClassCd) : fallbackSemantic;
    }

    private TagProfileDto populateCategoryProfile(final TagProfileDto tagProfile) {
        if (tagProfile == null) return null;
        if (tagProfile.getTagId() == null || StringUtils.isBlank(tagProfile.getContentType())) return tagProfile;

        final String createdBy = AuthUtils.getLoginUsername();
        if (StringUtils.isBlank(createdBy)) return tagProfile;

        this.populateTagCategoryInfo(tagProfile);
        final Integer tagCategoryId = tagProfile.getTagCategoryId();

        if (tagCategoryId == null) {
            tagProfile.setCategoryProfileId(null);
            tagProfile.setCategoryTextClass(TextClass.DEFAULT);
            tagProfile.setCategoryTextClassCd(TextClass.DEFAULT.getKey());
            return tagProfile;
        }

        final Optional<TagCategoryProfileEntity> categoryProfileOpt = tagCategoryProfileRepository
                .findByTagCategoryIdAndContentTypeAndCreatedBy(tagCategoryId, tagProfile.getContentType(), createdBy);
        if (categoryProfileOpt.isEmpty()) {
            tagProfile.setCategoryProfileId(null);
            tagProfile.setCategoryTextClass(TextClass.DEFAULT);
            tagProfile.setCategoryTextClassCd(TextClass.DEFAULT.getKey());
            return tagProfile;
        }

        final TagCategoryProfileEntity categoryProfile = categoryProfileOpt.get();
        final TextClass categorySemantic = TextClass.getOrDefault(categoryProfile.getTextClass());
        tagProfile.setCategoryProfileId(categoryProfile.getId());
        tagProfile.setCategoryTextClass(categorySemantic);
        tagProfile.setCategoryTextClassCd(categorySemantic.getKey());
        return tagProfile;
    }

    private TagProfileDto populateTagCategoryInfo(final TagProfileDto tagProfile) {
        if (tagProfile == null || tagProfile.getTagId() == null) return tagProfile;

        final Optional<TagEntity> tagOpt = tagRepository.findById(tagProfile.getTagId());
        tagProfile.setCtgr(tagOpt.map(TagEntity::getCtgr).orElse(""));
        tagProfile.setTagCategoryId(tagOpt.map(TagEntity::getTagCategoryId).orElse(null));
        return tagProfile;
    }

    private void upsertCategoryProfile(final TagProfileDto tagProfile) throws Exception {
        if (tagProfile == null || tagProfile.getTagCategoryId() == null || StringUtils.isBlank(tagProfile.getContentType())) return;

        final String createdBy = AuthUtils.requireLoginUsername();
        final TextClass categorySemantic = TextClass.getOrDefault(tagProfile.getCategoryTextClass());
        final Optional<TagCategoryProfileEntity> existingOpt = tagCategoryProfileRepository
                .findByTagCategoryIdAndContentTypeAndCreatedBy(tagProfile.getTagCategoryId(), tagProfile.getContentType(), createdBy);
        if (existingOpt.isEmpty() && TextClass.DEFAULT.equals(categorySemantic)) return;

        final TagCategoryProfileEntity categoryProfile = existingOpt
                .orElseGet(() -> TagCategoryProfileEntity.builder()
                        .tagCategoryId(tagProfile.getTagCategoryId())
                        .contentType(tagProfile.getContentType())
                        .createdBy(createdBy)
                        .build());
        categoryProfile.setTextClass(categorySemantic);
        tagCategoryProfileRepository.saveAndFlush(categoryProfile);
        tagProfile.setCategoryProfileId(categoryProfile.getId());
        tagProfile.setCategoryTextClass(categorySemantic);
        tagProfile.setCategoryTextClassCd(categorySemantic.getKey());
    }

    private String toCssTextClass(final String textClassCd) {
        if (StringUtils.isBlank(textClassCd)) return "";

        final String normalized = StringUtils.trimToEmpty(textClassCd);
        if (StringUtils.equalsIgnoreCase(normalized, TextClass.DEFAULT.getKey())) return "";

        return "text-" + normalized.toLowerCase(Locale.ROOT);
    }
}
