package io.nicheblog.dreamdiary.feature.journal.day.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentCntDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.service.TagProfileService;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayTagEntity;
import io.nicheblog.dreamdiary.feature.journal.day.mapstruct.JournalDayTagMapstruct;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayTagContentParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayTagQuery;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayTagRepository;
import io.nicheblog.dreamdiary.feature.journal.day.spec.JournalDayTagSpec;
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

@Service
@RequiredArgsConstructor
@Log4j2
public class JournalDayTagService
        implements BaseDtoReadableService<TagDto, Integer, JournalDayTagEntity> {

    @Getter
    private final JournalDayTagRepository repository;
    @Getter
    private final JournalDayTagSpec spec;
    @Getter
    private final JournalDayTagMapstruct mapstruct = JournalDayTagMapstruct.INSTANCE;

    private final ApplicationContext context;
    private final TagProfileService tagProfileService;

    public JournalDayTagMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public JournalDayTagMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private JournalDayTagService getSelf() {
        return context.getBean(this.getClass());
    }

    @Cacheable(value = "journalDayTagYyListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#tagId, #username)")
    public List<Integer> getYyListByTagIdAndUser(final Integer tagId, final String username) {
        return repository.findDistinctYysByTagIdAndCreatedBy(tagId, AuthUtils.requireUsername(username));
    }

    @Cacheable(value = "journalDayPeriodTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #query)")
    public List<TagDto> getPeriodTagListByUser(final String username, final JournalDayTagQuery query) throws Exception {
        return this.getSelf().getListDto(toSearchParam(username, query));
    }

    public List<TagDto> getSizedTagListByUser(final String username, final JournalDayTagQuery query) throws Exception {
        final String requiredUsername = AuthUtils.requireUsername(username);
        final List<TagDto> tagList = getSelf().getPeriodTagListByUser(requiredUsername, query);
        final int maxSize = this.calcMaxSize(tagList, requiredUsername, query);
        return this.applyTagSizes(tagList, maxSize);
    }

    public Map<String, List<TagDto>> getSizedTagGroupMapByUser(final String username, final JournalDayTagQuery query) throws Exception {
        return groupTagsByCategory(getSizedTagListByUser(username, query));
    }

    public Integer calcMaxSize(
            final List<TagDto> tagList,
            final String username,
            final JournalDayTagQuery query
    ) {
        if (CollectionUtils.isEmpty(tagList)) return 0;

        int maxFrequency = 0;
        final Map<Integer, Integer> tagCntMap = this.getSelf().getTagCountMapByUser(AuthUtils.requireUsername(username), query);

        for (final TagDto tag : tagList) {
            final Integer daySize = tagCntMap.getOrDefault(tag.getId(), 0);
            tag.setContentSize(daySize);
            maxFrequency = Math.max(maxFrequency, daySize);
        }

        return maxFrequency;
    }

    private List<TagDto> applyTagSizes(final List<TagDto> tagList, final int maxSize) {
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

        tagProfileService.applyVisualSemantic(sizedTagList, ContentType.JOURNAL_DAY);
        return sizedTagList;
    }

    @Cacheable(value = "journalDayTagCountMapByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #query)")
    public Map<Integer, Integer> getTagCountMapByUser(final String username, final JournalDayTagQuery query) {
        final List<TagContentCntDto> tagCountList = repository.countDaySizeMap(toTagContentParam(username, query));
        final ConcurrentMap<Integer, Integer> concurrentMap = tagCountList.stream()
                .collect(Collectors.toConcurrentMap(
                        TagContentCntDto::getTagId,
                        dto -> dto.getCount().intValue()
                ));
        return new ConcurrentHashMap<>(concurrentMap);
    }

    @Cacheable(value = "journalDayTagCategoryMapByUser", key = "#username")
    public Map<String, List<String>> getTagCategoryMapByUser(final String username) throws Exception {
        final List<JournalDayTagEntity> tagList = this.getSelf().getListEntity(toTagListParamMap(username));
        return tagList.stream()
                .collect(Collectors.groupingBy(
                        JournalDayTagEntity::getName,
                        Collectors.mapping(tag -> StringUtils.defaultString(tag.getCtgr()), Collectors.toList())
                ));
    }

    private HashMap<String, Object> toTagListParamMap(final String username) {
        final HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("createdBy", AuthUtils.requireUsername(username));
        return paramMap;
    }

    private JournalDaySearchParam toSearchParam(final String username, final JournalDayTagQuery query) {
        final JournalDaySearchParam.JournalDaySearchParamBuilder<?, ?> builder = JournalDaySearchParam.builder();
        if (query.yy() != null) builder.yy(query.yy());
        if (query.mnth() != null) builder.mnth(query.mnth());
        if (query.hasWeekStartDt()) builder.weekStartDt(query.weekStartDt());
        if (query.hasStdrdDt()) {
            builder.searchStartDt(query.stdrdDt());
            builder.searchEndDt(query.stdrdDt());
        }

        final JournalDaySearchParam searchParam = builder.build();
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return searchParam;
    }

    private JournalDayTagContentParam toTagContentParam(final String username, final JournalDayTagQuery query) {
        return JournalDayTagContentParam.builder()
                .yy(query.yy())
                .mnth(query.mnth())
                .weekStartDt(query.weekStartDt())
                .stdrdDt(query.stdrdDt())
                .createdBy(AuthUtils.requireUsername(username))
                .build();
    }

    private Map<String, List<TagDto>> groupTagsByCategory(final List<TagDto> tagList) {
        return tagList.stream().collect(Collectors.groupingBy(TagDto::getCtgr));
    }
}
