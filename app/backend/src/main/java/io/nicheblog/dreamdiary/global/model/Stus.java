package io.nicheblog.dreamdiary.global.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CmmStus
 * <pre>
 *  템플릿엔진 표출시 코드에 따라 바뀌는 정보 위임용 정의
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
public class Stus {

    /** 상태명 */
    private String nm;

    /** 상태 클래스명(색상) */
    private String classNm;

    /** 상태 아이콘(클래스) */
    private String icon;

    /* ----- */

    /**
     * 생성자.
     * 
     * @param nm 상태 이름 (String)
     */
    public Stus(final String nm) {
        this.nm = nm;
    }

    /**
     * 생성자.
     *
     * @param nm 상태 이름 (String)
     * @param classNm 상태와 관련된 CSS 클래스 이름 (String)
     */
    public Stus(final String nm, final String classNm) {
        this(nm);
        this.classNm = classNm;
    }

    /**
     * 생성자.
     *
     * @param nm 상태 이름 (String)
     * @param classNm 상태와 관련된 CSS 클래스 이름 (String)
     * @param icon 상태와 관련된 아이콘
     */
    public Stus(final String nm, final String classNm, final String icon) {
        this(nm, classNm);
        this.icon = icon;
    }
}
