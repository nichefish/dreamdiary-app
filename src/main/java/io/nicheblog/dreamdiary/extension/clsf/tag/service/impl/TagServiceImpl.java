package io.nicheblog.dreamdiary.extension.clsf.tag.service.impl;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.mapstruct.TagMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagSearchParam;
import io.nicheblog.dreamdiary.extension.clsf.tag.repository.jpa.TagRepository;
import io.nicheblog.dreamdiary.extension.clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.extension.clsf.tag.spec.TagSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Service("tagService")
@RequiredArgsConstructor
@Log4j2
public class TagServiceImpl
        implements TagService {

    @Getter
    private final TagRepository repository;
    @Getter
    private final TagSpec spec;
    @Getter
    private final TagMapstruct mapstruct = TagMapstruct.INSTANCE;

    /**
     * 태그 관리 화면에서 요소를 관리할 컨텐츠 타입 목록 조회
     *
     * @return {@link List} -- 컨텐츠 타입 목록
     */
    public List<ContentType> getContentTypeList() {
        return List.of(
                ContentType.JRNL_DAY,
                ContentType.JRNL_DIARY,
                ContentType.JRNL_DREAM
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
        return tagList.stream()
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
        final List<TagDto> tagList = this.getListDto(searchParam);
        final String refContentType = searchParam.getContentType();

        final int maxSize = this.calcMaxSize(tagList, refContentType);
        final int MIN_SIZE = 2; // 최소 크기
        final int MAX_SIZE = 9; // 최대 크기
        return tagList.stream()
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
    }

    /**
     * 최대 사용빈도 계산한 태그 목록 조회
     *
     * @param tagList 태그 목록 (List<TagDto>)
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
     * @param tagList 태그 목록 (List<TagDto>)
     * @param contentType 조회할 컨텐츠 타입 (ContentType)
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    @Transactional(readOnly = true)
    public Integer calcMaxSize(final List<TagDto> tagList, final String contentType) {
        int maxFrequency = 0;
        for (final TagDto tag : tagList) {
            // 캐싱 처리 위해 셀프 프록시
            final Integer tagSize = this.countTagSize(tag.getTagNo(), contentType, AuthUtils.getLgnUserId());
            tag.setContentSize(tagSize);
            maxFrequency = Math.max(maxFrequency, tagSize);
        }
        return maxFrequency;
    }

    /**
     * 최대 사용빈도 계산한 태그 목록 조회
     *
     * @param tagNo 태그 번호
     * @param contentType 조회할 컨텐츠 타입 (ContentType)
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    @Transactional(readOnly = true)
    public Integer countTagSize(final Integer tagNo, final String contentType, final String regstrId) {
        return repository.countTagSize(tagNo, contentType, regstrId);
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
                        tagEntity.setDelYn("N");
                        return tagEntity;
                    }
                    // 기존 데이터가 없으면 새 객체 생성
                    return new TagEntity(tag.getTagNm(), tag.getCtgr());
                })
                .collect(Collectors.toList());

        return repository.saveAllAndFlush(tagEntityList);
    }

    /**
     * 컨텐츠-태그와 연관관계 없는 마스터 태그 삭제
     */
    @Transactional
    public void deleteNoRefTags() {
        final List<TagEntity> entity = repository.findAll(spec.getNoRefTags());
        repository.deleteAll(entity);
    }
}
