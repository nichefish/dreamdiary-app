package io.nicheblog.dreamdiary.domain.jrnl.dream.service;

import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.mapstruct.JrnlDreamTagMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamSearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.dream.repository.jpa.JrnlDreamTagRepository;
import io.nicheblog.dreamdiary.domain.jrnl.dream.spec.JrnlDreamTagSpec;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseReadonlyService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JrnlDreamTagService
 * <pre>
 *  저널 꿈 태그 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface JrnlDreamTagService
        extends BaseReadonlyService<TagDto, TagDto, Integer, JrnlDreamTagEntity, JrnlDreamTagRepository, JrnlDreamTagSpec, JrnlDreamTagMapstruct> {

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
     * css 사이즈 계산한 태그 목록 조회
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- CSS 사이즈가 적용된 태그 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    List<TagDto> getDreamSizedListDto(final Integer yy, final Integer mnth) throws Exception;

    /**
     * 최대 사용빈도 계산한 꿈 태그 목록 조회
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
    Integer countDreamSize(final Integer tagNo, final Integer yy, final Integer mnth);

    Map<String, List<TagDto>> getDreamSizedGroupListDto(final Integer yy, final Integer mnth) throws Exception;
}
