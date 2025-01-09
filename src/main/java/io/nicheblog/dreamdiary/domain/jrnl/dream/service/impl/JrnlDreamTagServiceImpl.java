package io.nicheblog.dreamdiary.domain.jrnl.dream.service.impl;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.mapstruct.JrnlDreamTagMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamSearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.dream.repository.jpa.JrnlDreamTagRepository;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamTagService;
import io.nicheblog.dreamdiary.domain.jrnl.dream.spec.JrnlDreamTagSpec;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.TagDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JrnlDreamTagService
 * <pre>
 *  저널 꿈 태그 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlDreamTagService")
@RequiredArgsConstructor
@Log4j2
public class JrnlDreamTagServiceImpl
        implements JrnlDreamTagService {

    @Getter
    private final JrnlDreamTagRepository repository;
    @Getter
    private final JrnlDreamTagSpec spec;
    @Getter
    private final JrnlDreamTagMapstruct mapstruct = JrnlDreamTagMapstruct.INSTANCE;

    private final ApplicationContext context;
    private JrnlDreamTagServiceImpl getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 지정된 연도와 월을 기준으로 태그 목록을 캐시 처리하여 반환합니다.
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- 태그 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="jrnlDreamTagList", key="#yy + \"_\" + #mnth")
    public List<TagDto> getListDtoWithCache(final Integer yy, final Integer mnth) throws Exception {
        final JrnlDreamSearchParam searchParam = JrnlDreamSearchParam.builder().yy(yy).mnth(mnth).build();

        return this.getSelf().getListDto(searchParam);
    }

    /**
     * css 사이즈 계산한 태그 목록 조회
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- CSS 사이즈가 적용된 태그 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="jrnlDreamSizedTagList", key="#yy + \"_\" + #mnth")
    public List<TagDto> getDreamSizedListDto(final Integer yy, final Integer mnth) throws Exception {
        // 저널 꿈 태그 DTO 목록 조회
        final List<TagDto> tagList = this.getSelf().getListDtoWithCache(yy, mnth);

        final int maxSize = this.calcMaxSize(tagList, yy, mnth);
        final int MIN_SIZE = 2; // 최소 크기
        final int MAX_SIZE = 9; // 최대 크기
        return tagList.stream()
                .peek(dto -> {
                    int size = dto.getContentSize();
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
     * 최대 사용빈도 계산한 꿈 태그 목록 조회
     *
     * @param tagList 태그 목록 (List<TagDto>)
     * @param yy 조회할 년도
     * @param mnth 조회할 월
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    @Override
    public Integer calcMaxSize(final List<TagDto> tagList, Integer yy, Integer mnth) {
        int maxFrequency = 0;
        for (TagDto tag : tagList) {
            // 캐싱 처리 위해 셀프 프록시
            final Integer dreamSize = this.getSelf().countDreamSize(tag.getTagNo(), yy, mnth);
            tag.setContentSize(dreamSize);
            maxFrequency = Math.max(maxFrequency, dreamSize);
        }

        return maxFrequency;
    }

    /**
     * 꿈 태그별 크기 조회
     *
     * @param yy 조회할 년도
     * @param mnth 조회할 월
     * @return {@link Map} -- 카테고리별 태그 목록을 담은 Map
     */
    @Override
    @Cacheable(value="countDreamSize", key="#tagNo + \"_\" + #yy + \"_\" + #mnth")
    public Integer countDreamSize(final Integer tagNo, final Integer yy, final Integer mnth) {
        return repository.countDreamSize(tagNo, yy, mnth);
    }

    /**
     * 지정된 연도와 월을 기준으로 태그 목록을 카테고리별로 그룹화하여 반환합니다.
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link Map} -- 카테고리별로 그룹화된 태그 목록을 담은 Map
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    public Map<String, List<TagDto>> getDreamSizedGroupListDto(final Integer yy, final Integer mnth) throws Exception {
        final List<TagDto> tagList = this.getSelf().getDreamSizedListDto(yy, mnth);

        // 태그를 카테고리별로 그룹화하여 맵으로 반환
        return tagList.stream()
                .collect(Collectors.groupingBy(TagDto::getCtgr));
    }

    @Override
    public Map<String, List<String>> getTagCtgrMap() throws Exception {

        final List<JrnlDreamTagEntity> tagList = this.getSelf().getListEntity(new HashMap<>());
        return tagList.stream()
                .collect(Collectors.groupingBy(
                        JrnlDreamTagEntity::getTagNm,
                        Collectors.mapping(tag -> {
                            if (StringUtils.isBlank(tag.getCtgr())) return "";
                            return tag.getCtgr();
                        }, Collectors.toList())
                ));
    }
}
