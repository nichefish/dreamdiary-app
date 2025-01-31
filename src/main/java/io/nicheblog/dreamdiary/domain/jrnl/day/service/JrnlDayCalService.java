package io.nicheblog.dreamdiary.domain.jrnl.day.service;

import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar.BaseCalDto;

import java.util.List;

/**
 * JrnlDayService
 * <pre>
 *  저널 일자 관리 서비스 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface JrnlDayCalService {

    /**
     * 전체 목록 (저널 일자 및 휴가) 데이터 조회
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 일정 및 휴가 목록
     * @throws Exception 조회 및 처리 중 발생할 수 있는 예외
     */
    List<BaseCalDto> getSchdulTotalCalList(final JrnlDaySearchParam searchParam) throws Exception;

    /**
     * 내 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    List<BaseCalDto> getMyCalListDto(final JrnlDaySearchParam searchParam) throws Exception;
}