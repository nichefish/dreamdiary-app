package io.nicheblog.dreamdiary.global._common.file.exception;

import io.nicheblog.dreamdiary.global.exception.BaseException;

/**
 * AtchFileUploadException
 * <pre>
 *  파일 업로드 실패 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
public class AtchFileUploadException
        extends BaseException {

    /**
     * 생성자.
     *
     * @param msg
     * @param cause
     */
    public AtchFileUploadException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * 생성자.
     *
     * @param msg
     */
    public AtchFileUploadException(final String msg) {
        super(msg);
    }
}
