package io.nicheblog.dreamdiary.feature.user.account.service;

import io.nicheblog.dreamdiary.auth.policy.entity.AuthPolicyEntity;
import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyQueryService;
import io.nicheblog.dreamdiary.auth.security.entity.RoleEntity;
import io.nicheblog.dreamdiary.auth.security.repository.jpa.RoleRepository;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserStateEntity;
import io.nicheblog.dreamdiary.feature.user.account.mapstruct.UserMapstruct;
import io.nicheblog.dreamdiary.feature.user.account.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.feature.user.account.spec.UserSpec;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * UserService
 * <pre>
 *  사용자 관리 > 계정 및 권한 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class UserService
        implements BaseAttachableService<UserDto, UserDto, Integer, UserEntity>, BaseMultipartWritableService<UserDto, UserDto, Integer, UserEntity> {

    @Getter
    private final UserRepository repository;
    @Getter
    private final UserSpec spec;
    @Getter
    private final UserMapstruct mapstruct = UserMapstruct.INSTANCE;

    public UserMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public UserMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final AuthPolicyQueryService authPolicyQueryService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int PASSWORD_RESET_TOKEN_RANDOM_BYTES = 32;
    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();

    /** 관리자 '비밀번호 초기화' 시 적용할 임시 비밀번호(평문) — 초기 설치와 동일 설정 사용 */
    @Value("${app.auth.initial-admin-password:}")
    private String systemInitTempPw;

    /**
     * 사용자 관리 > 사용자 단일 조회 (Dto Level) (Long id와 별도로 String username)
     *
     * @param username 조회할 사용자의 계정명 (문자열)
     * @return {@link UserDto} -- 사용자 정보가 담긴 Dto 객체
     */
    public UserDto getDtlDto(final String username) throws Exception {
        // Entity 레벨 조회
        final UserEntity rsUserEntity = this.getDtlEntity(username);

        return mapstruct.toDto(rsUserEntity);
    }

    /**
     * 사용자 관리 > 사용자 단일 조회 (Entity Level) (Long id와 별도로 String username)
     *
     * @param username 조회할 사용자의 계정명 (문자열 형식)
     * @return {@link UserEntity} -- 사용자 정보를
     * @throws NullPointerException 사용자 정보가 존재하지 않을 경우 발생
     */
    public UserEntity getDtlEntity(final String username) throws Exception {
        final Optional<UserEntity> retrievedWrapper = repository.findByUsername(username);

        return Objects.requireNonNull(retrievedWrapper.orElseThrow(() -> new EntityNotFoundException("exception.UsernameNotFoundException")));
    }

    /* ----- */

    /**
     * 사용자 관리 > 사용자 ID 중복 체크
     *
     * @param username 중복을 확인할 사용자 계정명 (문자열 형식)
     * @return {@link Boolean} -- 중복 여부
     */
    public Boolean usernameDupChck(final String username) {
        return repository.findByUsername(username).isPresent();
    }

    /**
     * 사용자 관리 > 사용자 Email 중복 체크
     *
     * @param email 중복을 확인할 이메일 (문자열 형식)
     * @return {@link Boolean} -- 중복 여부
     */
    public Boolean emailDupChck(String email) {
        return repository.findByEmail(email).isPresent();
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final UserDto registDto) throws Exception {
        // 접속 IP 정보 없을시 사용으로 찍었더라도 미사용으로 변경
        if (StringUtils.isEmpty(registDto.getAllowedIpListStr())) {
            registDto.setUseAllowedIpYn("N");
            registDto.setAllowedIpListStr(null);
        }
    }

    /**
     * 등록 중간처리. (override)
     *
     * @param registEntity 등록 전 entity 객체
     */
    @Override
    public void preRegist(final UserEntity registEntity) throws Exception {
        // 접속 IP 정보 없을시 사용으로 찍었더라도 미사용으로 변경
        registEntity.setPassword(passwordEncoder.encode(registEntity.getPassword()));
        registEntity.setAcntStus(UserStateEntity.getRegistStus());
        this.applyRoleIds(registEntity);
        registEntity.cascade();
    }

    /**
     * 사용자 관리 > 사용자 비밀번호 초기화
     * @param key 식별자
     */
    @Transactional
    public ServiceResponse passwordReset(final Integer key) throws Exception {
        // Entity 레벨 조회
        final UserEntity retrievedEntity = this.getDtlEntity(key);
        if (retrievedEntity == null) throw new EntityNotFoundException("exception.EntityNotFoundException");

        if (StringUtils.isBlank(systemInitTempPw)) {
            throw new IllegalStateException("msg.user.pw.init-temp-pw.not-set");
        }
        retrievedEntity.setPassword(passwordEncoder.encode(systemInitTempPw));
        retrievedEntity.acntStus.setNeedsPasswordReset("Y");
        retrievedEntity.acntStus.setPasswordToken(null);
        retrievedEntity.acntStus.setPasswordResetTokenIssuedAt(DateUtils.getCurrDate());
        retrievedEntity.acntStus.setPasswordChangedAt(DateUtils.getCurrDate());
        final UserEntity updatedEntity = repository.saveAndFlush(retrievedEntity);

        return ServiceResponse.builder()
                .rslt(updatedEntity.getId() != null)
                .build();
    }

    /**
     * 패스워드 리셋 토큰 발급.
     * 실제 토큰은 응답으로만 돌려주고 DB에는 SHA-256 해시만 저장한다.
     */
    @Transactional
    public String issuePasswordResetToken(final String username) throws Exception {
        final UserEntity user = this.getDtlEntity(username);
        final String passwordToken = this.generatePasswordResetToken();

        if (user.acntStus == null) user.acntStus = UserStateEntity.builder().build();
        user.acntStus.setPasswordToken(this.hashPasswordResetToken(passwordToken));
        user.acntStus.setPasswordResetTokenIssuedAt(DateUtils.getCurrDate());
        repository.saveAndFlush(user);

        return passwordToken;
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 수정할 객체
     */
    @Override
    public void preModify(final UserDto modifyDto) throws Exception {
        // 접속 IP 정보 없을시 사용으로 찍었더라도 미사용으로 변경
        if (StringUtils.isEmpty(modifyDto.getAllowedIpListStr())) {
            modifyDto.setUseAllowedIpYn("N");
            modifyDto.setAllowedIpListStr(null);
        }
    }

    /**
     * 사용자 관리 > 사용자 수정
     *
     * @param modifyDto 수정할 객체
     */
    @Override
    @Transactional
    public ServiceResponse modify(final UserDto modifyDto) throws Exception {
        final UserEntity modifyEntity = this.getDtlEntity(modifyDto.getKey());
        mapstruct.updateFromDto(modifyDto, modifyEntity);
        this.applyRoleIds(modifyEntity);

        // update
        final UserEntity updatedEntity = this.updt(modifyEntity);
        final UserDto updatedDto = mapstruct.toDto(updatedEntity);

        return ServiceResponse.builder()
                .rslt(updatedEntity.getId() != null)
                .rsltObj(updatedDto)
                .build();
    }

    /**
     * 장기간 미접속여부 조회
     */
    public Boolean isDormant(final String username) throws Exception {
        if (StringUtils.isEmpty(username)) return false;
        if (Constant.SYSTEM_ACNT.equals(username) || Constant.DEV_ACNT.equals(username)) return false;

        final AuthPolicyEntity authPolicy = authPolicyQueryService.getDtlEntity();
        final Integer inactiveLockDays = authPolicy.getInactiveLockDays();

        final UserEntity user = this.getDtlEntity(username);
        Date lastLoginDt = user.acntStus.getLastLoginAt();
        if (lastLoginDt == null) lastLoginDt = user.getCreatedAt();
        final Date dormantDt = DateUtils.getDateAddDay(lastLoginDt, inactiveLockDays);

        return (dormantDt == null || dormantDt.compareTo(DateUtils.getCurrDate()) < 0);
    }

    /**
     * 사용자 정보 잠금
     */
    @Transactional
    public ServiceResponse userLock(final Integer key) throws Exception {
        // Entity 레벨 조회
        final UserEntity retrievedEntity = this.getDtlEntity(key);
        if (retrievedEntity == null) throw new EntityNotFoundException("exception.EntityNotFoundException");

        // lockedYn 플래그 업데이트
        retrievedEntity.acntStus.setLockedYn("Y");
        retrievedEntity.acntStus.setLoginFailCnt(0);
        final UserEntity updatedEntity = repository.saveAndFlush(retrievedEntity);

        return ServiceResponse.builder()
                .rslt(updatedEntity.getId() != null)
                .build();
    }

    /**
     * 사용자 정보 잠금 해제
     */
    @Transactional
    public ServiceResponse userUnlock(final Integer key) throws Exception {
        // Entity 레벨 조회
        final UserEntity retrievedEntity = this.getDtlEntity(key);
        if (retrievedEntity == null) throw new EntityNotFoundException("exception.EntityNotFoundException");

        // lockedYn 플래그 + 최종접속일 업데이트
        retrievedEntity.acntStus.setLockedYn("N");
        retrievedEntity.acntStus.setLoginFailCnt(0);
        retrievedEntity.acntStus.setLastLoginAt(DateUtils.getCurrDate());
        final UserEntity updatedEntity = repository.saveAndFlush(retrievedEntity);

        return ServiceResponse.builder()
                .rslt(updatedEntity.getId() != null)
                .build();
    }

    /**
     * 내부직원 목록 조회 (결재자 정보에 쓰임) (계정정보로 조회)
     *
     * @param yyStr (년도)
     */
    public List<UserDto> getCrdtUserList(final String yyStr) throws Exception {
        if (StringUtils.isEmpty(yyStr)) return null;
        // 목록 검색
        String startDtStr = yyStr + "-01-01";
        String endDtStr = yyStr + "-12-31";

        return this.getCrdtUserList(startDtStr, endDtStr);
    }

    /**
     * 내부직원 목록 조회 (결재자 정보에 쓰임) (계정정보로 조회)
     *
     * @param startDtStr : 시작일자yyyy-MM-dd
     * @param endDtStr : 종료일자yyyy-MM-dd
     */
    public List<UserDto> getCrdtUserList(final String startDtStr, final String endDtStr) throws Exception {
        // 목록 검색
        final List<UserEntity> entityList = repository.findAll(spec.searchCrdtUser(startDtStr, endDtStr));
        return mapstruct.toDtoList(entityList);
    }

    /**
     * 관련된 캐시 삭제
     *
     * @param rslt 캐시 삭제 판단에 필요한 객체
     */
    public void evictCache(final UserEntity rslt) {
        EhCacheUtils.evictCacheByKey("auditorInfo", rslt.getUsername());
    }

    /**
     * 사용자 역할 목록의 role_key(요청 문자열)를 기준으로 role_id를 채운다. (user_role 테이블에는 role_id만 저장)
     */
    private void applyRoleIds(final UserEntity userEntity) {
        if (userEntity == null || userEntity.getUserRoles() == null) return;

        userEntity.getUserRoles().forEach(userRole -> {
            if (userRole == null) return;

            final String roleKey = userRole.getRoleKey();
            if (StringUtils.isEmpty(roleKey)) return;

            final RoleEntity role = roleRepository.findByRoleKey(roleKey);
            if (role != null) {
                userRole.setRoleId(role.getId());
            }
        });
    }

    private String generatePasswordResetToken() {
        final byte[] randomBytes = new byte[PASSWORD_RESET_TOKEN_RANDOM_BYTES];
        secureRandom.nextBytes(randomBytes);
        return BASE64_URL.encodeToString(randomBytes);
    }

    public String hashPasswordResetToken(final String passwordToken) {
        return this.hashToken(passwordToken);
    }

    private String hashToken(final String token) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder(hashed.length * 2);
            for (final byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to hash password reset token.", e);
        }
    }
}
