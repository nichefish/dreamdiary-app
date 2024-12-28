package io.nicheblog.dreamdiary.domain.jrnl.diary.service;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.mapstruct.JrnlDiaryTagMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.diary.repository.jpa.JrnlDiaryTagRepository;
import io.nicheblog.dreamdiary.domain.jrnl.diary.spec.JrnlDiaryTagSpec;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseReadonlyService;

import java.util.List;
import java.util.Map;

/**
 * JrnlDiaryTagService
 * <pre>
 *  저널 일기 태그 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface JrnlDiaryTagService
        extends BaseReadonlyService<TagDto, TagDto, Integer, JrnlDiaryTagEntity, JrnlDiaryTagRepository, JrnlDiaryTagSpec, JrnlDiaryTagMapstruct> {

    /**
     * 지정된 연도와 월을 기준으로 태그 목록을 캐시 처리하여 반환합니다.
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- 태그 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    List<TagDto> getListDtoWithCache(final Integer yy, final Integer mnth) throws Exception;

    /**
     * css 사이즈 계산한 일기 태그 목록 조회
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- CSS 사이즈가 적용된 태그 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    List<TagDto> getDiarySizedListDto(final Integer yy, final Integer mnth) throws Exception;

    /**
     * 최대 사용빈도 계산한 일기 태그 목록 조회
     *
     * @param tagList 태그 목록 (List<TagDto>)
     * @param yy 조회할 년도
     * @param mnth 조회할 월
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    Integer calcMaxSize(final List<TagDto> tagList, Integer yy, Integer mnth);

    /**
     * 꿈 태그별 크기 조회
     *
     * @param yy 조회할 년도
     * @param mnth 조회할 월
     * @return {@link Map} -- 카테고리별 태그 목록을 담은 Map
     */
    Integer countDiarySize(final Integer tagNo, final Integer yy, final Integer mnth);

    Map<String, List<TagDto>> getDiarySizedGroupListDto(final Integer yy, final Integer mnth) throws Exception;
}
