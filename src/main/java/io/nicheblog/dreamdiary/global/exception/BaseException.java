package io.nicheblog.dreamdiary.global.exception;

import java.util.HashMap;
import java.util.List;

/**
 * BaseException
 * <pre>
 *  (공통/상속) 오류에 로깅 필요 값을 담아서 던질 수 있도록 만든 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
public class BaseException
        extends RuntimeException {

    /** 결과 (목록) */
    private List<?> rsltList;

    /** 결과 (맵) */
    private HashMap<String, Object> rsltMap;

    /** 결과 (숫자) */
    private Integer rsltVal;

    /** 결과 (객체) */
    private Object rsltObj;

    /** 결과 (문자열) */
    private String rsltStr;

    /**
     * 생성자.
     *
     * @param msg
     * @param cause
     */
    public BaseException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * 생성자.
     *
     * @param msg
     */
    public BaseException(final String msg) {
        super(msg);
    }
}
