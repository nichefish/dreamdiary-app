package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentCntDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.service.TagProfileService;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryTagEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.mapstruct.JournalEntryTagMapstruct;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntrySearchParam;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryTagContentParam;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryTagQuery;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryTagRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.spec.JournalEntryTagSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
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
public class JournalEntryTagService
        implements BaseDtoReadableService<TagDto, Integer, JournalEntryTagEntity> {

    @Getter
    private final JournalEntryTagRepository repository;
    @Getter
    private final JournalEntryTagSpec spec;
    @Getter
    private final JournalEntryTagMapstruct mapstruct = JournalEntryTagMapstruct.INSTANCE;

    private final TagProfileService tagProfileService;

    @Lazy
    @Autowired
    @Getter
    private JournalEntryTagService self;

    /**
     * 읽기 전용 mapstruct 구현체를 반환한다.
     *
     * @return 읽기 mapstruct
     */
    @Override
    public JournalEntryTagMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    /**
     * 사용자/콘텐츠 타입 기준 태그 목록을 조회한다.
     *
     * @param username 사용자 아이디
     * @param contentType 콘텐츠 타입
     * @return 태그 목록
     * @throws Exception 조회 중 예외
     */
    @Cacheable(value = "journalEntryTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #contentType)")
    public List<TagDto> getTagListByUser(final String username, final ContentType contentType) throws Exception {
        return this.getSelf().getListDto(toTagListParamMap(username, contentType));
    }

    /**
     * 기간 조건이 포함된 태그 목록을 조회한다.
     *
     * @param username 사용자 아이디
     * @param query 태그 조회 조건
     * @return 태그 목록
     * @throws Exception 조회 중 예외
     */
    @Cacheable(value = "journalEntryPeriodTagListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #query)")
    public List<TagDto> getPeriodTagListByUser(final String username, final JournalEntryTagQuery query) throws Exception {
        return this.getSelf().getListDto(toSearchParam(username, query));
    }

    /**
     * 태그 빈도를 계산해 시각 크기 클래스까지 반영한 목록을 반환한다.
     *
     * @param username 사용자 아이디
     * @param query 태그 조회 조건
     * @return 크기 정보가 반영된 태그 목록
     * @throws Exception 조회 중 예외
     */
    public List<TagDto> getSizedTagListByUser(final String username, final JournalEntryTagQuery query) throws Exception {
        final String requiredUsername = AuthUtils.requireUsername(username);
        final List<TagDto> tagList = getSelf().getCachedTagListByUser(requiredUsername, query);
        final int maxSize = this.calcMaxSize(tagList, requiredUsername, query);
        return this.applyTagSizes(tagList, maxSize, query.contentType());
    }

    /**
     * 태그 목록을 카테고리별 그룹 맵으로 변환한다.
     *
     * @param username 사용자 아이디
     * @param query 태그 조회 조건
     * @return 카테고리별 태그 맵
     * @throws Exception 조회 중 예외
     */
    public Map<String, List<TagDto>> getSizedTagGroupMapByUser(final String username, final JournalEntryTagQuery query) throws Exception {
        return groupTagsByCategory(getSizedTagListByUser(username, query));
    }

    /**
     * 태그명별 카테고리 리스트 맵을 만든다.
     *
     * @param username 사용자 아이디
     * @param contentType 콘텐츠 타입
     * @return 태그명별 카테고리 맵
     * @throws Exception 조회 중 예외
     */
    @Cacheable(value = "journalEntryTagCtgrMapByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #contentType)")
    public Map<String, List<String>> getTagCtgrMapByUser(final String username, final ContentType contentType) throws Exception {
        final List<JournalEntryTagEntity> tagList = this.getSelf().getListEntity(toTagListParamMap(username, contentType));
        return tagList.stream()
                .collect(Collectors.groupingBy(
                        JournalEntryTagEntity::getName,
                        Collectors.mapping(tag -> StringUtils.defaultString(tag.getCtgr()), Collectors.toList())
                ));
    }

    /**
     * 태그 ID별 사용 건수 맵을 조회한다.
     *
     * @param username 사용자 아이디
     * @param query 태그 조회 조건
     * @return 태그 건수 맵
     */
    @Cacheable(value = "journalEntryTagCountMapByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #query)")
    public ConcurrentHashMap<Integer, Integer> getTagCountMapByUser(final String username, final JournalEntryTagQuery query) {
        return countSizeMapUncached(toTagContentParam(username, query));
    }

    /**
     * 태그 목록의 최대 빈도수를 계산하고 각 태그에 빈도값을 주입한다.
     *
     * @param tagList 태그 목록
     * @param username 사용자 아이디
     * @param query 태그 조회 조건
     * @return 최대 빈도수
     */
    public Integer calcMaxSize(
            final List<TagDto> tagList,
            final String username,
            final JournalEntryTagQuery query
    ) {
        if (CollectionUtils.isEmpty(tagList)) return 0;

        int maxFrequency = 0;
        final Map<Integer, Integer> tagCntMap = getSelf().getTagCountMapByUser(AuthUtils.requireUsername(username), query);

        for (final TagDto tag : tagList) {
            final Integer contentSize = tagCntMap.getOrDefault(tag.getId(), 0);
            tag.setContentSize(contentSize);
            maxFrequency = Math.max(maxFrequency, contentSize);
        }

        return maxFrequency;
    }

    /**
     * 기간 조건 유무에 맞춰 캐시 조회 메소드를 선택한다.
     *
     * @param username 사용자 아이디
     * @param query 태그 조회 조건
     * @return 캐시된 태그 목록
     * @throws Exception 조회 중 예외
     */
    private List<TagDto> getCachedTagListByUser(final String username, final JournalEntryTagQuery query) throws Exception {
        if (query.hasPeriod()) {
            return getSelf().getPeriodTagListByUser(username, query);
        }
        return getSelf().getTagListByUser(username, query.contentType());
    }

    /**
     * 태그 콘텐츠 집계를 태그 ID -> 건수 맵으로 변환한다.
     *
     * @param param 태그 건수 집계 파라미터
     * @return 태그 건수 맵
     */
    private ConcurrentHashMap<Integer, Integer> countSizeMapUncached(final JournalEntryTagContentParam param) {
        final List<TagContentCntDto> tagCountList = repository.countSizeMap(param);
        final ConcurrentMap<Integer, Integer> concurrentMap = tagCountList.stream()
                .collect(Collectors.toConcurrentMap(
                        TagContentCntDto::getTagId,
                        dto -> dto.getCount().intValue()
                ));
        return new ConcurrentHashMap<>(concurrentMap);
    }

    /**
     * 최대 빈도 대비 비율로 태그 시각 크기 클래스를 부여한다.
     *
     * @param tagList 태그 목록
     * @param maxSize 최대 빈도수
     * @param contentType 콘텐츠 타입
     * @return 크기 클래스가 반영된 태그 목록
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
     * 태그 기본 조회용 파라미터 맵을 생성한다.
     *
     * @param username 사용자 아이디
     * @param contentType 콘텐츠 타입
     * @return 조회 파라미터 맵
     */
    private HashMap<String, Object> toTagListParamMap(final String username, final ContentType contentType) {
        final HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("createdBy", AuthUtils.requireUsername(username));
        paramMap.put("contentType", contentType.key);
        return paramMap;
    }

    /**
     * 태그 질의 객체를 엔트리 검색 파라미터로 변환한다.
     *
     * @param username 사용자 아이디
     * @param query 태그 조회 조건
     * @return 엔트리 검색 파라미터
     */
    private JournalEntrySearchParam toSearchParam(final String username, final JournalEntryTagQuery query) {
        final JournalEntrySearchParam.JournalEntrySearchParamBuilder<?, ?> builder = JournalEntrySearchParam.builder();
        if (query.yy() != null) builder.yy(query.yy());
        if (query.mnth() != null) builder.mnth(query.mnth());
        if (query.hasWeekStartDt()) builder.weekStartDt(query.weekStartDt());

        final JournalEntrySearchParam searchParam = builder.build();
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        searchParam.setContentType(query.contentType().key);
        return searchParam;
    }

    /**
     * 태그 건수 집계 전용 파라미터를 생성한다.
     *
     * @param username 사용자 아이디
     * @param query 태그 조회 조건
     * @return 태그 건수 집계 파라미터
     */
    private JournalEntryTagContentParam toTagContentParam(final String username, final JournalEntryTagQuery query) {
        return JournalEntryTagContentParam.builder()
                .yy(query.yy())
                .mnth(query.mnth())
                .weekStartDt(query.weekStartDt())
                .createdBy(AuthUtils.requireUsername(username))
                .contentType(query.contentType().key)
                .build();
    }

    /**
     * 태그를 카테고리 기준으로 그룹핑한다.
     *
     * @param tagList 태그 목록
     * @return 카테고리별 태그 맵
     */
    private Map<String, List<TagDto>> groupTagsByCategory(final List<TagDto> tagList) {
        return tagList.stream().collect(Collectors.groupingBy(TagDto::getCtgr));
    }
}
