package io.nicheblog.dreamdiary.adapter.kasi.service;

import io.nicheblog.dreamdiary.adapter.kasi.model.HldyKasiApiItemDto;

import java.util.List;

/**
 * HldyKasiApiService
 * <pre>
 *  API:: 한국천문연구원(KASI):: 휴일 정보 조회 서비스 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface HldyKasiApiService {

    /**
     * API로 받아온 휴일 정보를 DB에서 삭제 후 재등록
     * <pre>
         * 1. 기존 데이터 삭제
         * 2. 새로운 휴일 정보를 API에서 조회
         * 3. 조회된 데이터를 DB에 저장
     * </pre>
     *
     * @param yyStr 조회 및 처리할 연도 (String) - 없으면 현재 연도를 사용
     * @return {@link Boolean} -- 성공적으로 처리된 경우 true
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    Boolean procHldyList(final String yyStr) throws Exception;

    /**
     * API:: 한국천문연구원(KASI):: 휴일 정보 조회
     *
     * @param yyParam 조회할 연도 (String), 비어 있을 경우 현재 연도로 설정
     * @return {@link List} -- 휴일 정보 리스트
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    List<HldyKasiApiItemDto> getHldyList(final String yyParam) throws Exception;

    /**
     * API:: 한국천문연구원(KASI):: 휴일 정보 받아와서 DB 저장
     *
     * @param hldyApiList 휴일 정보 리스트
     * @return {@link Boolean} -- 성공적으로 저장된 경우 true
     * @throws Exception 저장 중 발생할 수 있는 예외
     */
    Boolean regHldyList(final List<HldyKasiApiItemDto> hldyApiList) throws Exception;

    /**
     * API:: 한국천문연구원(KASI):: API 조회 휴일 정보 DB 삭제
     *
     * @param yyStr 삭제할 연도 (String)
     * @throws Exception 삭제 중 발생할 수 있는 예외
     */
    void delHldyList(final String yyStr) throws Exception;
}
