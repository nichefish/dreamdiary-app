package io.nicheblog.dreamdiary.extension.file;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * MimeType
 * <pre>
 *  파일 MIME_TYPE 정보.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
public enum MimeType {

    TEXT("txt", "text/plain"),
    PDF("pdf", "application/pdf"),
    DOWNLOAD("download", "application/download; charset=utf-8"),
    STREAM("stream", "application/octet-stream; charset=euc-kr");

    private final String extn;
    private final String mimeType;

    /**
     * MimeType 로부터 파일 확장자를 반환한다
     *
     * @param mimeType MimeType 문자열
     * @return ImageMimeType
     */
    public static MimeType findExtn(final String mimeType) {
        final Optional<MimeType> optional = Arrays.stream(MimeType.values()).filter(x -> x.getMimeType().equals(mimeType)).findAny();
        return optional.orElse(null);
    }

    /**
     * 파일 확장자로부터 MimeType을 반환한다
     *
     * @param extn 확장자
     * @return ImageMimeType
     */
    public static MimeType findMimeType(final String extn) {
        final Optional<MimeType> optional = Arrays.stream(MimeType.values()).filter(x -> x.getExtn().equals(extn)).findAny();
        return optional.orElse(null);
    }
}