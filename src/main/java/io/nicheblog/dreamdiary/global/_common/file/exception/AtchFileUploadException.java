package io.nicheblog.dreamdiary.global._common.file.exception;

/**
 * AtchFileUploadException
 * <pre>
 *  파일 업로드 실패 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
public class AtchFileUploadException
        extends RuntimeException {

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
