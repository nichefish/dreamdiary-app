package io.nicheblog.dreamdiary.extension.file;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * ImageMimeType
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
public enum ImageMimeType {
    
    // png|jpg|jpeg|gif|avif|heic|heif|tiff|webp|psd|dng|cr2|crw|raf|erf|mrw|orf|nef|raw|rw2|rwl|pef|pbm|pgm|ppm|x3f|arw|sr2

    PNG("png", "image/png"),
    JPG("jpg", "image/jpg"),
    JPEG("jpeg", "image/jpeg"),
    GIF("gif", "image/gif"),
    AVIF("avif", "image/avif"),
    HEIC("heic", "image/heic"),
    HEIF("heif", "image/heic"),
    TIFF("tiff", "image/tiff"),
    WEBP("webp", "image/webp"),
    PSD("psd", "image/psd"),
    DNG("dng", "image/x-adobe-dng"),
    CR2("cr2", "image/x-canon-cr2"),
    CRW("crw", "image/x-canon-crw"),
    RAF("raf", "image/x-fuji-raf"),
    ERF("erf", "image/x-epson-erf"),
    MRW("mrw", "image/x-minolta-mrw"),
    ORF("orf", "image/x-olympus-orf"),
    NEF("nef", "image/x-nikon-nef"),
    RAW("raw", "image/x-panasonic-raw"),
    RW2("rw2", "image/x-panasonic-raw"),
    RWL("rwl", "image/x-panasonic-raw"),
    PEF("pef", "image/x-pentax-pef"),
    PBM("pbm", "image/x-portable-bitmap"),
    PGM("pgm", "image/x-portable-graymap"),
    PPM("ppm", "image/x-portable-pixmap"),
    X3F("x3f", "image/x-sigma-x3f"),
    ARW("arw", "image/x-sony-arw"),
    SR2("sr2", "image/x-sony-sr2");

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
