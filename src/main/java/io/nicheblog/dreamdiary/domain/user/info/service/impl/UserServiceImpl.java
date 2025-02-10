package io.nicheblog.dreamdiary.domain.user.info.service.impl;

import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.entity.LgnPolicyEntity;
import io.nicheblog.dreamdiary.domain.admin.lgnPolicy.service.LgnPolicyService;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserStusEmbed;
import io.nicheblog.dreamdiary.domain.user.info.mapstruct.UserMapstruct;
import io.nicheblog.dreamdiary.domain.user.info.model.UserDto;
import io.nicheblog.dreamdiary.domain.user.info.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.domain.user.info.service.UserService;
import io.nicheblog.dreamdiary.domain.user.info.spec.UserSpec;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
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
@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl
        implements UserService {

    @Getter
    private final UserRepository repository;
    @Getter
    private final UserSpec spec;
    @Getter
    private final UserMapstruct mapstruct = UserMapstruct.INSTANCE;

    private final LgnPolicyService lgnPolicyService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 관리 > 사용자 단일 조회 (Dto Level) (Long userNo와 별도로 String userId)
     *
     * @param userId 조회할 사용자의 ID (문자열)
     * @return {@link UserDto.DTL} -- 사용자 정보가 담긴 DTO 객체
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    public UserDto.DTL getDtlDto(final String userId) throws Exception {
        // Entity 레벨 조회
        final UserEntity rsUserEntity = this.getDtlEntity(userId);

        return mapstruct.toDto(rsUserEntity);
    }

    /**
     * 사용자 관리 > 사용자 단일 조회 (Entity Level) (Long userNo와 별도로 String userId)
     *
     * @param userId 조회할 사용자의 ID (문자열 형식)
     * @return {@link UserEntity} -- 사용자 정보를
     * @throws NullPointerException 사용자 정보가 존재하지 않을 경우 발생
     * @throws Exception 조회 중 발생할 수 있는 기타 예외
     */
    @Override
    public UserEntity getDtlEntity(final String userId) throws Exception {
        final Optional<UserEntity> retrievedWrapper = repository.findByUserId(userId);

        return Objects.requireNonNull(retrievedWrapper.orElseThrow(() -> new EntityNotFoundException(MessageUtils.getExceptionMsg("UsernameNotFoundException"))));
    }

    /* ----- */

    /**
     * 사용자 관리 > 사용자 ID 중복 체크
     *
     * @param userId 중복을 확인할 사용자 ID (문자열 형식)
     * @return {@link Boolean} -- 중복 여부
     */
    @Override
    public Boolean userIdDupChck(final String userId) {
        return repository.findByUserId(userId).isPresent();
    }

    /**
     * 사용자 관리 > 사용자 Email 중복 체크
     *
     * @param email 중복을 확인할 이메일 (문자열 형식)
     * @return {@link Boolean} -- 중복 여부
     */
    @Override
    public Boolean emailDupChck(String email) {
        return repository.findByEmail(email).isPresent();
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final UserDto.DTL registDto) throws Exception {
        // 접속 IP 정보 없을시 사용으로 찍었더라도 미사용으로 변경
        if (StringUtils.isEmpty(registDto.getAcsIpListStr())) {
            registDto.setUseAcsIpYn("N");
            registDto.setAcsIpListStr(null);
        }
    }

    /**
     * 등록 중간처리. (override)
     *
     * @param registEntity 등록 전 entity 객체
     */
    @Override
    public void midRegist(final UserEntity registEntity) throws Exception {
        // 접속 IP 정보 없을시 사용으로 찍었더라도 미사용으로 변경
        registEntity.setPassword(passwordEncoder.encode(registEntity.getPassword()));
        registEntity.setAcntStus(UserStusEmbed.getRegistStus());
        registEntity.cascade();
    }

    /**
     * 사용자 관리 > 사용자 비밀번호 초기화
     * @param key 식별자
     */
    @Override
    @Transactional
    public Boolean passwordReset(final Integer key) throws Exception {
        // Entity 레벨 조회
        final UserEntity retrievedEntity = this.getDtlEntity(key);
        if (retrievedEntity == null) return false;

        // 로그인 설정 조회 (cachable)
        final LgnPolicyEntity lgnPolicy = lgnPolicyService.getDtlEntity();
        final String pwForReset = lgnPolicy.getPwForReset();
        // update
        retrievedEntity.setPassword(pwForReset);
        retrievedEntity.acntStus.setNeedsPwReset("Y");
        retrievedEntity.acntStus.setPwChgDt(DateUtils.getCurrDate());
        final UserEntity updatedEntity = repository.saveAndFlush(retrievedEntity);

        return (updatedEntity.getUserNo() != null);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 수정할 객체
     */
    @Override
    public void preModify(final UserDto.DTL modifyDto) throws Exception {
        // 접속 IP 정보 없을시 사용으로 찍었더라도 미사용으로 변경
        if (StringUtils.isEmpty(modifyDto.getAcsIpListStr())) {
            modifyDto.setUseAcsIpYn("N");
            modifyDto.setAcsIpListStr(null);
        }
    }

    /**
     * 사용자 관리 > 사용자 수정
     *
     * @param modifyDto 수정할 객체
     */
    @Override
    @Transactional
    public UserDto.DTL modify(final UserDto.DTL modifyDto) throws Exception {
        final UserEntity modifyEntity = this.getDtlEntity(modifyDto);
        mapstruct.updateFromDto(modifyDto, modifyEntity);

        // update
        final UserEntity updatedEntity = this.updt(modifyEntity);
        final UserDto.DTL updatedDto = mapstruct.toDto(updatedEntity);
        updatedDto.setIsSuccess((updatedEntity.getUserNo() != null));

        return updatedDto;
    }

    /**
     * 장기간 미접속여부 조회
     */
    @Override
    public Boolean isDormant(final String userId) throws Exception {
        if (StringUtils.isEmpty(userId)) return false;
        if (Constant.SYSTEM_ACNT.equals(userId) || Constant.DEV_ACNT.equals(userId)) return false;

        final LgnPolicyEntity lgnPolicy = lgnPolicyService.getDtlEntity();
        final Integer lgnLockDy = lgnPolicy.getLgnLockDy();

        final UserEntity user = this.getDtlEntity(userId);
        Date lastLgnDt = user.acntStus.getLstLgnDt();
        if (lastLgnDt == null) lastLgnDt = user.getRegDt();
        final Date dormantDt = DateUtils.getDateAddDay(lastLgnDt, lgnLockDy);

        return (dormantDt == null || dormantDt.compareTo(DateUtils.getCurrDate()) < 0);
    }

    /**
     * 사용자 정보 잠금
     */
    @Override
    @Transactional
    public Boolean userLock(final Integer key) throws Exception {
        // Entity 레벨 조회
        final UserEntity retrievedEntity = this.getDtlEntity(key);
        if (retrievedEntity == null) return false;

        // lockedYn 플래그 업데이트
        retrievedEntity.acntStus.setLockedYn("Y");
        retrievedEntity.acntStus.setLgnFailCnt(0);
        final UserEntity updatedEntity = repository.saveAndFlush(retrievedEntity);

        return updatedEntity.getUserNo() != null;
    }

    /**
     * 사용자 정보 잠금 해제
     */
    @Override
    @Transactional
    public Boolean userUnlock(final Integer key) throws Exception {
        // Entity 레벨 조회
        final UserEntity retrievedEntity = this.getDtlEntity(key);
        if (retrievedEntity == null) return false;

        // lockedYn 플래그 + 최종접속일 업데이트
        retrievedEntity.acntStus.setLockedYn("N");
        retrievedEntity.acntStus.setLgnFailCnt(0);
        retrievedEntity.acntStus.setLstLgnDt(DateUtils.getCurrDate());
        final UserEntity updatedEntity = repository.saveAndFlush(retrievedEntity);

        return updatedEntity.getUserNo() != null;
    }

    /**
     * 내부직원 목록 조회 (결재자 정보에 쓰임) (계정정보로 조회)
     *
     * @param yyStr (년도)
     */
    @Override
    public List<UserDto.LIST> getCrdtUserList(final String yyStr) throws Exception {
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
    @Override
    public List<UserDto.LIST> getCrdtUserList(final String startDtStr, final String endDtStr) throws Exception {
        // 목록 검색
        List<UserEntity> userEntityList = repository.findAll(spec.searchCrdtUser(startDtStr, endDtStr));

        // List<Entity> -> List<ListDto>
        return this.listEntityToDto(userEntityList);
    }

    /**
     * 관련된 캐시 삭제
     *
     * @param rslt 캐시 삭제 판단에 필요한 객체
     */
    @Override
    public void evictCache(final UserEntity rslt) {
        EhCacheUtils.evictCache("auditorInfo", "userId:"+rslt.getUserId());
        EhCacheUtils.clearL2Cache(AuditorInfo.class);
    }
}