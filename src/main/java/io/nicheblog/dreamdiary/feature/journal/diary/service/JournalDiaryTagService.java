package io.nicheblog.dreamdiary.feature.journal.diary.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentCntDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.service.TagProfileService;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryTagEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.mapstruct.JournalDiaryTagMapstruct;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiarySearchParam;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryTagContentParam;
import io.nicheblog.dreamdiary.feature.journal.diary.repository.jpa.JournalDiaryTagRepository;
import io.nicheblog.dreamdiary.feature.journal.diary.spec.JournalDiaryTagSpec;
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
 * JournalDiaryTagService
 * <pre>
 *  저널 일기 태그 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalDiaryTagService
        implements BaseDtoReadableService<TagDto, Integer, JournalDiaryTagEntity> {

    @Getter
    private final JournalDiaryTagRepository repository;
    @Getter
    private final JournalDiaryTagSpec spec;
    @Getter
    private final JournalDiaryTagMapstruct mapstruct = JournalDiaryTagMapstruct.INSTANCE;

    public JournalDiaryTagMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public JournalDiaryTagMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationContext context;
    private final TagProfileService tagProfileService;

    private JournalDiaryTagService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 태그 ID-태그 이름 맵을 반환합니다.
     *
     * @param username 사용자 계정명
     * @return {@link Map} -- 태그 ID를 키로 하고, 태그 이름을 값으로 가지는 맵
     */
    @Cacheable(value = "journalDiaryTagListByUser", key = "#username")
    public List<TagDto> getTagListByUser(final String username) throws Exception {
        final HashMap<String, Object> paramMap = new HashMap<>() {{
            put("createdBy", AuthUtils.requireUsername(username));
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
    @Cacheable(value = "journalDiaryYyMnthTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #yy, #mnth)")
    public List<TagDto> getListDtoWithCacheByUser(final String username, final Integer yy, final Integer mnth) throws Exception {
        final JournalDiarySearchParam searchParam = JournalDiarySearchParam.builder().yy(yy).mnth(mnth).build();
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
    }

    @Cacheable(value = "journalDiaryWeeklyTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #weekStartDt)")
    public List<TagDto> getWeeklyListDtoWithCacheByUser(final String username, final String weekStartDt) throws Exception {
        final JournalDiarySearchParam searchParam = JournalDiarySearchParam.builder().weekStartDt(weekStartDt).build();
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * css 사이즈 계산한 일기 태그 목록 조회
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param username 사용자 계정명
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- CSS 사이즈가 적용된 태그 목록
     */
    @Cacheable(value = "journalDiaryYyMnthSizedTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #yy, #mnth)")
    public List<TagDto> getDiarySizedListDtoByUser(final String username, final Integer yy, final Integer mnth) throws Exception {
        final List<TagDto> tagList = this.getSelf().getListDtoWithCacheByUser(username, yy, mnth);
        final int maxSize = this.calcMaxSize(tagList, AuthUtils.requireUsername(username), yy, mnth, null);
        return this.applyTagSizes(tagList, maxSize, ContentType.JOURNAL_DIARY);
    }

    @Cacheable(value = "journalDiaryWeeklySizedTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #weekStartDt)")
    public List<TagDto> getWeeklySizedListDtoByUser(final String username, final String weekStartDt) throws Exception {
        final List<TagDto> tagList = this.getSelf().getWeeklyListDtoWithCacheByUser(username, weekStartDt);
        final int maxSize = this.calcMaxSize(tagList, AuthUtils.requireUsername(username), null, null, weekStartDt);
        return this.applyTagSizes(tagList, maxSize, ContentType.JOURNAL_DIARY);
    }

    /**
     * 최대 사용빈도 계산한 일기 태그 목록 조회
     *
     * @param tagList 태그 Dto 목록
     * @param yy 조회할 년도
     * @param mnth 조회할 월
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    public Integer calcMaxSize(final List<TagDto> tagList, final String username, final Integer yy, final Integer mnth, final String weekStartDt) {
        if (CollectionUtils.isEmpty(tagList)) return 0;

        int maxFrequency = 0;

        final JournalDiaryTagContentParam param = JournalDiaryTagContentParam.builder()
                .yy(yy)
                .mnth(mnth)
                .weekStartDt(weekStartDt)
                .createdBy(AuthUtils.requireUsername(username))
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
    @Cacheable(value = "journalDiaryCountMapByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#param.createdBy, #param.yy, #param.mnth, #param.weekStartDt)")
    public ConcurrentHashMap<Integer, Integer> countDiarySizeMap(final JournalDiaryTagContentParam param) {
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
     * @param username 사용자 계정명
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link Map} -- 카테고리별로 그룹화된 태그 목록을 담은 Map
     */
    public Map<String, List<TagDto>> getDiarySizedGroupListDtoByUser(final String username, final Integer yy, final Integer mnth) throws Exception {
        final List<TagDto> tagList = this.getSelf().getDiarySizedListDtoByUser(AuthUtils.requireUsername(username), yy, mnth);
        tagProfileService.applyVisualSemantic(tagList, ContentType.JOURNAL_DIARY);
        return tagList.stream().collect(Collectors.groupingBy(TagDto::getCtgr));
    }

    public Map<String, List<TagDto>> getWeeklySizedGroupListDtoByUser(final String username, final String weekStartDt) throws Exception {
        final List<TagDto> tagList = this.getSelf().getWeeklySizedListDtoByUser(AuthUtils.requireUsername(username), weekStartDt);
        tagProfileService.applyVisualSemantic(tagList, ContentType.JOURNAL_DIARY);
        return tagList.stream().collect(Collectors.groupingBy(TagDto::getCtgr));
    }

    /**
     * 태그 카테고리 맵을 반환합니다.
     *
     * @param username 사용자 계정명
     * @return {@link Map} -- 태그 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    @Cacheable(value = "journalDiaryTagCtgrMapByUser", key = "#username")
    public Map<String, List<String>> getTagCtgrMapByUser(final String username) throws Exception {
        final HashMap<String, Object> paramMap = new HashMap<>() {{
            put("createdBy", AuthUtils.requireUsername(username));
        }};

        final List<JournalDiaryTagEntity> tagList = this.getSelf().getListEntity(paramMap);
        return tagList.stream()
                .collect(Collectors.groupingBy(
                        JournalDiaryTagEntity::getTagNm,
                        Collectors.mapping(tag -> StringUtils.defaultString(tag.getCtgr()), Collectors.toList())
                ));
    }
}

