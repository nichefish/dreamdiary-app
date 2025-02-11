package io.nicheblog.dreamdiary.extension.clsf.tag.service;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.mapstruct.TagMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagSearchParam;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.tag.repository.jpa.TagRepository;
import io.nicheblog.dreamdiary.extension.clsf.tag.spec.TagSpec;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;

import java.util.List;

/**
 * TagService
 * <pre>
 *  태그 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface TagService
        extends BaseCrudService<TagDto, TagDto, Integer, TagEntity, TagRepository, TagSpec, TagMapstruct> {

    /**
     * 태그 관리 화면에서 요소를 관리할 컨텐츠 타입 목록 조회
     *
     * @return {@link List} -- 컨텐츠 타입 목록
     */
    List<ContentType> getContentTypeList();

    /**
     * 컨텐츠 타입에 해당하는 태그만 INNER-JOIN으로 조회
     *
     * @param contentType 조회할 컨텐츠 타입
     * @return {@link List} -- 컨텐츠 타입에 해당하는 태그 목록
     */
    List<TagDto> getContentSpecificTagList(final ContentType contentType);
    
    /**
     * 컨텐츠 타입에 해당하는 태그만 INNER-JOIN으로 조회
     *
     * @param contentType 조회할 컨텐츠 타입
     * @return {@link List} -- 컨텐츠 타입에 해당하는 태그 목록
     */
    List<TagDto> getContentSpecificTagList(final String contentType);

    /**
     * 컨텐츠 타입에 해당하는 태그만 INNER-JOIN으로 조회 (+사이즈 정보 포함)
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param contentType 조회할 컨텐츠 타입
     * @return {@link List} -- 컨텐츠 타입에 해당하는 태그 목록
     */
    List<TagDto> getContentSpecificSizedTagList(final ContentType contentType);

    /**
     * 컨텐츠 타입과 무관하게 태그 조회 (+사이즈 정보 포함)
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param searchParam 검색 파라미터
     * @return {@link List} -- 컨텐츠 타입에 해당하는 태그 목록
     */
    List<TagDto> getOverallSizedTagList(TagSearchParam searchParam) throws Exception;

    /**
     * 최대 사용빈도 계산한 태그 목록 조회
     *
     * @param tagList 태그 목록 (List<TagDto>)
     * @param contentType 조회할 컨텐츠 타입 (ContentType)
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    Integer calcMaxSize(final List<TagDto> tagList, final ContentType contentType);

    /**
     * 최대 사용빈도 계산한 태그 목록 조회
     *
     * @param tagList 태그 목록 (List<TagDto>)
     * @param contentType 조회할 컨텐츠 타입 (ContentType)
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    Integer calcMaxSize(final List<TagDto> tagList, final String contentType);

    /**
     * 최대 사용빈도 계산한 태그 목록 조회
     *
     * @param tagNo 태그 번호
     * @param contentType 조회할 컨텐츠 타입 (ContentType)
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    Integer countTagSize(final Integer tagNo, final String contentType, final String regstrId);

    /**
     * 컨텐츠 태그 처리
     * 새로운 태그를 추가하고, 더 이상 필요하지 않은 태그를 삭제합니다. 태그 목록이 동일한 경우에는 처리하지 않고 리턴합니다.
     *
     * @param clsfKey 복합키 정보
     * @param tagCmpstn 태그 구성 객체 (새로운 태그 목록 포함)
     * @throws Exception 태그 처리 중 발생할 수 있는 예외
     */
    void procTags(final BaseClsfKey clsfKey, final TagCmpstn tagCmpstn) throws Exception;

    /**
     * 마스터 태그 추가:: 메소드 분리
     *
     * @param tagList 처리할 태그 DTO 목록
     * @return {@link List<TagEntity>} -- 저장된 태그 엔티티 목록
     */
    List<TagEntity> addMasterTag(final List<TagDto> tagList);

    /**
     * 컨텐츠-태그와 연관관계 없는 마스터 태그 삭제
     */
    void deleteNoRefTags();
}
