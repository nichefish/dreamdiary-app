package io.nicheblog.dreamdiary.global.exception;

/**
 * DupRegException
 * <pre>
 *  항목 중복(이중)등록시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
public class DupRegException
        extends BaseException {

    /**
     * 생성자.
     *
     * @param msg
     * @param cause
     */
    public DupRegException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * 생성자.
     *
     * @param msg
     */
    public DupRegException(final String msg) {
        super(msg);
    }
}
