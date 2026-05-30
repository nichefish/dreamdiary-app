package io.nicheblog.dreamdiary.global.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.StandardException;

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
@StandardException
public class DataRetrievalException
        extends BaseException {

    /** PK값 */
    private Object key;

    /**
     * 생성자.
     *
     * @param msg String
     * @param cause Throwable
     * @param key Object
     */
    public DataRetrievalException(final String msg, final Throwable cause, final Object key) {
        super(msg, cause);
        this.key = key;
    }
}
