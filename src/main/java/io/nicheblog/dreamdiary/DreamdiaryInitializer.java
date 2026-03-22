package io.nicheblog.dreamdiary;

import io.nicheblog.dreamdiary.auth.policy.model.AuthPolicyDto;
import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyService;
import io.nicheblog.dreamdiary.auth.security.entity.AuthRoleEntity;
import io.nicheblog.dreamdiary.auth.security.service.AuthService;
import io.nicheblog.dreamdiary.feature.user.info.model.UserAuthRoleDto;
import io.nicheblog.dreamdiary.feature.user.info.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.info.service.UserService;
import io.nicheblog.dreamdiary.global.ActiveProfile;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.event.CacheWarmupEvent;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.log.sys.event.LogSysEvent;
import io.nicheblog.dreamdiary.infrastructure.log.sys.handler.LogSysEventListener;
import io.nicheblog.dreamdiary.infrastructure.log.sys.model.LogSysParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * DreamdiaryInitializer
 * <pre>
 *  어플리케이션 초기화 로직 수행 클래스.
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class DreamdiaryInitializer
        implements CommandLineRunner {

    private final ActiveProfile activeProfile;
    private final AuthService authService;
    private final UserService userService;
    private final AuthPolicyService authPolicyService;
    private final ApplicationEventPublisherWrapper publisher;

    @Value("${system.init-temp-pw:}")
    public String SYSTEM_INIT_TEMP_PW;

    /**
     * 프로그램 최초 구동시 수행할 로직.
     *
     * @param args 명령줄에서 전달된 인수 목록
     */
    @Override
    public void run(final String... args) throws Exception {

        log.info("DreamdiaryApplication init... activeProfile: {}", activeProfile.getActive());

        this.regSystemAcntIfEmpty();
        // 인증 정책 부재시 등록 :: 메소드 분리
        this.regAuthPolicyIfEmpty();

        // 파일 관련 기본 폴더 생성
        final File fileDirectory = new File("file/");
        if (!fileDirectory.exists() && !fileDirectory.mkdirs()) throw new IOException(MessageUtils.getMessage("msg.rslt.mkdir-failed"));
        final File upfileDirectory = new File("file/upfile/");
        if (!upfileDirectory.exists() && !upfileDirectory.mkdirs()) throw new IOException(MessageUtils.getMessage("msg.rslt.mkdir-failed"));
        final File reportDirectory = new File("file/report/");
        if (!reportDirectory.exists() && !reportDirectory.mkdirs()) throw new IOException(MessageUtils.getMessage("msg.rslt.mkdir-failed"));

        // 캐시 웜업:: 초기 로딩 속도를 희생하여 미리 캐싱 처리함으로써 실행속도 상승
        publisher.publishAsyncEvent(new CacheWarmupEvent(this));

        // 시스템 재기동 로그 적재:: 운영 환경 이외에는 적재하지 않음
        if (activeProfile.isProd()) {
            final LogSysParam logParam = new LogSysParam(true, MessageUtils.getMessage("msg.rslt.system-restarted"), ActvtyCtgr.SYSTEM);
            publisher.publishAsyncEvent(new LogSysEvent(this, logParam));
        }
    }

    /**
     * 최초 실행시 사용자가 공백이므로 관리자 계정 자동 등록. (PW 암호화)
     */
    public void regSystemAcntIfEmpty() {

        final LogSysParam logParam = new LogSysParam();

        boolean isSuccess = false;
        boolean systemAcntExists = false;
        String rsltMsg = "";
        try {
            try {
                // 시스템계정 존재여부 체크
                authService.loadUserByUsername(Constant.SYSTEM_ACNT);
                systemAcntExists = true;
            } catch (final UsernameNotFoundException e) {
                // 시스템 계정 부재시 등록:: 메소드 분리
                isSuccess = this.regSystemAcnt();
                rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
            }
        } catch (final Exception e) {
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
        } finally {
            // 시스템 계정 등록 처리했을 경우 로그 적재
            if (!systemAcntExists) {
                logParam.setResult(isSuccess, rsltMsg);
                publisher.publishAsyncEvent(new LogSysEvent(this, logParam));
            }
        }
    }

    /**
     * 시스템 계정 등록.
     * 임의의 고정 패스워드로 생성되었으므로 최초설치 후 직접 비밀번호를 변경해 주어야 한다.
     */
    public boolean regSystemAcnt() throws Exception {

        final AuthRoleEntity authRoleEntityMngr = authService.getAuthRole(Code.AUTH_MNGR);

        final UserAuthRoleDto userAuthRole = UserAuthRoleDto.builder()
                .authCd(Code.AUTH_MNGR)
                .role(authRoleEntityMngr)
                .build();

        final UserDto systemAcnt = UserDto.builder()
                .nickNm(Constant.SYSTEM_ACNT_NM)
                .userId(Constant.SYSTEM_ACNT)
                .password(SYSTEM_INIT_TEMP_PW)
                .authList(List.of(userAuthRole))
                .regstrId(Constant.SYSTEM_ACNT)
                .build();

        final ServiceResponse result = userService.regist(systemAcnt);
        return result.getRslt();
    }

    /**
     * 최초 실행시 인증 정책이 공백이므로 기본값 자동 등록. (PW 암호화)
     *
     * @see LogSysEventListener
     */
    public void regAuthPolicyIfEmpty() {

        final LogSysParam logParam = new LogSysParam();

        boolean isSuccess = false;
        boolean authPolicyExists = false;
        String rsltMsg = "";
        try {
            // 인증 정책 존재여부 체크
            final AuthPolicyDto rsAuthPolicy = authPolicyService.getDtlDto();
            if (rsAuthPolicy != null) {
                authPolicyExists = true;
                return;
            }
            // 인증 정책 부재시 등록:: 메소드 분리
            isSuccess = this.regAuthPolicy();
            rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
        } catch (final Exception e) {
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
        } finally {
            // 인증 정책 등록 처리했을 경우 로그 적재
            if (!authPolicyExists) {
                logParam.setResult(isSuccess, rsltMsg);
                publisher.publishAsyncEvent(new LogSysEvent(this, logParam));
            }
        }
    }

    /**
     * 인증 정책 등록.
     * 임의의 고정 패스워드로 생성되었으므로 최초설치 후 직접 비밀번호를 변경해 주어야 한다.
     */
    public boolean regAuthPolicy() throws Exception {

        final AuthPolicyDto authPolicy = AuthPolicyDto.builder()
                .lgnTryLmt(5)
                .pwChgDy(90)
                .lgnLockDy(90)
                .pwForReset(SYSTEM_INIT_TEMP_PW)
                .build();

        return authPolicyService.regist(authPolicy).getRslt();
    }
}
