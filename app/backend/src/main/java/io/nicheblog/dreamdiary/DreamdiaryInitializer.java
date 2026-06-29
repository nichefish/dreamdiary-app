package io.nicheblog.dreamdiary;

import io.nicheblog.dreamdiary.auth.config.AuthProperties;
import io.nicheblog.dreamdiary.auth.security.entity.RoleEntity;
import io.nicheblog.dreamdiary.auth.security.service.AuthService;
import io.nicheblog.dreamdiary.feature.admin.auth.policy.model.AuthPolicyDto;
import io.nicheblog.dreamdiary.feature.admin.auth.policy.service.AuthPolicyService;
import io.nicheblog.dreamdiary.feature.file.utils.FileUtils;
import io.nicheblog.dreamdiary.feature.journal.config.JournalProperties;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingSyncJobService;
import io.nicheblog.dreamdiary.feature.user.account.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.account.model.UserRoleDto;
import io.nicheblog.dreamdiary.feature.user.account.service.UserService;
import io.nicheblog.dreamdiary.global.ActiveProfile;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.infrastructure.release.service.ReleaseHistoryService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.event.CacheWarmupEvent;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogEvent;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

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
    private final ReleaseHistoryService releaseHistoryService;
    private final JournalEntryEmbeddingSyncJobService journalEntryEmbeddingSyncJobService;
    private final JournalProperties journalProperties;
    private final AuthProperties authProperties;

    /**
     * 프로그램 최초 구동시 수행할 로직.
     *
     * @param args 명령줄에서 전달된 인수 목록
     */
    @Override
    public void run(final String... args) throws Exception {

        log.info("Application initialization started. profile={}", activeProfile.getActive());

        try {
            releaseHistoryService.recordServerStart();
            releaseHistoryService.recordDeployIfChanged();
        } catch (final Exception e) {
            // 변경 전: release history 적재 로직이 없어 서버 시작/배포 히스토리 영속화 불가
            // 변경 후: 시작 시도 시 기록하되 실패해도 초기화 로직은 계속 진행
            log.error("Release history recording failed. startup continues.", e);
        }

        this.regSystemAcntIfEmpty();
        // 인증 정책 부재시 등록 :: 메소드 분리
        this.regAuthPolicyIfEmpty();

        // 파일 관련 기본 폴더 생성
        FileUtils.ensureDirectory("files/");
        FileUtils.ensureDirectory("files/upfiles/");
        FileUtils.ensureDirectory("files/reports/");

        // 캐시 웜업:: 초기 로딩 속도를 희생하여 미리 캐싱 처리함으로써 실행속도 상승
        publisher.publishAsyncEvent(new CacheWarmupEvent(this));
        log.info("Startup task queued. task=cacheWarmup");

        queueEmbeddingSyncOnStartup();

        // 시스템 재기동 로그 적재:: 운영 환경 이외에는 적재하지 않음
        if (activeProfile.isProd()) {
            final LogParam logParam = LogParam.forSystem(true, MessageUtils.getMessage("common.result.system-restarted"), ActvtyCtgr.SYSTEM);
            publisher.publishAsyncEvent(new LogEvent(this, logParam));
        }
        log.info("Application initialization completed. profile={}", activeProfile.getActive());
    }

    /**
     * 최초 실행시 사용자가 공백이므로 관리자 계정 자동 등록. (PW 암호화)
     */
    public void regSystemAcntIfEmpty() {

        final LogParam logParam = LogParam.forSystem();

        boolean isSuccess = false;
        boolean systemAcntExists = false;
        String rsltMsg = "";
        try {
            try {
                // 시스템계정 존재여부 체크
                authService.loadUserByUsername(Constant.SYSTEM_ACNT);
                systemAcntExists = true;
                log.info("Startup check completed. resource=systemAccount status=exists");
            } catch (final UsernameNotFoundException e) {
                // 시스템 계정 부재시 등록:: 메소드 분리
                isSuccess = this.regSystemAcnt();
                rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
                log.info("Startup action completed. resource=systemAccount status={} detail={}", isSuccess ? "created" : "failed", rsltMsg);
            }
        } catch (final Exception e) {
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
            log.error("Startup action failed. resource=systemAccount detail={}", rsltMsg, e);
        } finally {
            // 시스템 계정 등록 처리했을 경우 로그 적재
            if (!systemAcntExists) {
                logParam.setResult(isSuccess, rsltMsg);
                publisher.publishAsyncEvent(new LogEvent(this, logParam));
            }
        }
    }

    /**
     * 시스템 계정 등록.
     * 임의의 고정 패스워드로 생성되었으므로 최초설치 후 직접 비밀번호를 변경해 주어야 한다.
     */
    public boolean regSystemAcnt() throws Exception {

        final RoleEntity mngrRole = authService.getRole(Code.AUTH_MNGR);

        final UserRoleDto userRole = UserRoleDto.builder()
                .roleKey(Code.AUTH_MNGR)
                .role(mngrRole)
                .build();

        final UserDto systemAcnt = UserDto.builder()
                .nickname(Constant.SYSTEM_ACNT_NM)
                .username(Constant.SYSTEM_ACNT)
                .password(authProperties.getInitialAdminPassword())
                .userRoles(List.of(userRole))
                .createdBy(Constant.SYSTEM_ACNT)
                .build();

        final ServiceResponse result = userService.regist(systemAcnt);
        return result.getRslt();
    }

    /**
     * 최초 실행시 인증 정책이 공백이므로 기본값 자동 등록. (PW 암호화)
     *
     * @see io.nicheblog.dreamdiary.infrastructure.log.handler.LogEventListener
     */
    public void regAuthPolicyIfEmpty() {

        final LogParam logParam = LogParam.forSystem();

        boolean isSuccess = false;
        boolean authPolicyExists = false;
        String rsltMsg = "";
        try {
            // 인증 정책 존재여부 체크
            final AuthPolicyDto rsAuthPolicy = authPolicyService.getDtlDto();
            if (rsAuthPolicy != null) {
                authPolicyExists = true;
                log.info("Startup check completed. resource=authPolicy status=exists");
                return;
            }
            // 인증 정책 부재시 등록:: 메소드 분리
            isSuccess = this.regAuthPolicy();
            rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;
            log.info("Startup action completed. resource=authPolicy status={} detail={}", isSuccess ? "created" : "failed", rsltMsg);
        } catch (final Exception e) {
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
            log.error("Startup action failed. resource=authPolicy detail={}", rsltMsg, e);
        } finally {
            // 인증 정책 등록 처리했을 경우 로그 적재
            if (!authPolicyExists) {
                logParam.setResult(isSuccess, rsltMsg);
                publisher.publishAsyncEvent(new LogEvent(this, logParam));
            }
        }
    }

    /**
     * 인증 정책 등록.
     * 임의의 고정 패스워드로 생성되었으므로 최초설치 후 직접 비밀번호를 변경해 주어야 한다.
     */
    public boolean regAuthPolicy() throws Exception {

        final AuthPolicyDto authPolicy = AuthPolicyDto.builder()
                .loginAttemptLimit(5)
                .loginAttemptWindowMinutes(10)
                .accountLockDurationMinutes(30)
                .sessionTimeoutMinutes(60)
                .passwordChangeCycleDays(90)
                .passwordHistoryCount(2)
                .inactiveLockDays(90)
                .passwordResetTokenExpiryMinutes(30)
                .duplicateLoginAllowedYn("N")
                .build();

        return authPolicyService.regist(authPolicy).getRslt();
    }

    /**
     * 서버 기동 시 embedding queue sync job을 백그라운드로 enqueue한다.
     *
     * <p>Admin Sync Entries와 동일한 {@link JournalEntryEmbeddingSyncJobService#startSync()} 경로를 사용한다.
     * 이미 RUNNING이면 중복 시작하지 않는다.</p>
     */
    private void queueEmbeddingSyncOnStartup() {
        if (!journalProperties.getEmbedding().getSyncOnStartup()) {
            log.info("Startup task skipped. task=journalEntryEmbeddingSync reason=disabled");
            return;
        }

        try {
            journalEntryEmbeddingSyncJobService.startSync();
            log.info("Startup task queued. task=journalEntryEmbeddingSync");
        } catch (final Exception e) {
            log.warn("Startup task failed. task=journalEntryEmbeddingSync", e);
        }
    }
}
