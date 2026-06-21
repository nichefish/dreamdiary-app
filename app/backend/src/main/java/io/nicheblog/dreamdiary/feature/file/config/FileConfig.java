package io.nicheblog.dreamdiary.feature.file.config;

import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class FileConfig {

    private final FileProperties fileProperties;

    @Getter
    private Set<String> allowedExtensions;
    @Getter
    private Set<String> allowedMimeTypes;
    @Getter
    private Set<String> imageExtensions;

    /**
     * 빈 생성 시 한 번만 실행하여 문자열을 {@code Set<String>}으로 변환
     */
    @PostConstruct
    public void init() {
        allowedExtensions = CmmUtils.parseToSet(fileProperties.getAllowedExtensions(), "|");
        allowedMimeTypes = CmmUtils.parseToSet(fileProperties.getAllowedMimeTypes(), "|");
        imageExtensions = CmmUtils.parseToSet(fileProperties.getImageExtensions(), "|");
    }
}
