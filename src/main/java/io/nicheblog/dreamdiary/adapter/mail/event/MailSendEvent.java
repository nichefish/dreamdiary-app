package io.nicheblog.dreamdiary.adapter.mail.event;

import io.nicheblog.dreamdiary.adapter.mail.handler.MailEventListener;
import io.nicheblog.dreamdiary.adapter.mail.model.MailSendParam;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * MailSendEvent
 * <pre>
 *  메일 발송 이벤트.
 * </pre>
 *
 * @author nichefish
 * @see MailEventListener
 */
@Getter
@Setter
public class MailSendEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 메일 팔송 파라미터 */
    private final MailSendParam mailSendParam;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param mailSendParam 발송할 메일 정보를 담은 객체
     */
    public MailSendEvent(final Object source, final MailSendParam mailSendParam) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.mailSendParam = mailSendParam;
    }
}
