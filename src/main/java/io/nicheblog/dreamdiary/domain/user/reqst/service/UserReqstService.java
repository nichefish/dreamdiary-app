package io.nicheblog.dreamdiary.domain.user.reqst.service;

import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDto;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserReqstService
 * <pre>
 *  사용자 계정 신청 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface UserReqstService {

    /**
     * 신청 전처리.
     * 
     * @param registDto 등록할 객체
     */
    void preRegist(final UserReqstDto registDto) throws Exception;
    
    /**
     * 사용자 관리 > 사용자 신규계정 신청
     * 계정 기본정보만 입력, 세부정보는 가입 승인 후 수정
     * (등록 과정과 거의 동일하지만 일단 프로세스 분리)
     *
     * @param registDto 등록할 객체
     * @return {@link UserReqstDto} -- 성공 결과 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    UserReqstDto regist(final UserReqstDto registDto) throws Exception;

    /**
     * 사용자 정보 승인 처리.
     *
     * @param key 사용자 번호
     * @return {@link Boolean} 처리 성공 여부
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional
    Boolean cf(final Integer key) throws Exception;

    /**
     * 사용자 정보 승인 취소 처리.
     *
     * @param key 사용자 번호
     * @return {@link Boolean} 처리 성공 여부
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional
    Boolean uncf(final Integer key) throws Exception;
}
