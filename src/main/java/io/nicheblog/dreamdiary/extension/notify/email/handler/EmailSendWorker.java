package io.nicheblog.dreamdiary.extension.notify.email.handler;

import io.nicheblog.dreamdiary.extension.notify.email.event.EmailSendEvent;
import io.nicheblog.dreamdiary.extension.notify.email.model.EmailSendParam;
import io.nicheblog.dreamdiary.extension.notify.email.service.EmailService;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.sys.event.LogSysEvent;
import io.nicheblog.dreamdiary.global._common.log.sys.handler.LogSysEventListener;
import io.nicheblog.dreamdiary.global._common.log.sys.model.LogSysParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * MailSendWorker
 * <pre>
 *  메일 발송 처리 Worker :: Runnable 구현 (Queue 처리)
 *  메일 큐에서 MailSendEvent를 가져와 메일을 발송합니다.
 * </pre>
 *
 * @author nichefish
 **/
@Component
@RequiredArgsConstructor
@Log4j2
public class EmailSendWorker implements Runnable {

	private final EmailService emailService;
	private final ApplicationEventPublisher publisher;

	/** 메일 queue */
	private static final BlockingQueue<EmailSendEvent> emailSendQueue = new LinkedBlockingQueue<>();

	@PostConstruct
	public void init() {
		final Thread workerThread = new Thread(this);
		workerThread.start();
	}

	/**
 	 * 메일 큐에서 MailSendEvent를 가져와 메일을 발송합니다.
	 *
	 * @see LogSysEventListener
	 */
	@Override
	public void run() {
		while (true) {
			// Blocks until an element is available
			try {
				// 메일 발송 처리
				final EmailSendEvent mailEvent = emailSendQueue.take();
				final EmailSendParam mailSendParam = mailEvent.getEmailSendParam();
				SecurityContextHolder.setContext(mailEvent.getSecurityContext());

				emailService.send(mailSendParam);
			} catch (final Exception e) {
				log.warn("mail send failed", e);
				Thread.currentThread().interrupt();
				final LogSysParam logParam = new LogSysParam(true, "메일 발송에 실패했습니다.", ActvtyCtgr.SYSTEM);
				logParam.setExceptionInfo(e);
				publisher.publishEvent(new LogSysEvent(this, logParam));
			}
		}
	}

	/**
	 * 메일 발송 이벤트를 큐에 추가합니다.
	 *
	 * @param event 큐에 추가할 MailSendEvent 객체
	 */
	public void offer(final EmailSendEvent event) {
		boolean isOffered = emailSendQueue.offer(event);
		if (!isOffered) log.warn("queue offer failed... {}", event.toString());
	}
}
