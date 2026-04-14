package io.nicheblog.dreamdiary.feature.jrnl.dream.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.JrnlDreamTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * JrnlDreamTagService
 * <pre>
 *  로그인 사용자 기준 꿈 태그 서비스. (facade)
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlDreamTagService {

    private final JrnlDreamTagService jrnlDreamTagService;

    public List<TagDto> getMyTagList() throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return jrnlDreamTagService.getTagListByUser(username);
    }

    public List<TagDto> getMyListDtoWithCache(final Integer yy, final Integer mnth) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return jrnlDreamTagService.getListDtoWithCacheByUser(username, yy, mnth);
    }

    public List<TagDto> getMyWeeklyListDtoWithCache(final String weekStartDt) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return jrnlDreamTagService.getWeeklyListDtoWithCacheByUser(username, weekStartDt);
    }

    /**
     * css 사이즈 계산한 태그 목록 조회
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- CSS 사이즈가 적용된 태그 목록
     */
    public List<TagDto> getMyDreamSizedListDto(final Integer yy, final Integer mnth) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return jrnlDreamTagService.getDreamSizedListDtoByUser(username, yy, mnth);
    }

    public List<TagDto> getMyWeeklySizedListDto(final String weekStartDt) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return jrnlDreamTagService.getWeeklySizedListDtoByUser(username, weekStartDt);
    }

    /**
     * 지정된 연도와 월을 기준으로 태그 목록을 카테고리별로 그룹화하여 반환합니다.
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link Map} -- 카테고리별로 그룹화된 태그 목록을 담은 Map
     */
    public Map<String, List<TagDto>> getMyDreamSizedGroupListDto(final Integer yy, final Integer mnth) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return jrnlDreamTagService.getDreamSizedGroupListDtoByUser(username, yy, mnth);
    }

    public Map<String, List<TagDto>> getMyWeeklySizedGroupListDto(final String weekStartDt) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return jrnlDreamTagService.getWeeklySizedGroupListDtoByUser(username, weekStartDt);
    }

    /**
     * 내 태그 카테고리 맵을 반환합니다.
     *
     * @return {@link Map} -- 태그 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    public Map<String, List<String>> getMyTagCtgrMap() throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return jrnlDreamTagService.getTagCtgrMapByUser(username);
    }
}
