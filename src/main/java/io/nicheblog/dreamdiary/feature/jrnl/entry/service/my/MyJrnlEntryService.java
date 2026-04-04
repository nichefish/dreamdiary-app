package io.nicheblog.dreamdiary.feature.jrnl.entry.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.feature.jrnl.entry.model.JrnlEntrySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.entry.service.JrnlEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJrnlEntryService
 * <pre>
 *  로그인 사용자 기준 저널 항목 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlEntryService {

    private final JrnlEntryService jrnlEntryService;

    /**
     * 목록 조회 (dto level)
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JrnlEntryDto> getMyListDto(final JrnlEntrySearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlEntryService.getListDtoByUser(userId, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JrnlEntryDto} -- 조회된 객체
     */
    public JrnlEntryDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlEntryService.getDtlDtoWithCacheByUser(userId, key);
    }
}
