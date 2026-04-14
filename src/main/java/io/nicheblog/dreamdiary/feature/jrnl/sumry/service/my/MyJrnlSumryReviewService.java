package io.nicheblog.dreamdiary.feature.jrnl.sumry.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.model.JrnlSumryReviewDto;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.service.JrnlSumryReviewService;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJrnlSumryReviewService
 * <pre>
 *  로그인 사용자 기준 저널 결산 리뷰 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlSumryReviewService {

    private final JrnlSumryReviewService jrnlSumryReviewService;

    /**
     * 저널 결산 리뷰 정뵤 목록 조회
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link List<JrnlSumryReviewDto>} -- 검색 조건에 맞는 결산 목록 Dto 리스트
     */
    public List<JrnlSumryReviewDto> getMyListDto(final BaseSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return jrnlSumryReviewService.getListDtoByUser(username, searchParam);
    }
}
