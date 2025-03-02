package io.nicheblog.dreamdiary.extension.clsf.tag.event;

import io.nicheblog.dreamdiary.extension.clsf.tag.handler.JrnlTagCacheUpdtEventListener;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * JrnlTagCntCacheUpdtEvent
 * <pre>
 *  태그 갯수 캐시 이벤트 :: 메인 로직과 분리.
 * </pre>
 *
 * @author nichefish
 * @see JrnlTagCacheUpdtEventListener
 */
@Getter
public class JrnlTagCacheUpdtEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 컨텐츠 복합키 */
    private final BaseClsfKey clsfKey;
    /** 컨텐츠 복합키 */
    private final Integer yy;
    /** 컨텐츠 복합키 */
    private final Integer mnth;
    /** tagNo별 갯수 캐시 변화량 (+1, -1 등) */
    private final Map<Integer, Integer> tagCntChangeMap;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param yy 년도
     * @param mnth 월
     * @param tagCntChangeMap tagNo별 갯수 캐시 변화량 (+1, -1 등)
     */
    public JrnlTagCacheUpdtEvent(final Object source, final BaseClsfKey clsfKey, final Integer yy, final Integer mnth, final Map<Integer, Integer> tagCntChangeMap) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.clsfKey = clsfKey;
        this.yy = yy;
        this.mnth = mnth;
        this.tagCntChangeMap = tagCntChangeMap;
    }
}
