package io.nicheblog.dreamdiary.feature.jrnl.intrpt.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.service.JrnlIntrptTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * MyJrnlIntrptTagService
 * <pre>
 *  로그인 사용자 기준 저널 해석 태그 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlIntrptTagService {

    private final JrnlIntrptTagService jrnlIntrptTagService;

    /**
     * css 사이즈 계산된 일기 태그 목록 조회
     * 태그 1개 = 1. 폰트 2~9
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- CSS 사이즈가 적용된 태그 목록
     */
    public List<TagDto> getMyIntrptSizedListDto(final Integer yy, final Integer mnth) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlIntrptTagService.getIntrptSizedListDtoByUser(userId, yy, mnth);
    }

    /**
     * css 사이즈 계산한 일기 태그 목록 조회
     * 태그 1개 = 1. 그 외엔 2~9
     *
     * @param yy 조회할 연도
     * @param mnth 조회할 월
     * @return {@link List} -- CSS 사이즈가 적용된 태그 목록
     */
    public Map<String, List<TagDto>> getMyIntrptSizedGroupListDto(final Integer yy, final Integer mnth) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlIntrptTagService.getIntrptSizedGroupListDtoByUser(userId, yy, mnth);
    }

    /**
     * 내 태그 카테고리 맵을 반환합니다.
     *
     * @return {@link Map} -- 태그 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    public Map<String, List<String>> getMyTagCtgrMap() throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlIntrptTagService.getTagCtgrMapByUser(userId);
    }
}
