package io.nicheblog.dreamdiary.feature.user.reqst.service;

import io.nicheblog.dreamdiary.feature.user.info.entity.UserAuthRoleEntity;
import io.nicheblog.dreamdiary.feature.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.info.entity.UserStateEntity;
import io.nicheblog.dreamdiary.feature.user.info.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.feature.user.reqst.entity.UserSignupRequestEntity;
import io.nicheblog.dreamdiary.feature.user.reqst.model.UserReqstDto;
import io.nicheblog.dreamdiary.feature.user.reqst.repository.jpa.UserSignupRequestRepository;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

/**
 * UserReqstService
 * <pre>
 *  사용자 계정 신청 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class UserReqstService {

    private final UserRepository userRepository;
    private final UserSignupRequestRepository userSignupRequestRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 신청 전처리. (dto level)
     *
     * @param registDto 등록할 객체
     */
    public void preRegist(final UserReqstDto registDto) throws Exception {
        // 접속 IP 정보 없을시 사용으로 찍었더라도 미사용으로 변경
        if (StringUtils.isEmpty(registDto.getAllowedIpListStr())) {
            registDto.setUseAllowedIpYn("N");
            registDto.setAllowedIpListStr(null);
        }
    }

    /**
     * 신청 전처리. (Entity level)
     *
     * @param registEntity 등록할 객체
     */
    public void preRegist(final UserSignupRequestEntity registEntity) throws Exception {
        registEntity.setPassword(passwordEncoder.encode(registEntity.getPassword()));
        registEntity.setStatus("PENDING");
    }

    /**
     * 사용자 관리 > 사용자 신규계정 신청
     * 계정 기본정보만 입력, 세부정보는 가입 승인 후 수정
     * (등록 과정과 거의 동일하지만 일단 프로세스 분리)
     *
     * @param registDto 등록할 객체
     * @return {@link UserReqstDto} -- 성공 결과 객체
     */
    @Transactional
    public ServiceResponse regist(final UserReqstDto registDto) throws Exception {
        this.preRegist(registDto);

        final String username = registDto.getUsername();
        final String email = registDto.getEmail();
        if (userRepository.findByUsername(username).isPresent()) {
            return ServiceResponse.builder().rslt(false).message("이미 사용 중인 아이디입니다.").build();
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ServiceResponse.builder().rslt(false).message("이미 사용 중인 이메일입니다.").build();
        }
        if (userSignupRequestRepository.existsByUsernameAndStatus(username, "PENDING")) {
            return ServiceResponse.builder().rslt(false).message("이미 대기 중인 신청이 존재합니다.").build();
        }
        if (userSignupRequestRepository.existsByEmailAndStatus(email, "PENDING")) {
            return ServiceResponse.builder().rslt(false).message("해당 이메일로 대기 중인 신청이 존재합니다.").build();
        }

        final UserSignupRequestEntity registEntity = UserSignupRequestEntity.builder()
                .username(username)
                .password(registDto.getPassword())
                .nickname(registDto.getNickname())
                .email(email)
                .phoneNumber(registDto.getPhoneNumber())
                .content(registDto.getContent())
                .build();
        this.preRegist(registEntity);
        final UserSignupRequestEntity updatedEntity = userSignupRequestRepository.save(registEntity);

        final UserReqstDto rsltDto = UserReqstDto.builder()
                .id(updatedEntity.getId())
                .username(updatedEntity.getUsername())
                .nickname(updatedEntity.getNickname())
                .email(updatedEntity.getEmail())
                .phoneNumber(updatedEntity.getPhoneNumber())
                .content(updatedEntity.getContent())
                .createdBy(updatedEntity.getCreatedBy())
                .build();

        return ServiceResponse.builder()
                .rslt(updatedEntity.getId() != null)
                .rsltObj(rsltDto)
                .build();
    }

    /**
     * 사용자 정보 승인 처리.
     *
     * @param key 신청 번호
     * @return {@link Boolean} 처리 성공 여부
     */
    @Transactional
    public ServiceResponse cf(final Integer key) throws Exception {
        final UserSignupRequestEntity req =
                userSignupRequestRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("exception.EntityNotFoundException"));
        if (!"PENDING".equals(req.getStatus())) {
            return ServiceResponse.builder().rslt(false).build();
        }

        final UserEntity userEntity = UserEntity.builder()
                .username(req.getUsername())
                .password(req.getPassword())
                .nickname(req.getNickname())
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .content(req.getContent())
                .authList(List.of(new UserAuthRoleEntity(Code.AUTH_USER)))
                .acntStus(UserStateEntity.getRegistStus())
                .build();
        userEntity.cascade();
        final UserEntity updatedEntity = userRepository.saveAndFlush(userEntity);

        req.setStatus("APPROVED");
        req.setApprovedAt(DateUtils.getCurrDate());
        userSignupRequestRepository.save(req);

        return ServiceResponse.builder()
                .rslt(updatedEntity.getId() != null)
                .build();
    }

    /**
     * 사용자 정보 승인 취소 처리.
     *
     * @param key 신청 번호
     * @return {@link Boolean} 처리 성공 여부
     */
     @Transactional
    public ServiceResponse uncf(final Integer key) throws Exception {
        final UserSignupRequestEntity req =
                userSignupRequestRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("exception.EntityNotFoundException"));
        req.setStatus("REJECTED");
        req.setRejectedAt(DateUtils.getCurrDate());
        final UserSignupRequestEntity updatedEntity = userSignupRequestRepository.saveAndFlush(req);

        return ServiceResponse.builder()
                .rslt(updatedEntity.getId() != null)
                .build();
    }

    /**
     * 인증 메일에서 전달된 username 기준 신청 승인 처리.
     */
    @Transactional
    public ServiceResponse cfByUsername(final String username) throws Exception {
        final Optional<UserSignupRequestEntity> reqWrapper =
                userSignupRequestRepository.findTopByUsernameAndStatusOrderByCreatedAtDesc(username, "PENDING");
        if (reqWrapper.isEmpty()) throw new EntityNotFoundException("exception.EntityNotFoundException");
        return this.cf(reqWrapper.get().getId());
    }
}

