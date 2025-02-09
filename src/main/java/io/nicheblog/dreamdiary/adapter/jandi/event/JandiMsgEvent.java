package io.nicheblog.dreamdiary.adapter.jandi.event;

import io.nicheblog.dreamdiary.adapter.jandi.model.JandiParam;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * JandiMsgEvent
 * <pre>
 *  잔디 메세지 발송 이벤트.
 * </pre>
 *
 * @author nichefish
 */
@Getter
public class JandiMsgEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 잔디 발송 파라미터 */
    private final JandiParam jandiMsg;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param jandiMsg 발송할 잔디 메세지를 담은 객체
     */
    public JandiMsgEvent(final Object source, final JandiParam jandiMsg) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.jandiMsg = jandiMsg;
    }
}
