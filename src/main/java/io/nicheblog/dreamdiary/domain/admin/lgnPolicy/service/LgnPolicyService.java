package io.nicheblog.dreamdiary.domain.admin.lgnPolicy.service;

import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.entity.LgnPolicyEntity;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.model.LgnPolicyDto;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;

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
     */
    LgnPolicyDto getDtlDto() throws Exception;

    /**
     * 사용자 관리 > 로그인 설정 조회 (Entity 레벨)
     *
     * @return {@link LgnPolicyEntity} -- 로그인 설정 엔티티
     */
    LgnPolicyEntity getDtlEntity() throws Exception;

    /**
     * 사용자 관리 > 로그인 설정 정보 등록/수정
     *
     * @param registDto 로그인 정책 정보 Dto
     * @return {@link Boolean} -- 성공 여부를 나타내는 Boolean 값
     */
    ServiceResponse regist(final LgnPolicyDto registDto) throws Exception;
}
