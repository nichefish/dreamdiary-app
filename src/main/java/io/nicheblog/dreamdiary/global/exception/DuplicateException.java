package io.nicheblog.dreamdiary.global.exception;

import lombok.experimental.StandardException;

/**
 * DuplicateException
 * <pre>
 *  항목 중복(이중) 등록시 던지는 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class DuplicateException
        extends BaseException {
}
