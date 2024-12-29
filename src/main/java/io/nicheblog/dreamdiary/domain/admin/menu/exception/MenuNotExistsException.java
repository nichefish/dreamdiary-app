package io.nicheblog.dreamdiary.domain.admin.menu.exception;

import io.nicheblog.dreamdiary.global.exception.BaseException;

import java.util.HashMap;
import java.util.List;

/**
 * MenuNotExistsException
 * <pre>
 *  (공통/상속) 오류에 로깅 필요 값을 담아서 던질 수 있도록 만든 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
public class MenuNotExistsException
        extends BaseException {

    /**
     * 생성자.
     *
     * @param msg
     * @param cause
     */
    public MenuNotExistsException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * 생성자.
     *
     * @param msg
     */
    public MenuNotExistsException(final String msg) {
        super(msg);
    }
}
