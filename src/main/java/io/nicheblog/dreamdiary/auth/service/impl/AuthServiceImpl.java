package io.nicheblog.dreamdiary.auth.service.impl;

import io.nicheblog.dreamdiary.auth.entity.AuditorInfo;
import io.nicheblog.dreamdiary.auth.entity.AuthRoleEntity;
import io.nicheblog.dreamdiary.auth.mapstruct.AuthInfoMapstruct;
import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.repository.jpa.AuthRoleRepository;
import io.nicheblog.dreamdiary.auth.service.AuthService;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.domain.user.info.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * AuthService
 * <pre>
 *  Spring Security:: 인증 및 권한 처리 관련 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl
        implements AuthService {

    private final UserRepository userRepository;
    private final AuthRoleRepository authRoleRepository;
    private final AuthInfoMapstruct mapstruct = AuthInfoMapstruct.INSTANCE;

    /**
     * userId로 계정 + 사용자 정보 조회
     * 로그인 등 인증시 Spring Security에서 사용.
     *
     * @param userId 조회할 사용자의 ID
     * @return {@link AuthInfo} -- Spring Security용 사용자 인증정보 객체
     * @throws UsernameNotFoundException 사용자 정보를 찾을 수 없는 경우
     */
    @Override
    @SneakyThrows
    @Transactional(readOnly = true)
    public AuthInfo loadUserByUsername(final String userId) throws UsernameNotFoundException {
        Optional<UserEntity> rsWrapper = userRepository.findByUserId(userId);
        if (rsWrapper.isEmpty()) throw new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다.");
        UserEntity rsUser = rsWrapper.get();

        // TODO: 사용자 프로필 정보 존재여부 체크
        // Integer userProflNo = rsUserEntity.getUserProflNo();
        // if (userProflNo != null) {
        //     UserProflEntity rsUserInfo = userProflRepository.findById(userProflNo).orElse(null);
        //     rsUserEntity.setUserProfl(rsUserInfo);
        // }

        return AuthInfoMapstruct.INSTANCE.toDto(rsUser);
    }

    /**
     * 로그인 실패시 실패 카운트를 증가시킨다.
     *
     * @param userId 로그인 실패한 사용자 ID
     * @return {@link Integer} -- 업데이트된 로그인 실패 횟수
     */
    @Override
    @Transactional
    public Integer applyLgnFailCnt(final String userId) {
        // ID로 사용자 정보 조회
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        if (userEntityWrapper.isEmpty()) return 0;
        UserEntity userEntity = userEntityWrapper.get();
        // 로그인 실패횟수 조회해서 세팅
        Integer currLgnFailCnt = userEntity.acntStus.getLgnFailCnt();
        Integer newLgnFailCnt = (currLgnFailCnt == null) ? 1 : currLgnFailCnt + 1;
        userEntity.acntStus.setLgnFailCnt(newLgnFailCnt);
        // 저장 후 반환된 값 반환
        UserEntity rsltEntity = userRepository.save(userEntity);
        return rsltEntity.acntStus.getLgnFailCnt();
    }

    /**
     * 계정 잠금 처리
     *
     * @param userId 계정을 잠글 사용자 ID
     */
    @Override
    @Transactional
    public void lockAccount(final String userId) {
        // ID로 사용자 정보 조회
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        UserEntity userEntity = userEntityWrapper.orElseThrow(NullPointerException::new);
        // 계정 잠금 처리
        userEntity.acntStus.setLockedYn("Y");
        userEntity.acntStus.setLgnFailCnt(0);
        userRepository.save(userEntity);
    }

    /**
     * 로그인 성공시 최종 로그인일자 세팅 및 실패 카운트 초기화
     *
     * @param userId 처리할 사용자 ID
     */
    @Override
    @Transactional
    public void setLstLgnDt(final String userId) {
        // ID로 사용자 정보 조회
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        UserEntity userEntity = userEntityWrapper.orElseThrow(NullPointerException::new);
        // 최종 로그인 날짜 세팅 및 실패 카운터 0으로 세팅
        userEntity.acntStus.setLstLgnDt(new Date());
        userEntity.acntStus.setLgnFailCnt(0);
        userRepository.save(userEntity);
    }

    /**
     * 권한 정보 조회
     * TODO: 사이트 커지면 역할 분리해야 함
     *
     * @param authCd 조회할 권한 코드
     * @return {@link AuthRoleEntity} -- 권한 정보 객체
     */
    @Override
    public AuthRoleEntity getAuthRole(final String authCd) {
        return authRoleRepository.findById(authCd).orElse(null);
    }

    /**
     * getAuditorInfo
     *
     * @param userId 사용자 ID
     * @return AuditorInfo
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "auditorInfo", key = "'userId:' + #userId", condition = "#userId!=null")
    public AuditorInfo getAuditorInfo(String userId) {
        Optional<UserEntity> userEntityWrapper = userRepository.findByUserId(userId);
        if (userEntityWrapper.isEmpty()) return null;

        UserEntity userEntity = userEntityWrapper.get();
        return mapstruct.toAuditorInfo(userEntity);
    }
}