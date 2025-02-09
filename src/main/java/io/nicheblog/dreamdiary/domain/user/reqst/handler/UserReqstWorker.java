package io.nicheblog.dreamdiary.domain.user.reqst.handler;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.service.VerificationCodeService;
import io.nicheblog.dreamdiary.domain.user.reqst.event.UserReqstEvent;
import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDto;
import io.nicheblog.dreamdiary.extension.notify.email.event.UserReqstVerificationEmailSendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * UserReqstWorker
 * <pre>
 *  사용자 등록 흐처리 Worker :: Runnable 구현 (Queue 처리)
 *  Queue에서 UserReqstEvent를 가져와 후속 처리한다.
 * </pre>
 *
 * @author nichefish
 * @see UserReqstEventListener
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class UserReqstWorker
        implements Runnable {

    private final VerificationCodeService verificationCodeService;
    private final ApplicationEventPublisher publisher;

    /** 태그 queue */
    private static final BlockingQueue<UserReqstEvent> uesrReqstQueue = new LinkedBlockingQueue<>();
    private final JwtTokenProvider jwtTokenProvider;

    @PostConstruct
    public void init() {
        final Thread workerThread = new Thread(this);
        workerThread.start();
    }

    /**
     * 태그 Queue에서 TagProcEvent를 가져와 처리한다.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // Blocks until an element is available
                final UserReqstEvent event = uesrReqstQueue.take();
                final UserReqstDto userReqst = event.getUserReqst();

                final String email = userReqst.getEmail();
                if (StringUtils.isEmpty(email)) {
                    log.warn("User email is missing for event: {}", event);
                    continue;
                }

                // 랜덤 보안 코드 생성 (예: UUID 기반)
                final String userId = userReqst.getUserId();
                final String jwt = jwtTokenProvider.createToken(userId, userReqst.getAuthStrList());
                log.info("Generated security code for {}: {}", email, jwt);

                // 보안 코드 저장 (DB 또는 캐시 사용 가능) - 예제에서는 Redis 사용 가능
                verificationCodeService.setVerificationCode(email, jwt);

                // 이메일 발송
                publisher.publishEvent(new UserReqstVerificationEmailSendEvent(this, userReqst, jwt));
            }
        } catch (final InterruptedException e) {
            log.warn("user request handling failed", e);
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            log.warn("user request handling failed", e);
        }
    }

    /**
     * 사용자 신청 이벤트를 큐에 추가합니다.
     *
     * @param event 큐에 추가할 TagActvtyEvent / TagAnonActvtyEvent 객체
     */
    public void offer(final UserReqstEvent event) {
        boolean isOffered = uesrReqstQueue.offer(event);
        if (!isOffered) log.warn("queue offer failed... {}", event.toString());
    }
}
