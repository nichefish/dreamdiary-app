package io.nicheblog.dreamdiary.domain.jrnl.day.event;

import io.nicheblog.dreamdiary.extension.clsf.tag.handler.TagEventListener;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * JrnlDayTagEvent
 * <pre>
 *  태그 추가 이벤트 :: 메인 로직과 분리.
 * </pre>
 *
 * @author nichefish
 * @see TagEventListener
 */
@Getter
public class JrnlDayTagCntSubEvent
        extends ApplicationEvent {

    /** 보안 컨텍스트 */
    private final SecurityContext securityContext;
    /** 컨텐츠 복합키 */
    private final Integer yy;
    /** 컨텐츠 복합키 */
    private final Integer mnth;
    /** 태그 */
    private final List<Integer> tagNoList;

    /* ----- */

    /**
     * 생성자.
     *
     * @param source 이벤트의 출처를 나타내는 객체
     * @param yy 년도
     * @param mnth 월
     * @param tagNoList 태그 번호 목록
     */
    public JrnlDayTagCntSubEvent(final Object source, final Integer yy, final Integer mnth, final List<Integer> tagNoList) {
        super(source);
        this.securityContext = SecurityContextHolder.getContext();
        this.yy = yy;
        this.mnth = mnth;
        this.tagNoList = tagNoList;
    }
}
