package io.nicheblog.dreamdiary.feature.attachable.tag.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagCategoryEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.mapstruct.TagMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagSearchParam;
import io.nicheblog.dreamdiary.feature.attachable.tag.repository.jpa.TagCategoryRepository;
import io.nicheblog.dreamdiary.feature.attachable.tag.repository.jpa.TagRepository;
import io.nicheblog.dreamdiary.feature.attachable.tag.spec.TagSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TagService
 * <pre>
 *  태그 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class TagService
        implements BaseDtoWritableService<TagDto, TagDto, Integer, TagEntity> {

    @Getter
    private final TagRepository repository;
    @Getter
    private final TagSpec spec;
    @Getter
    private final TagMapstruct mapstruct = TagMapstruct.INSTANCE;
    private final TagCategoryRepository tagCategoryRepository;
    private final TagProfileService tagProfileService;

    public TagMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public TagMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /**
     * 태그 관리 화면에서 요소를 관리할 컨텐츠 타입 목록 조회
     *
     * @return {@link List} -- 컨텐츠 타입 목록
     */
    public List<ContentType> getContentTypeList() {
        return List.of(
                ContentType.JOURNAL_DAY,
                ContentType.JOURNAL_DIARY,
                ContentType.JOURNAL_NOTE,
                ContentType.JOURNAL_DREAM
        );
    }

    /**
     * 컨텐츠 타입에 해당하는 태그만 INNER-JOIN으로 조회
     *
     * @param contentType 조회할 컨텐츠 타입
     * @return {@link List} -- 컨텐츠 타입에 해당하는 태그 목록
     */
    @Transactional(readOnly = true)
    public List<TagDto> getContentSpecificTagList(final ContentType contentType) {
        return this.getContentSpecificTagList(contentType.key);
    }

    /**
     * 컨텐츠 타입에 해당하는 태그만 INNER-JOIN으로 조회
     *
     * @param contentType 조회할 컨텐츠 타입
     * @return {@link List} -- 컨텐츠 타입에 해당하는 태그 목록
     */
    @Transactional(readOnly = true)
    public List<TagDto> getContentSpecificTagList(final String contentType) {
        final List<TagEntity> contentSpeficitTagList = repository.findAll(spec.getContentSpecificTag(contentType));
        return contentSpeficitTagList.stream()
                .map(entity -> {
                    try {
                        return mapstruct.toDto(entity);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 컨텐츠 타입에 해당하는 태그만 INNER-JOIN으로 조회 (+사이즈 정보 포함)
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param contentType 조회할 컨텐츠 타입
     * @return {@link List} -- 컨텐츠 타입에 해당하는 태그 목록
     */
    @Transactional(readOnly = true)
    public List<TagDto> getContentSpecificSizedTagList(final ContentType contentType) {
        final List<TagDto> tagList = this.getContentSpecificTagList(contentType);

        final int maxSize = this.calcMaxSize(tagList, contentType);
        final int MIN_SIZE = 2; // 최소 크기
        final int MAX_SIZE = 9; // 최대 크기
        final List<TagDto> sizedTagList = tagList.stream()
                .peek(dto -> {
                    final int size = dto.getContentSize();
                    if (size == 1) {
                        dto.setTagClass("ts-1");
                    } else {
                        final double ratio = (double) size / maxSize; // 사용 빈도의 비율 계산
                        final int tagSize = (int) (MIN_SIZE + (MAX_SIZE - MIN_SIZE) * ratio);
                        dto.setTagClass("ts-"+tagSize);
                    }
                })
                .sorted()
                .collect(Collectors.toList());
        tagProfileService.applyVisualSemantic(sizedTagList, contentType);
        return sizedTagList;
    }

    /**
     * 컨텐츠 타입과 무관하게 태그 조회 (+사이즈 정보 포함)
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param searchParam 검색 파라미터
     * @return {@link List} -- 컨텐츠 타입에 해당하는 태그 목록
     */
    @Transactional(readOnly = true)
    public List<TagDto> getOverallSizedTagList(final TagSearchParam searchParam) throws Exception {
        final List<TagEntity> tagEntityList = this.getListEntity(searchParam);
        final List<TagDto> tagList = mapstruct.toDtoList(tagEntityList);
        final String refContentType = searchParam.getContentType();

        final int maxSize = this.calcMaxSize(tagList, refContentType);
        final int MIN_SIZE = 2; // 최소 크기
        final int MAX_SIZE = 9; // 최대 크기
        final List<TagDto> sizedTagList = tagList.stream()
                .peek(dto -> {
                    final int size = dto.getContentSize();
                    if (size == 1) {
                        dto.setTagClass("ts-1");
                    } else {
                        final double ratio = (double) size / maxSize; // 사용 빈도의 비율 계산
                        final int tagSize = (int) (MIN_SIZE + (MAX_SIZE - MIN_SIZE) * ratio);
                        dto.setTagClass("ts-"+tagSize);
                    }
                })
                .sorted()
                .collect(Collectors.toList());
        tagProfileService.applyVisualSemantic(sizedTagList, refContentType);
        return sizedTagList;
    }

    /**
     * 최대 사용빈도 계산한 태그 목록 조회
     *
     * @param tagList 태그 Dto 목록
     * @param contentType 조회할 컨텐츠 타입 (ContentType)
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    @Transactional(readOnly = true)
    public Integer calcMaxSize(final List<TagDto> tagList, final ContentType contentType) {
        return this.calcMaxSize(tagList, contentType.key);
    }

    /**
     * 최대 사용빈도 계산한 태그 목록 조회
     *
     * @param tagList 태그 Dto 목록
     * @param contentType 조회할 컨텐츠 타입 (ContentType)
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    @Transactional(readOnly = true)
    public Integer calcMaxSize(final List<TagDto> tagList, final String contentType) {
        int maxFrequency = 0;
        for (final TagDto tag : tagList) {
            // 캐싱 처리 위해 셀프 프록시
            final Integer tagSize = this.countTagSize(tag.getId(), contentType, AuthUtils.getLoginUsername());
            tag.setContentSize(tagSize);
            maxFrequency = Math.max(maxFrequency, tagSize);
        }
        return maxFrequency;
    }

    /**
     * 최대 사용빈도 계산한 태그 목록 조회
     *
     * @param tagId 태그 ID
     * @param contentType 조회할 컨텐츠 타입 (ContentType)
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    @Transactional(readOnly = true)
    public Integer countTagSize(final Integer tagId, final String contentType, final String createdBy) {
        return repository.countTagSize(tagId, contentType, createdBy);
    }

    /**
     * 마스터 태그 추가:: 메소드 분리
     *
     * @param tagList 처리할 태그 Dto 목록
     * @return {@link List<TagEntity>} -- 저장된 태그 엔티티 목록
     */
    @Transactional
    public List<TagEntity> addMasterTag(final List<TagDto> tagList) {

        final List<TagEntity> tagEntityList = tagList.stream()
                .distinct() // 중복된 태그 문자열 제거
                .map(tag -> {
                    Optional<TagEntity> existingTag = repository.findByTagNmAndCtgr(tag.getTagNm(), tag.getCtgr());
                    if (existingTag.isPresent()) {
                        TagEntity tagEntity = existingTag.get();
                        tagEntity.setDeletedAt(null);
                        this.syncCategory(tagEntity);
                        return tagEntity;
                    }
                    // 기존 데이터가 없으면 새 객체 생성
                    final TagEntity tagEntity = new TagEntity(tag.getTagNm(), tag.getCtgr());
                    this.syncCategory(tagEntity);
                    return tagEntity;
                })
                .collect(Collectors.toList());

        return repository.saveAllAndFlush(tagEntityList);
    }

    /**
     * 태그-컨텐츠와 연관관계 없는 마스터 태그 삭제
     */
    @Transactional
    public void deleteNoRefTags() {
        final List<TagEntity> entity = repository.findAll(spec.getNoRefTags());
        repository.deleteAll(entity);
    }

    /**
     * 태그 ID 목록으로 태그 목록 조회
     * @param tagIds 태그 ID 목록
     * @return 태그 Dto 목록
     */
    public List<TagDto> getTagListByIds(final List<Integer> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) return Collections.emptyList();

        final List<TagEntity> tagEntityList = repository.findAllByIdIn(tagIds);
        return mapstruct.toDtoList(tagEntityList);
    }

    public List<TagDto> getTagListByTagIds(final List<Integer> tagIds) {
        return this.getTagListByIds(tagIds);
    }

    private void syncCategory(final TagEntity tagEntity) {
        if (tagEntity == null) return;

        final String ctgr = Optional.ofNullable(tagEntity.getCtgr())
                .map(String::trim)
                .orElse("");
        if (ctgr.isEmpty()) {
            tagEntity.setTagCategoryId(null);
            tagEntity.setTagCategory(null);
            return;
        }

        final TagCategoryEntity tagCategory = tagCategoryRepository.findByCtgrNm(ctgr)
                .orElseGet(() -> tagCategoryRepository.saveAndFlush(new TagCategoryEntity(ctgr)));

        tagEntity.setCtgr(ctgr);
        tagEntity.setTagCategoryId(tagCategory.getId());
        tagEntity.setTagCategory(tagCategory);
    }
}

