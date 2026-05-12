package io.nicheblog.dreamdiary.feature.user.signup.handler;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.service.VerificationCodeService;
import io.nicheblog.dreamdiary.feature.user.signup.event.UserSignupEvent;
import io.nicheblog.dreamdiary.feature.user.signup.event.UserSignupVerificationEmailSendEvent;
import io.nicheblog.dreamdiary.feature.user.signup.model.UserSignupRequestDto;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * UserSignupWorker
 * <pre>
 *  계정 신청 후속 처리 Worker :: Runnable 구현 (Queue 처리)
 *  Queue에서 UserSignupEvent를 가져와 처리한다.
 * </pre>
 *
 * 명명 규약: 처리 스레드/워커는 {@code UserSignup*}; 이벤트 페이로드는 {@link UserSignupRequestDto}.
 *
 * @author nichefish
 * @see UserSignupEventListener
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class UserSignupWorker
        implements Runnable {

    private final VerificationCodeService verificationCodeService;
    private final ApplicationEventPublisherWrapper publisher;

    /** 계정 신청 이벤트 대기 큐 */
    private static final BlockingQueue<UserSignupEvent> userSignupEventQueue = new LinkedBlockingQueue<>();
    private final JwtTokenProvider jwtTokenProvider;

    @PostConstruct
    public void init() {
        final Thread workerThread = new Thread(this);
        workerThread.start();
    }

    /**
     * 계정 신청 큐에서 UserSignupEvent를 가져와 처리한다.
     */
    @Override
    public void run() {
        while (true) {
            try {
                // Blocks until an element is available
                final UserSignupEvent event = userSignupEventQueue.take();
                final UserSignupRequestDto userSignupRequest = event.getUserSignupRequest();

                final String email = userSignupRequest.getEmail();
                if (StringUtils.isEmpty(email)) {
                    log.warn("User email is missing for event: {}", event);
                    continue;
                }

                // 랜덤 보안 코드 생성 (예: UUID 기반)
                final String username = userSignupRequest.getUsername();
                final String jwt = jwtTokenProvider.createToken(username, userSignupRequest.getRoleKeyList());
                log.info("Generated security code for {}: {}", email, jwt);

                // 보안 코드 저장 (DB 또는 캐시 사용 가능) - 예제에서는 Redis 사용 가능
                verificationCodeService.setVerificationCode(email, jwt);

                // 이메일 발송
                publisher.publishAsyncEvent(new UserSignupVerificationEmailSendEvent(this, userSignupRequest, jwt));
            } catch (final InterruptedException e) {
                log.warn("user signup queue handling interrupted", e);
                Thread.currentThread().interrupt();
            } catch (final Exception e) {
                log.warn("user signup queue handling failed", e);
            }
        }
    }

    /**
     * 사용자 계정 신청 이벤트를 큐에 추가합니다.
     *
     * @param event 처리할 {@link UserSignupEvent}
     */
    public void offer(final UserSignupEvent event) {
        boolean isOffered = userSignupEventQueue.offer(event);
        if (!isOffered) log.warn("queue offer failed... {}", event.toString());
    }
}
