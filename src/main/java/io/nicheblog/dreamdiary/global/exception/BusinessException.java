package io.nicheblog.dreamdiary.global.exception;

/**
 * BusinessException
 * <pre>
 *  (공통/상속) 비즈니스 로직 Exception
 * </pre>
 *
 * @author nichefish
 */
public class BusinessException
        extends BaseException {

    /**
     * 생성자.
     *
     * @param msg
     * @param cause
     */
    public BusinessException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * 생성자.
     *
     * @param msg
     */
    public BusinessException(final String msg) {
        super(msg);
    }
}
