package io.nicheblog.dreamdiary.global._common._clsf.tag.service.impl;

import io.nicheblog.dreamdiary.global._common._clsf.ContentType;
import io.nicheblog.dreamdiary.global._common._clsf.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.global._common._clsf.tag.mapstruct.TagMapstruct;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.TagSearchParam;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.global._common._clsf.tag.repository.jpa.TagRepository;
import io.nicheblog.dreamdiary.global._common._clsf.tag.service.ContentTagService;
import io.nicheblog.dreamdiary.global._common._clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.global._common._clsf.tag.spec.TagSpec;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private final ContentTagService contentTagService;

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
                    } catch (Exception e) {
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
    public List<TagDto> getOverallSizedTagList(TagSearchParam searchParam) throws Exception {
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
            final Integer tagSize = this.countTagSize(tag.getTagNo(), contentType);
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
    public Integer countTagSize(final Integer tagNo, final String contentType) {
        return repository.countTagSize(tagNo, contentType);
    }

    /**
     * 컨텐츠 태그 처리
     * 새로운 태그를 추가하고, 더 이상 필요하지 않은 태그를 삭제합니다. 태그 목록이 동일한 경우에는 처리하지 않고 리턴합니다.
     *
     * @param clsfKey 복합키 정보
     * @param tagCmpstn 태그 구성 객체 (새로운 태그 목록 포함)
     * @throws Exception 태그 처리 중 발생할 수 있는 예외
     */
    @Transactional
    public void procTags(final BaseClsfKey clsfKey, final TagCmpstn tagCmpstn) throws Exception {
        // 태그객체가 넘어오지 않았으면? 리턴.
        if (tagCmpstn == null) return;

        // 기존 태그와 컨텐츠 태그가 동일하면 리턴
        final List<TagDto> existingTagList = contentTagService.getTagStrListByClsfKey(clsfKey);
        final List<TagDto> newTagList = tagCmpstn.getParsedTagList();
        final boolean isSame = newTagList.size() == existingTagList.size() && new HashSet<>(newTagList).containsAll(existingTagList);
        if (isSame) return;
        
        // 1, 추가해야 할 마스터 태그 처리 (메소드 분리)
        // 새로운 태그 목록에서 기존 태그 목록을 빼면 추가해야 할 태그들이 나옴
        final Set<TagDto> newTagSet = new HashSet<>(newTagList);
        existingTagList.forEach(newTagSet::remove);
        final List<TagEntity> rsList = CollectionUtils.isNotEmpty(newTagSet)
                ? this.addMasterTag(new ArrayList<>(newTagSet))
                : new ArrayList<>();

        // 2. 삭제해야 할 태그 삭제
        // 기존 태그 목록에서 새로운 태그 목록을 빼면 삭제해야 할 태그들이 나옴
        final Set<TagDto> obsoleteTagSet = new HashSet<>(existingTagList);
        newTagList.forEach(obsoleteTagSet::remove);
        if (CollectionUtils.isNotEmpty(obsoleteTagSet)) contentTagService.delObsoleteContentTags(clsfKey, new ArrayList<>(obsoleteTagSet));

        // 3. 추가해야 할 컨텐츠-태그를 처리해준다.
        if (CollectionUtils.isNotEmpty(rsList)) contentTagService.addContentTags(clsfKey, rsList);
    }

    /**
     * 마스터 태그 추가:: 메소드 분리
     *
     * @param tagList 처리할 태그 DTO 목록
     * @return {@link List<TagEntity>} -- 저장된 태그 엔티티 목록
     */
    @Transactional
    public List<TagEntity> addMasterTag(final List<TagDto> tagList) {

        final List<TagEntity> tagEntityList = tagList.stream()
                .distinct() // 중복된 태그 문자열 제거
                .map(tag -> repository.findByTagNmAndCtgr(tag.getTagNm(), tag.getCtgr())
                        .orElseGet(() -> new TagEntity(tag.getTagNm(), tag.getCtgr()))) // 데이터베이스에서 태그 조회, 없으면 새 객체 생성
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
