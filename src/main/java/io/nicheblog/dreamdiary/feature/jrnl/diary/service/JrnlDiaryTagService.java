package io.nicheblog.dreamdiary.feature.jrnl.diary.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentCntDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagProfileService;
import io.nicheblog.dreamdiary.feature.jrnl.diary.entity.JrnlDiaryTagEntity;
import io.nicheblog.dreamdiary.feature.jrnl.diary.mapstruct.JrnlDiaryTagMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiarySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryTagContentParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.repository.jpa.JrnlDiaryTagRepository;
import io.nicheblog.dreamdiary.feature.jrnl.diary.spec.JrnlDiaryTagSpec;
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
 * JrnlDiaryTagService
 * <pre>
 *  저널 일기 태그 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JrnlDiaryTagService
        implements BaseDtoReadableService<TagDto, Integer, JrnlDiaryTagEntity> {

    @Getter
    private final JrnlDiaryTagRepository repository;
    @Getter
    private final JrnlDiaryTagSpec spec;
    @Getter
    private final JrnlDiaryTagMapstruct mapstruct = JrnlDiaryTagMapstruct.INSTANCE;

    public JrnlDiaryTagMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public JrnlDiaryTagMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationContext context;
    private final TagProfileService tagProfileService;

    private JrnlDiaryTagService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 태그 ID-태그 이름 맵을 반환합니다.
     *
     * @param userId 사용자 아이디
     * @return {@link Map} -- 태그 ID를 키로 하고, 태그 이름을 값으로 가지는 맵
     */
    @Cacheable(value = "jrnlDiaryTagListByUser", key = "#userId")
    public List<TagDto> getTagListByUser(final String userId) throws Exception {
        final HashMap<String, Object> paramMap = new HashMap<>() {{
            put("regstrId", AuthUtils.requireUserId(userId));
        }};

        return this.getSelf().getListDto(paramMap);
    }

    /**
     * 지정된 연도와 월을 기준으로 태그 목록을 캐시 처리하여 반환합니다.
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- 태그 목록
     */
    @Cacheable(value = "jrnlDiaryYyMnthTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#userId, #yy, #mnth)")
    public List<TagDto> getListDtoWithCacheByUser(final String userId, final Integer yy, final Integer mnth) throws Exception {
        final JrnlDiarySearchParam searchParam = JrnlDiarySearchParam.builder().yy(yy).mnth(mnth).build();
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));
        return this.getSelf().getListDto(searchParam);
    }

    @Cacheable(value = "jrnlDiaryWeeklyTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#userId, #weekStartDt)")
    public List<TagDto> getWeeklyListDtoWithCacheByUser(final String userId, final String weekStartDt) throws Exception {
        final JrnlDiarySearchParam searchParam = JrnlDiarySearchParam.builder().weekStartDt(weekStartDt).build();
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * css 사이즈 계산한 일기 태그 목록 조회
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param userId 사용자 ID
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- CSS 사이즈가 적용된 태그 목록
     */
    @Cacheable(value = "jrnlDiaryYyMnthSizedTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#userId, #yy, #mnth)")
    public List<TagDto> getDiarySizedListDtoByUser(final String userId, final Integer yy, final Integer mnth) throws Exception {
        final List<TagDto> tagList = this.getSelf().getListDtoWithCacheByUser(userId, yy, mnth);
        final int maxSize = this.calcMaxSize(tagList, AuthUtils.requireUserId(userId), yy, mnth, null);
        return this.applyTagSizes(tagList, maxSize, ContentType.JRNL_DIARY);
    }

    @Cacheable(value = "jrnlDiaryWeeklySizedTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#userId, #weekStartDt)")
    public List<TagDto> getWeeklySizedListDtoByUser(final String userId, final String weekStartDt) throws Exception {
        final List<TagDto> tagList = this.getSelf().getWeeklyListDtoWithCacheByUser(userId, weekStartDt);
        final int maxSize = this.calcMaxSize(tagList, AuthUtils.requireUserId(userId), null, null, weekStartDt);
        return this.applyTagSizes(tagList, maxSize, ContentType.JRNL_DIARY);
    }

    /**
     * 최대 사용빈도 계산한 일기 태그 목록 조회
     *
     * @param tagList 태그 Dto 목록
     * @param yy 조회할 년도
     * @param mnth 조회할 월
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    public Integer calcMaxSize(final List<TagDto> tagList, final String userId, final Integer yy, final Integer mnth, final String weekStartDt) {
        if (CollectionUtils.isEmpty(tagList)) return 0;

        int maxFrequency = 0;

        final JrnlDiaryTagContentParam param = JrnlDiaryTagContentParam.builder()
                .yy(yy)
                .mnth(mnth)
                .weekStartDt(weekStartDt)
                .regstrId(AuthUtils.requireUserId(userId))
                .build();
        final Map<Integer, Integer> tagCntMap = this.getSelf().countDiarySizeMap(param);

        for (final TagDto tag : tagList) {
            final Integer diarySize = tagCntMap.getOrDefault(tag.getId(), 0);
            tag.setContentSize(diarySize);
            maxFrequency = Math.max(maxFrequency, diarySize);
        }

        return maxFrequency;
    }

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
     * 일기 태그별 크기 맵 조회
     *
     * @return {@link Map} -- 카테고리별 태그 목록을 담은 Map
     */
    @Cacheable(value = "jrnlDiaryCountMapByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#param.regstrId, #param.yy, #param.mnth, #param.weekStartDt)")
    public ConcurrentHashMap<Integer, Integer> countDiarySizeMap(final JrnlDiaryTagContentParam param) {
        final List<TagContentCntDto> tagCountList = repository.countDiarySizeMap(param);

        final ConcurrentMap<Integer, Integer> concurrentMap = tagCountList.stream()
                .collect(Collectors.toConcurrentMap(
                        TagContentCntDto::getTagId,
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
    public Map<String, List<TagDto>> getDiarySizedGroupListDtoByUser(final String userId, final Integer yy, final Integer mnth) throws Exception {
        final List<TagDto> tagList = this.getSelf().getDiarySizedListDtoByUser(AuthUtils.requireUserId(userId), yy, mnth);
        tagProfileService.applyVisualSemantic(tagList, ContentType.JRNL_DIARY);
        return tagList.stream().collect(Collectors.groupingBy(TagDto::getCtgr));
    }

    public Map<String, List<TagDto>> getWeeklySizedGroupListDtoByUser(final String userId, final String weekStartDt) throws Exception {
        final List<TagDto> tagList = this.getSelf().getWeeklySizedListDtoByUser(AuthUtils.requireUserId(userId), weekStartDt);
        tagProfileService.applyVisualSemantic(tagList, ContentType.JRNL_DIARY);
        return tagList.stream().collect(Collectors.groupingBy(TagDto::getCtgr));
    }

    /**
     * 태그 카테고리 맵을 반환합니다.
     *
     * @param userId 사용자 아이디
     * @return {@link Map} -- 태그 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    @Cacheable(value = "jrnlDiaryTagCtgrMapByUser", key = "#userId")
    public Map<String, List<String>> getTagCtgrMapByUser(final String userId) throws Exception {
        final HashMap<String, Object> paramMap = new HashMap<>() {{
            put("regstrId", AuthUtils.requireUserId(userId));
        }};

        final List<JrnlDiaryTagEntity> tagList = this.getSelf().getListEntity(paramMap);
        return tagList.stream()
                .collect(Collectors.groupingBy(
                        JrnlDiaryTagEntity::getTagNm,
                        Collectors.mapping(tag -> StringUtils.defaultString(tag.getCtgr()), Collectors.toList())
                ));
    }
}
