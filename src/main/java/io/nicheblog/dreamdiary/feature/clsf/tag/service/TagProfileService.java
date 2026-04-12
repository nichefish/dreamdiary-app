package io.nicheblog.dreamdiary.feature.clsf.tag.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagProfileEntity;
import io.nicheblog.dreamdiary.feature.clsf.tag.mapstruct.TagProfileMapstruct;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagProfileDto;
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

    public TagProfileMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public TagProfileMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /* ----- */

    /**
     * 태그 번호 + 컨텐츠 타입으로 프로필 단건 조회.
     *
     * @param refTagNo 참조 태그 번호
     * @param refContentType 참조 컨텐츠 타입
     * @return {@link Optional} -- 조회된 TagProfileDto
     */
    public Optional<TagProfileDto> getDtoByTagNoAndContentType(final Integer refTagNo, final String refContentType) throws Exception {
        final String regstrId = AuthUtils.requireLgnUserId();
        final Optional<TagProfileEntity> entityOpt = repository.findByTagNoAndContentTypeAndRegstrId(refTagNo, refContentType, regstrId);
        if (entityOpt.isEmpty()) return Optional.empty();

        final TagProfileDto dto = mapstruct.toDto(entityOpt.get());
        return Optional.of(this.normalizeTextSemantic(dto));
    }

    public TagProfileDto getDtoByRefOrNew(final Integer refTagNo, final String refContentType) throws Exception {
        return this.getDtoByTagNoAndContentType(refTagNo, refContentType)
                .orElseGet(() -> TagProfileDto.builder()
                        .tagNo(refTagNo)
                        .contentType(refContentType)
                        .textClass(TextClass.DEFAULT)
                        .textClassCd(TextClass.DEFAULT.getKey())
                        .build());
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

        final List<Integer> tagNoList = tagList.stream()
                .map(TagDto::getTagNo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(tagNoList)) return;

        final Map<Integer, TextClass> semanticMap = repository.findAllByTagNoInAndContentTypeAndRegstrId(tagNoList, contentType, regstrId)
                .stream()
                .collect(Collectors.toMap(
                        TagProfileEntity::getTagNo,
                        profile -> TextClass.getOrDefault(profile.getTextClass()),
                        (left, right) -> left
                ));

        tagList.forEach(tag -> {
            final TextClass semantic = semanticMap.getOrDefault(tag.getTagNo(), TextClass.DEFAULT);
            tag.setTextSemantic(semantic);
            tag.setTextClassCd(semantic.getKey());
            tag.setTextClass(toCssTextClass(tag.getTextClassCd()));
        });
    }

    @Transactional
    public ServiceResponse upsert(final TagProfileDto tagProfile) throws Exception {
        this.normalizeTextSemantic(tagProfile);

        if (tagProfile.getTagProfileNo() == null) {
            this.getDtoByTagNoAndContentType(tagProfile.getTagNo(), tagProfile.getContentType())
                    .ifPresent(existing -> tagProfile.setTagProfileNo(existing.getTagProfileNo()));
        }

        return tagProfile.getTagProfileNo() == null ? this.regist(tagProfile) : this.modify(tagProfile);
    }

    @Override
    public TagProfileEntity getDtlEntity(final Integer key) throws Exception {
        final String regstrId = AuthUtils.requireLgnUserId();
        return repository.findByTagProfileNoAndRegstrId(key, regstrId)
                .orElseThrow(() -> new EntityNotFoundException("exception.EntityNotFoundException"));
    }

    @Override
    public TagProfileEntity findDtlEntity(final Integer key) throws Exception {
        final String regstrId = AuthUtils.getLgnUserId();
        if (StringUtils.isBlank(regstrId)) return null;

        return repository.findByTagProfileNoAndRegstrId(key, regstrId).orElse(null);
    }

    @Override
    public TagProfileDto getDtlDto(final Integer key) throws Exception {
        final TagProfileEntity entity = this.getDtlEntity(key);
        return this.normalizeTextSemantic(mapstruct.toDto(entity));
    }

    public void evictTagCloudCaches(final String contentType) {
        final String userId = AuthUtils.getLgnUserId();
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(contentType)) return;

        if (ContentType.JRNL_DAY.key.equals(contentType)) {
            EhCacheUtils.clearUserCache("jrnlDayYyMnthSizedTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDayWeeklySizedTagListByUser", userId);
            return;
        }
        if (ContentType.JRNL_DIARY.key.equals(contentType)) {
            EhCacheUtils.clearUserCache("jrnlDiaryYyMnthSizedTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDiaryWeeklySizedTagListByUser", userId);
            return;
        }
        if (ContentType.JRNL_DREAM.key.equals(contentType)) {
            EhCacheUtils.clearUserCache("jrnlDreamYyMnthSizedTagListByUser", userId);
            EhCacheUtils.clearUserCache("jrnlDreamWeeklySizedTagListByUser", userId);
            return;
        }
        if (ContentType.JRNL_INTRPT.key.equals(contentType)) {
            EhCacheUtils.clearUserCache("jrnlIntrptYyMnthSizedTagListByUser", userId);
        }
    }

    private TagProfileDto normalizeTextSemantic(final TagProfileDto tagProfile) {
        if (tagProfile == null) return null;

        final TextClass semanticFromEntity = TextClass.getOrDefault(tagProfile.getTextClass());
        final String textClassCd = StringUtils.trimToEmpty(tagProfile.getTextClassCd());
        final boolean hasExplicitTextClassCd = StringUtils.isNotBlank(textClassCd)
                && !StringUtils.equalsIgnoreCase(textClassCd, TextClass.DEFAULT.getKey());

        final TextClass semantic = hasExplicitTextClassCd
                ? TextClass.fromCode(textClassCd)
                : semanticFromEntity;
        tagProfile.setTextClass(semantic);
        tagProfile.setTextClassCd(semantic.getKey());
        return tagProfile;
    }

    private String toCssTextClass(final String textClassCd) {
        if (StringUtils.isBlank(textClassCd)) return "";

        final String normalized = StringUtils.trimToEmpty(textClassCd);
        if (StringUtils.equalsIgnoreCase(normalized, TextClass.DEFAULT.getKey())) return "";

        return "text-" + normalized.toLowerCase(Locale.ROOT);
    }
}
