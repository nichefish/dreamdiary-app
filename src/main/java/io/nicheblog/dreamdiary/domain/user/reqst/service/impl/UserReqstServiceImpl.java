package io.nicheblog.dreamdiary.domain.user.reqst.service.impl;

import io.nicheblog.dreamdiary.domain.user.info.entity.UserAuthRoleEntity;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserStusEmbed;
import io.nicheblog.dreamdiary.domain.user.info.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.domain.user.info.service.UserService;
import io.nicheblog.dreamdiary.domain.user.reqst.mapstruct.UserReqstMapstruct;
import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDto;
import io.nicheblog.dreamdiary.domain.user.reqst.service.UserReqstService;
import io.nicheblog.dreamdiary.global.Constant;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserReqstService
 * <pre>
 *  사용자 계정 신청 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("userReqstService")
@RequiredArgsConstructor
public class UserReqstServiceImpl
        implements UserReqstService {

    private final UserService userService;
    private final UserReqstMapstruct userReqstMapstruct = UserReqstMapstruct.INSTANCE;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 신청 전처리.
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final UserReqstDto registDto) throws Exception {
        // 접속 IP 정보 없을시 사용으로 찍었더라도 미사용으로 변경
        if (StringUtils.isEmpty(registDto.getAcsIpListStr())) {
            registDto.setUseAcsIpYn("N");
            registDto.setAcsIpListStr(null);
        }
    }

    /**
     * 사용자 관리 > 사용자 신규계정 신청
     * 계정 기본정보만 입력, 세부정보는 가입 승인 후 수정
     * (등록 과정과 거의 동일하지만 일단 프로세스 분리)
     *
     * @param registDto 등록할 객체
     * @return {@link UserReqstDto} -- 성공 결과 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    public UserReqstDto regist(final UserReqstDto registDto) throws Exception {
        // 전처리 (메소드 분리)
        this.preRegist(registDto);

        // Dto -> Entity 변환
        UserEntity registEntity = userReqstMapstruct.toEntity(registDto);
        // userReqstEntity.setUserProflNo(this.userInfoReg(userReqstEntity, userReqst));
        registEntity.setAuthList(List.of(new UserAuthRoleEntity(Constant.AUTH_USER)));
        registEntity.setPassword(passwordEncoder.encode(registDto.getPassword()));
        registEntity.setAcntStus(UserStusEmbed.getReqstStus());
        registEntity.cascade();
        // insert
        UserEntity updatedEntity = userRepository.save(registEntity);

        return userReqstMapstruct.toDto(updatedEntity);
    }

    /**
     * 사용자 정보 승인 처리.
     *
     * @param key 사용자 번호
     * @return {@link Boolean} 처리 성공 여부
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    public Boolean cf(final Integer key) throws Exception {
        // Entity 레벨 조회
        final UserEntity retrievedEntity = userService.getDtlEntity(key);
        if (retrievedEntity == null) return false;

        // lockedYn 플래그 업데이트
        retrievedEntity.acntStus.setCfYn("Y");
        retrievedEntity.acntStus.setLockedYn("N");
        retrievedEntity.acntStus.setLgnFailCnt(0);
        final UserEntity updatedEntity = userRepository.saveAndFlush(retrievedEntity);

        return updatedEntity.getUserNo() != null;
    }

    /**
     * 사용자 정보 승인 취소 처리.
     *
     * @param key 사용자 번호
     * @return {@link Boolean} 처리 성공 여부
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    public Boolean uncf(final Integer key) throws Exception {
        // Entity 레벨 조회
        final UserEntity retrievedEntity = userService.getDtlEntity(key);
        if (retrievedEntity == null) return false;

        // lockedYn 플래그 업데이트
        retrievedEntity.acntStus.setCfYn("N");
        retrievedEntity.acntStus.setLockedYn("Y");
        retrievedEntity.acntStus.setLgnFailCnt(0);
        final UserEntity updatedEntity = userRepository.saveAndFlush(retrievedEntity);

        return updatedEntity.getUserNo() != null;
    }
}
