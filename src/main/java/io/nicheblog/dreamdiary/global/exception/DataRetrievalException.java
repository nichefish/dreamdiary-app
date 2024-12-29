package io.nicheblog.dreamdiary.global.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * DataRetrievalException
 * <pre>
 *  데이터 조회 실패시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
public class DataRetrievalException
        extends BaseException {

    /** PK값 */
    private Object key;

    /**
     * 생성자.
     *
     * @param msg
     * @param cause
     */
    public DataRetrievalException(final String msg, final Throwable cause, final Object key) {
        super(msg, cause);
        this.key = key;
    }

    /**
     * 생성자.
     *
     * @param msg
     * @param cause
     */
    public DataRetrievalException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * 생성자.
     *
     * @param msg
     */
    public DataRetrievalException(final String msg) {
        super(msg);
    }
}
