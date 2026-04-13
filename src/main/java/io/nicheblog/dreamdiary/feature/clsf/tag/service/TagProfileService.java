package io.nicheblog.dreamdiary.feature.clsf.tag.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagCategoryProfileEntity;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagProfileEntity;
import io.nicheblog.dreamdiary.feature.clsf.tag.mapstruct.TagProfileMapstruct;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagProfileDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.repository.jpa.TagCategoryProfileRepository;
import io.nicheblog.dreamdiary.feature.clsf.tag.repository.jpa.TagRepository;
import io.nicheblog.dreamdiary.feature.clsf.tag.repository.jpa.TagProfileRepository;
import io.nicheblog.dreamdiary.feature.clsf.tag.spec.TagProfileSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
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
@Service("tagProfileService")
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
        final String regstrId = AuthUtils.requireLgnUserId();
        final Optional<TagProfileEntity> entityOpt = repository.findByTagIdAndContentTypeAndRegstrId(tagId, contentType, regstrId);
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

    @Transactional(readOnly = true)
    public void applyVisualSemantic(final List<TagDto> tagList, final String contentType) {
        if (CollectionUtils.isEmpty(tagList) || StringUtils.isBlank(contentType)) return;
        final String regstrId = AuthUtils.getLgnUserId();
        if (StringUtils.isBlank(regstrId)) return;

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
                        .findAllByTagCategoryIdInAndContentTypeAndRegstrId(tagCategoryIdList, contentType, regstrId)
                        .stream()
                        .collect(Collectors.toMap(
                                TagCategoryProfileEntity::getTagCategoryId,
                                profile -> TextClass.getOrDefault(profile.getTextClass()),
                                (left, right) -> left
                        ));

        final Map<Integer, TextClass> semanticMap = repository.findAllByTagIdInAndContentTypeAndRegstrId(tagIdList, contentType, regstrId)
                .stream()
                .filter(profile -> profile.getTextClass() != null)
                .collect(Collectors.toMap(
                        TagProfileEntity::getTagId,
                        profile -> TextClass.getOrDefault(profile.getTextClass()),
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
        });
    }

    @Transactional
    public ServiceResponse upsert(final TagProfileDto tagProfile) throws Exception {
        this.populateTagCategoryInfo(tagProfile);
        this.normalizeTagTextForUpsert(tagProfile);
        this.normalizeCategoryTextSemantic(tagProfile);

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
        final String regstrId = AuthUtils.requireLgnUserId();
        return repository.findByIdAndRegstrId(key, regstrId)
                .orElseThrow(() -> new EntityNotFoundException("exception.EntityNotFoundException"));
    }

    @Override
    public TagProfileEntity findDtlEntity(final Integer key) throws Exception {
        final String regstrId = AuthUtils.getLgnUserId();
        if (StringUtils.isBlank(regstrId)) return null;

        return repository.findByIdAndRegstrId(key, regstrId).orElse(null);
    }

    @Override
    public TagProfileDto getDtlDto(final Integer key) throws Exception {
        final TagProfileEntity entity = this.getDtlEntity(key);
        return this.normalizeTagTextForRead(mapstruct.toDto(entity));
    }

    public void evictTagCloudCaches(final String contentType) {
        final String userId = AuthUtils.getLgnUserId();
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(contentType)) return;

        if (ContentType.JRNL_DAY.key.equals(contentType)) {
            EhCacheUtils.clearUserCache("jrnlDayYyMnthTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDayWeeklyTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDayYyMnthSizedTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDayWeeklySizedTagListByUser", userId);
            return;
        }
        if (ContentType.JRNL_DIARY.key.equals(contentType)) {
            EhCacheUtils.clearUserCache("jrnlDiaryTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDiaryYyMnthTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDiaryWeeklyTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDiaryYyMnthSizedTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDiaryWeeklySizedTagListByUser", userId);
            return;
        }
        if (ContentType.JRNL_DREAM.key.equals(contentType)) {
            EhCacheUtils.clearUserCache("jrnlDreamTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDreamYyMnthTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDreamWeeklyTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDreamYyMnthSizedTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDreamWeeklySizedTagListByUser", userId);
            return;
        }
        if (ContentType.JRNL_INTRPT.key.equals(contentType)) {
            EhCacheUtils.clearUserCache("jrnlIntrptYyMnthTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlIntrptYyMnthSizedTagListByUser", userId);
        }
    }

    /**
     * 조회용: DB에 저장된 개별 색만 반영. {@code text_class}가 NULL이면 상속(폼에는 코드 미전달).
     */
    private TagProfileDto normalizeTagTextForRead(final TagProfileDto tagProfile) {
        if (tagProfile == null) return null;
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

        final String regstrId = AuthUtils.getLgnUserId();
        if (StringUtils.isBlank(regstrId)) return tagProfile;

        this.populateTagCategoryInfo(tagProfile);
        final Integer tagCategoryId = tagProfile.getTagCategoryId();

        if (tagCategoryId == null) {
            tagProfile.setCategoryProfileId(null);
            tagProfile.setCategoryTextClass(TextClass.DEFAULT);
            tagProfile.setCategoryTextClassCd(TextClass.DEFAULT.getKey());
            return tagProfile;
        }

        final Optional<TagCategoryProfileEntity> categoryProfileOpt = tagCategoryProfileRepository
                .findByTagCategoryIdAndContentTypeAndRegstrId(tagCategoryId, tagProfile.getContentType(), regstrId);
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

        final String regstrId = AuthUtils.requireLgnUserId();
        final TextClass categorySemantic = TextClass.getOrDefault(tagProfile.getCategoryTextClass());
        final Optional<TagCategoryProfileEntity> existingOpt = tagCategoryProfileRepository
                .findByTagCategoryIdAndContentTypeAndRegstrId(tagProfile.getTagCategoryId(), tagProfile.getContentType(), regstrId);
        if (existingOpt.isEmpty() && TextClass.DEFAULT.equals(categorySemantic)) return;

        final TagCategoryProfileEntity categoryProfile = existingOpt
                .orElseGet(() -> TagCategoryProfileEntity.builder()
                        .tagCategoryId(tagProfile.getTagCategoryId())
                        .contentType(tagProfile.getContentType())
                        .regstrId(regstrId)
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
