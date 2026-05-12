package io.nicheblog.dreamdiary.global.exception;

import lombok.experimental.StandardException;

/**
 * FailureException
 * <pre>
 *  실패시 분기에서 빼버리기 위해 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class FailureException
        extends BaseException {
}
