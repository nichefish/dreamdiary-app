package io.nicheblog.dreamdiary.global.exception;

/**
 * FailureException
 * <pre>
 *  실패시 분기에서 빼버리기 위해 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
public class FailureException
        extends BaseException {

    /**
     * 생성자.
     *
     * @param msg
     * @param cause
     */
    public FailureException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * 생성자.
     *
     * @param msg
     */
    public FailureException(final String msg) {
        super(msg);
    }
}
