package io.nicheblog.dreamdiary.feature.jrnl.day.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentCntDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagProfileService;
import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDayTagEntity;
import io.nicheblog.dreamdiary.feature.jrnl.day.mapstruct.JrnlDayTagMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayTagContentParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.repository.jpa.JrnlDayTagRepository;
import io.nicheblog.dreamdiary.feature.jrnl.day.spec.JrnlDayTagSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * JrnlDayTagService
 * <pre>
 *  저널 일자 태그 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JrnlDayTagService
        implements BaseDtoReadableService<TagDto, Integer, JrnlDayTagEntity> {

    @Getter
    private final JrnlDayTagRepository repository;
    @Getter
    private final JrnlDayTagSpec spec;
    @Getter
    private final JrnlDayTagMapstruct mapstruct = JrnlDayTagMapstruct.INSTANCE;

    public JrnlDayTagMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public JrnlDayTagMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationContext context;
    private final TagProfileService tagProfileService;

    private JrnlDayTagService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 사용자 기준 특정 태그가 존재하는 연도 목록을 반환합니다.
     *
     * @param tagNo 태그 번호
     * @param userId 사용자 ID
     * @return 연도 목록
     */
    @Cacheable(value = "jrnlDayTagYyListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#tagNo, #userId)")
    public List<Integer> getYyListByTagNoAndUser(final Integer tagNo, final String userId) {
        return repository.findDistinctYysByTagNoAndRegstrId(tagNo, AuthUtils.requireUserId(userId));
    }

    /**
     * 지정된 연도와 월을 기준으로 태그 목록을 캐시 처리하여 반환합니다.
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- 태그 목록
     */
    @Cacheable(value = "jrnlDayYyMnthTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#userId, #yy, #mnth)")
    public List<TagDto> getYyMnthListDtoWithCacheByUser(final String userId, final Integer yy, final Integer mnth) throws Exception {
        final JrnlDaySearchParam searchParam = JrnlDaySearchParam.builder().yy(yy).mnth(mnth).build();
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 주 시작일자를 기준으로 태그 목록을 캐시 처리하여 반환합니다.
     *
     * @param weekStartDt 주 시작일자
     * @return {@link List} -- 태그 목록
     */
    @Cacheable(value = "jrnlDayWeeklyTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#userId, #weekStartDt)")
    public List<TagDto> getWeeklyListDtoWithCacheByUser(final String userId, final String weekStartDt) throws Exception {
        final JrnlDaySearchParam searchParam = JrnlDaySearchParam.builder().weekStartDt(weekStartDt).build();
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * css 사이즈 계산한 일자 태그 목록 조회
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param userId 사용자 ID
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- CSS 사이즈가 적용된 태그 목록
     */
    @Cacheable(value = "jrnlDayYyMnthSizedTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#userId, #yy, #mnth)")
    public List<TagDto> getYyMnthSizedListDtoByUser(final String userId, final Integer yy, final Integer mnth) throws Exception {
        final List<TagDto> tagList = this.getSelf().getYyMnthListDtoWithCacheByUser(userId, yy, mnth);
        final int maxSize = this.calcMaxSize(tagList, AuthUtils.requireUserId(userId), yy, mnth, null);
        return this.applyTagSizes(tagList, maxSize, ContentType.JRNL_DAY);
    }

    /**
     * css 사이즈 계산한 일자 태그 목록 조회
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param userId 사용자 ID
     * @param weekStartDt 주 시작일자
     * @return {@link List} -- CSS 사이즈가 적용된 태그 목록
     */
    @Cacheable(value = "jrnlDayWeeklySizedTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#userId, #weekStartDt)")
    public List<TagDto> getWeeklySizedListDtoByUser(final String userId, final String weekStartDt) throws Exception {
        final List<TagDto> tagList = this.getSelf().getWeeklyListDtoWithCacheByUser(userId, weekStartDt);
        final int maxSize = this.calcMaxSize(tagList, AuthUtils.requireUserId(userId), null, null, weekStartDt);
        return this.applyTagSizes(tagList, maxSize, ContentType.JRNL_DAY);
    }

    /**
     * 최대 사용빈도 계산한 일자 태그 목록 조회
     *
     * @param tagList 태그 Dto 목록
     * @param yy 조회할 년도
     * @param mnth 조회할 월
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    public Integer calcMaxSize(
            final List<TagDto> tagList,
            final String userId,
            final Integer yy,
            final Integer mnth,
            final String weekStartDt
    ) {
        if (CollectionUtils.isEmpty(tagList)) return 0;

        int maxFrequency = 0;

        final JrnlDayTagContentParam param = JrnlDayTagContentParam.builder()
                .yy(yy)
                .mnth(mnth)
                .weekStartDt(weekStartDt)
                .regstrId(AuthUtils.requireUserId(userId))
                .build();
        final Map<Integer, Integer> tagCntMap = this.getSelf().countDaySizeMap(param);

        for (final TagDto tag : tagList) {
            final Integer daySize = tagCntMap.getOrDefault(tag.getTagNo(), 0);
            tag.setContentSize(daySize);
            maxFrequency = Math.max(maxFrequency, daySize);
        }

        return maxFrequency;
    }

    /**
     * 태그 사이즈 적용
     * @param tagList 태그 목록
     * @param maxSize 최대 크기
     * @return 사이즈 적용된 태그 Dto 목록
     */
    private List<TagDto> applyTagSizes(final List<TagDto> tagList, final int maxSize, final ContentType contentType) {
        final int minSize = 2;
        final int maxTagSize = 9;

        final List<TagDto> sizedTagList = tagList.stream()
                .peek(dto -> {
                    final int size = dto.getContentSize();
                    if (size <= 1 || maxSize <= 1) {
                        dto.setTagClass("ts-1");
                        return;
                    }

                    final double ratio = (double) size / maxSize;
                    final int tagSize = (int) (minSize + (maxTagSize - minSize) * ratio);
                    dto.setTagClass("ts-" + tagSize);
                })
                .sorted()
                .collect(Collectors.toList());

        tagProfileService.applyVisualSemantic(sizedTagList, contentType);
        return sizedTagList;
    }

    /**
     * 일자 태그별 크기 맵 조회
     *
     * @return {@link Map} -- 카테고리별 태그 목록을 담은 Map
     */
    @Cacheable(value = "jrnlDayCountMapByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#param.regstrId, #param.yy, #param.mnth, #param.weekStartDt)")
    public Map<Integer, Integer> countDaySizeMap(final JrnlDayTagContentParam param) {
        final List<TagContentCntDto> tagCountList = repository.countDaySizeMap(param);

        final ConcurrentMap<Integer, Integer> concurrentMap = tagCountList.stream()
                .collect(Collectors.toConcurrentMap(
                        TagContentCntDto::getTagNo,
                        dto -> dto.getCount().intValue()
                ));
        return new ConcurrentHashMap<>(concurrentMap);
    }

    /**
     * 지정된 연도와 월을 기준으로 태그 목록을 카테고리별로 그룹화하여 반환합니다.
     *
     * @param userId 사용자 ID
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link Map} -- 카테고리별로 그룹화된 태그 목록을 담은 Map
     */
    public Map<String, List<TagDto>> getYyMnthSizedGroupListDtoByUser(final String userId, final Integer yy, final Integer mnth) throws Exception {
        final List<TagDto> tagList = this.getSelf().getYyMnthSizedListDtoByUser(AuthUtils.requireUserId(userId), yy, mnth);
        return tagList.stream().collect(Collectors.groupingBy(TagDto::getCtgr));
    }

    /**
     * 주 시작일자를 기준으로 태그 목록을 카테고리별로 그룹화하여 반환합니다.
     *
     * @param userId 사용자 ID
     * @param weekStartDt 주 시작일자
     * @return {@link Map} -- 카테고리별로 그룹화된 태그 목록을 담은 Map
     */
    public Map<String, List<TagDto>> getWeeklySizedGroupListDtoByUser(final String userId, final String weekStartDt) throws Exception {
        final List<TagDto> tagList = this.getSelf().getWeeklySizedListDtoByUser(AuthUtils.requireUserId(userId), weekStartDt);
        return tagList.stream().collect(Collectors.groupingBy(TagDto::getCtgr));
    }

    /**
     * 사용자별 태그 카테고리 맵을 반환합니다.
     *
     * @param userId 사용자 아이디
     * @return {@link Map} -- 태그 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    @Cacheable(value = "jrnlDayTagCtgrMapByUser", key = "#userId")
    public Map<String, List<String>> getTagCtgrMapByUser(final String userId) throws Exception {
        final HashMap<String, Object> paramMap = new HashMap<>() {{
            put("regstrId", AuthUtils.requireUserId(userId));
        }};

        final List<JrnlDayTagEntity> tagList = this.getSelf().getListEntity(paramMap);
        return tagList.stream()
                .collect(Collectors.groupingBy(
                        JrnlDayTagEntity::getTagNm,
                        Collectors.mapping(tag -> StringUtils.defaultString(tag.getCtgr()), Collectors.toList())
                ));
    }
}
