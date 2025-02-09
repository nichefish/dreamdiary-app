package io.nicheblog.dreamdiary.domain.user.info.service;

import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.domain.user.info.mapstruct.UserMapstruct;
import io.nicheblog.dreamdiary.domain.user.info.model.UserDto;
import io.nicheblog.dreamdiary.domain.user.info.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.domain.user.info.spec.UserSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseMultiCrudService;

import java.util.List;

/**
 * UserService
 * <pre>
 *  사용자 관리 > 계정 및 권한 관리 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface UserService
        extends BaseMultiCrudService<UserDto.DTL, UserDto.LIST, Integer, UserEntity, UserRepository, UserSpec, UserMapstruct> {


    /**
     * 사용자 관리 > 사용자 단일 조회 (Dto Level) (Long userNo와 별도로 String userId)
     *
     * @param userId 조회할 사용자의 ID (문자열)
     * @return {@link UserDto.DTL} -- 사용자 정보가 담긴 DTO 객체
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    UserDto.DTL getDtlDto(final String userId) throws Exception;

    /**
     * 사용자 관리 > 사용자 단일 조회 (Entity Level) (Long userNo와 별도로 String userId)
     *
     * @param userId 조회할 사용자의 ID (문자열 형식)
     * @return {@link UserEntity} -- 사용자 정보를
     * @throws NullPointerException 사용자 정보가 존재하지 않을 경우 발생
     * @throws Exception 조회 중 발생할 수 있는 기타 예외
     */
    UserEntity getDtlEntity(final String userId) throws Exception;

    /**
     * 사용자 관리 > 사용자 ID 중복 체크
     *
     * @param userId 중복을 확인할 사용자 ID (문자열 형식)
     * @return {@link Boolean} -- 중복 여부
     */
    Boolean userIdDupChck(final String userId) ;

    /**
     * 사용자 관리 > 사용자 Email 중복 체크
     *
     * @param email 중복을 확인할 이메일 (문자열 형식)
     * @return {@link Boolean} -- 중복 여부
     */
    Boolean emailDupChck(String email);

    /**
     * 사용자 관리 > 사용자 비밀번호 초기화
     * @param key 식별자
     */
    Boolean passwordReset(final Integer key) throws Exception;

    /**
     * 장기간 미접속여부 조회
     */
    Boolean isDormant(final String userId) throws Exception;

    /**
     * 사용자 정보 잠금
     */
    Boolean userLock(final Integer key) throws Exception;

    /**
     * 사용자 정보 잠금 해제
     */
    Boolean userUnlock(final Integer key) throws Exception;

    /**
     * 내부직원 목록 조회 (결재자 정보에 쓰임) (계정정보로 조회)
     *
     * @param yyStr (년도)
     */
    List<UserDto.LIST> getCrdtUserList(final String yyStr) throws Exception;

    /**
     * 내부직원 목록 조회 (결재자 정보에 쓰임) (계정정보로 조회)
     *
     * @param startDtStr : 시작일자yyyy-MM-dd
     * @param endDtStr : 종료일자yyyy-MM-dd
     */
    List<UserDto.LIST> getCrdtUserList(final String startDtStr, final String endDtStr) throws Exception;
}