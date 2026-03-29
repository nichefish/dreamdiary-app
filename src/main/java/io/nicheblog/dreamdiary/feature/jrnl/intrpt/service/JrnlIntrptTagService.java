package io.nicheblog.dreamdiary.feature.jrnl.intrpt.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagContentCntDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.entity.JrnlIntrptTagEntity;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.mapstruct.JrnlIntrptTagMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.model.JrnlIntrptSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.model.JrnlIntrptTagContentParam;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.repository.jpa.JrnlIntrptTagRepository;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.spec.JrnlIntrptTagSpec;
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
 * JrnlIntrptTagService
 * <pre>
 *  저널 일기 태그 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlIntrptTagService")
@RequiredArgsConstructor
@Log4j2
public class JrnlIntrptTagService
        implements BaseDtoReadableService<TagDto, Integer, JrnlIntrptTagEntity> {

    @Getter
    private final JrnlIntrptTagRepository repository;
    @Getter
    private final JrnlIntrptTagSpec spec;
    @Getter
    private final JrnlIntrptTagMapstruct mapstruct = JrnlIntrptTagMapstruct.INSTANCE;

    public JrnlIntrptTagMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JrnlIntrptTagMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationContext context;
    private JrnlIntrptTagService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 지정된 연도와 월을 기준으로 태그 목록을 캐시 처리하여 반환합니다.
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- 태그 목록
     */
    @Cacheable(value="jrnlIntrptYyMnthTagListByUser", key="new org.springframework.cache.interceptor.SimpleKey(#userId, #yy, #mnth)")
    public List<TagDto> getListDtoWithCacheByUser(final String userId, final Integer yy, final Integer mnth) throws Exception {
        final JrnlIntrptSearchParam searchParam = JrnlIntrptSearchParam.builder().yy(yy).mnth(mnth).build();
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));

        return this.getSelf().getListDto(searchParam);
    }

    /**
     * css 사이즈 계산한 일기 태그 목록 조회
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- CSS 사이즈가 적용된 태그 목록
     */
    public List<TagDto> getMyIntrptSizedListDto(final Integer yy, final Integer mnth) throws Exception {
        final String userId = AuthUtils.getLgnUserId();
        return this.getSelf().getIntrptSizedListDtoByUser(userId, yy, mnth);
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
    @Cacheable(value="jrnlIntrptYyMnthSizedTagListByUser", key="new org.springframework.cache.interceptor.SimpleKey(#userId, #yy, #mnth)")
    public List<TagDto> getIntrptSizedListDtoByUser(final String userId, final Integer yy, final Integer mnth) throws Exception {
        // 저널 꿈 태그 Dto 목록 조회
        final List<TagDto> tagList = this.getSelf().getListDtoWithCacheByUser(userId, yy, mnth);

        final int maxSize = this.calcMaxSize(tagList, AuthUtils.requireUserId(userId), yy, mnth);
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
     * 최대 사용빈도 계산한 일기 태그 목록 조회
     *
     * @param tagList 태그 목록
     * @param yy 조회할 년도
     * @param mnth 조회할 월
     * @return {@link Integer} -- 태그 목록에서 계산된 최대 사용 빈도 (Integer)
     */
    public Integer calcMaxSize(final List<TagDto> tagList, final String userId, Integer yy, Integer mnth) {
        if (CollectionUtils.isEmpty(tagList)) return 0;

        int maxFrequency = 0;

        final JrnlIntrptTagContentParam param = JrnlIntrptTagContentParam.builder()
                .yy(yy)
                .mnth(mnth)
                .regstrId(AuthUtils.requireUserId(userId))
                .build();
        final Map<Integer, Integer> tagCntMap = this.getSelf().countIntrptSizeMap(param);

        for (final TagDto tag : tagList) {
            final Integer diarySize = tagCntMap.getOrDefault(tag.getTagNo(), 0);
            tag.setContentSize(diarySize);
            maxFrequency = Math.max(maxFrequency, diarySize);
        }

        return maxFrequency;
    }

    /**
     * 일기 태그별 크기 맵 조회
     *
     * @return {@link Map} -- 카테고리별 태그 목록을 담은 Map
     */
    @Cacheable(value="jrnlIntrptCountMapByUser", key="new org.springframework.cache.interceptor.SimpleKey(#param.regstrId, #param.yy, #param.mnth)")
    public ConcurrentHashMap<Integer, Integer> countIntrptSizeMap(final JrnlIntrptTagContentParam param) {
        final List<TagContentCntDto> tagCountList = repository.countIntrptSizeMap(param);

        // List를 태그 번호를 키로 하고, 태그 개수를 값으로 하는 Map으로 변환
        final ConcurrentMap<Integer, Integer> concurrentMap = tagCountList.stream()
                .collect(Collectors.toConcurrentMap(
                        TagContentCntDto::getTagNo,
                        dto -> dto.getCount().intValue()   // Long을 int로 변환
                ));
        return new ConcurrentHashMap<>(concurrentMap);
    }

    public Map<String, List<TagDto>> getMyIntrptSizedGroupListDto(final Integer yy, final Integer mnth) throws Exception {
        final String userId = AuthUtils.getLgnUserId();
        return this.getIntrptSizedGroupListDtoByUser(userId, yy, mnth);
    }

    /**
     * 지정된 연도와 월을 기준으로 태그 목록을 카테고리별로 그룹화하여 반환합니다.
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link Map} -- 카테고리별로 그룹화된 태그 목록을 담은 Map
     */
    public Map<String, List<TagDto>> getIntrptSizedGroupListDtoByUser(final String userId, final Integer yy, final Integer mnth) throws Exception {
        final List<TagDto> tagList = this.getSelf().getIntrptSizedListDtoByUser(AuthUtils.requireUserId(userId), yy, mnth);

        // 태그를 카테고리별로 그룹화하여 맵으로 반환
        return tagList.stream()
                .collect(Collectors.groupingBy(TagDto::getCtgr));
    }

    /**
     * 내 태그 카테고리 맵을 반환합니다.
     *
     * @return {@link Map} -- 태그 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    public Map<String, List<String>> getMyTagCtgrMap() throws Exception {
        final String userId = AuthUtils.getLgnUserId();
        return this.getSelf().getTagCtgrMapByUser(userId);
    }

    /**
     * 태그 카테고리 맵을 반환합니다.
     *
     * @param userId 사용자 아이디
     * @return {@link Map} -- 태그 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    @Cacheable(value="jrnlIntrptTagCtgrMapByUser", key="#userId")
    public Map<String, List<String>> getTagCtgrMapByUser(final String userId) throws Exception {
        final HashMap<String, Object> paramMap = new HashMap<>() {{
            put("regstrId", AuthUtils.requireUserId(userId));
        }};

        final List<JrnlIntrptTagEntity> tagList = this.getSelf().getListEntity(paramMap);
        return tagList.stream()
                .collect(Collectors.groupingBy(
                        JrnlIntrptTagEntity::getTagNm,
                        Collectors.mapping(tag -> {
                            if (StringUtils.isBlank(tag.getCtgr())) return "";
                            return tag.getCtgr();
                        }, Collectors.toList())
                ));
    }
}
