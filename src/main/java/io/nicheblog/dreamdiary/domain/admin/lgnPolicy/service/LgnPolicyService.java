package io.nicheblog.dreamdiary.domain.admin.lgnPolicy.service;

import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.entity.LgnPolicyEntity;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.model.LgnPolicyDto;

/**
 * LgnPolicyService
 * <pre>
 *  로그인 정책 정보 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface LgnPolicyService {

    /**
     * 사용자 관리 > 로그인 설정 조회 (Dto 레벨)
     *
     * @return {@link LgnPolicyDto} -- 로그인 설정 정보
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    LgnPolicyDto getDtlDto() throws Exception;

    /**
     * 사용자 관리 > 로그인 설정 조회 (Entity 레벨)
     *
     * @return {@link LgnPolicyEntity} -- 로그인 설정 엔티티
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    LgnPolicyEntity getDtlEntity() throws Exception;

    /**
     * 사용자 관리 > 로그인 설정 정보 등록/수정
     *
     * @param registDto 로그인 정책 정보 Dto
     * @return {@link Boolean} -- 성공 여부를 나타내는 Boolean 값
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    Boolean regist(final LgnPolicyDto registDto) throws Exception;
}
