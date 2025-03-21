package io.nicheblog.dreamdiary.extension.file.config;

import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * FileConfig
 * <pre>
 *  파일 관련 설정.
 * </pre>
 *
 * @author nichefish
 */
@Configuration
public class FileConfig {

    @Value("${upload.allow.ext:jpg|jpeg|gif|png|bmp|webp|wav|mp3|mp4|txt|csv|tsv|json|xls|xlsx|doc|docx|ppt|pptx|hwp|pdf}")
    private String allowedExtensionsStr;
    @Value("${file.allowed-mime-types:image/jpeg|image/png|image/gif|image/bmp|image/webp|application/pdf|application/msword|application/vnd.ms-excel|application/vnd.openxmlformats-officedocument.wordprocessingml.document|application/vnd.openxmlformats-officedocument.spreadsheetml.sheet|application/vnd.openxmlformats-officedocument.presentationml.presentation|application/zip|application/x-rar-compressed|application/x-7z-compressed|text/plain|text/csv}")
    private String allowedMimeTypesStr;
    @Value("${file.image-extensions:jpg|jpeg|png|gif|bmp|webp|svg}")
    private String imageExtensionsStr;

    @Getter
    private Set<String> allowedExtensions;
    @Getter
    private Set<String> allowedMimeTypes;
    @Getter
    private Set<String> imageExtensions;

    /**
     * 빈 생성 시 한 번만 실행하여 문자열을 Set<String>으로 변환
     */
    @PostConstruct
    public void init() {
        allowedExtensions = CmmUtils.parseToSet(allowedExtensionsStr, "|");
        allowedMimeTypes = CmmUtils.parseToSet(allowedMimeTypesStr, "|");
        imageExtensions = CmmUtils.parseToSet(imageExtensionsStr, "|");
    }
}
