package io.nicheblog.dreamdiary.global._common.file;

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
     * @param mimeType
     * @return ImageMimeType
     */
    public static ImageMimeType findExtn(String mimeType) {
        Optional<ImageMimeType> optional = Arrays.stream(ImageMimeType.values()).filter(x -> x.getMimeType().equals(mimeType)).findAny();
        return optional.orElse(null);
    }

    /**
     * 파일 확장자로부터 MimeType을 반환한다
     *
     * @param extn
     * @return ImageMimeType
     */
    public static ImageMimeType findMimeType(String extn) {
        Optional<ImageMimeType> optional = Arrays.stream(ImageMimeType.values()).filter(x -> x.getExtn().equals(extn)).findAny();
        return optional.orElse(null);
    }
}