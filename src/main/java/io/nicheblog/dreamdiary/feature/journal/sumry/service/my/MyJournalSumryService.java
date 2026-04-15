package io.nicheblog.dreamdiary.feature.journal.sumry.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.sumry.model.JournalSumryDto;
import io.nicheblog.dreamdiary.feature.journal.sumry.service.JournalSumryService;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJournalSumryService
 * <pre>
 *  로그인 사용자 기준 저널 결산 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJournalSumryService {

    private final JournalSumryService journalSumryService;

    /**
     * 저널 결산 정뵤 목록 조회 :: 캐시 사용 위해 구현체로 pullUp
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link List< JournalSumryDto >} -- 검색 조건에 맞는 결산 목록 Dto 리스트
     */
    public List<JournalSumryDto> getMyListDto(final BaseSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return journalSumryService.getListDtoByUser(username, searchParam);
    }

    /**
     * 관련 정보를 취합하여 총 저널 결산 정보를 생성합니다. (캐시 처리)
     *
     * @return {@link JournalSumryDto} -- 총 결산 정보가 담긴 Dto 객체
     */
    public JournalSumryDto getMyTotalSumry() {
        final String username = AuthUtils.requireLgnUsername();
        return journalSumryService.getTotalSumryByUser(username);
    }

    /**
     * 저널 결산 상세 정보 조회 (캐시 처리)
     *
     * @param key 식별자
     * @return {@link JournalSumryDto} -- 조회된 결산 정보가 담긴 Dto 객체
     */
    public JournalSumryDto getMySumryDtl(final Integer key) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return journalSumryService.getSumryDtlByUser(username, key);
    }

    /**
     * 년도별 저널 결산 정보 조회 (캐시 처리)
     *
     * @param yy 조회할 년도
     * @return {@link JournalSumryDto} -- 조회된 결산 정보가 담긴 Dto 객체, 없을 경우 null 반환
     */
    public JournalSumryDto getMyDtlDtoByYy(final Integer yy) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return journalSumryService.getDtlDtoByYyByUser(username, yy);
    }

    /**
     * 년도를 받아서 해당 년도 저널 결산 정보 생성
     *
     * @return {@link Boolean} -- 결산 생성 성공 여부 (항상 true 반환)
     */
    public Boolean makeMyYySumry(final Integer yy) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return journalSumryService.makeYySumryByUser(username, yy);
    }

    /**
     * 전체 년도 저널 결산 정보 생성
     *
     * @return {@link Boolean} -- 결산 생성 성공 여부 (항상 true 반환)
     */
    public Boolean makeMyTotalYySumry() throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        final Boolean result = journalSumryService.makeTotalYySumryByUser(username);
        EhCacheUtils.clearMyCache("journalSumryDtlDtoByUser");
        EhCacheUtils.clearMyCache("journalSumryYyDtlDtoByUser");
        return result;
    }

}

