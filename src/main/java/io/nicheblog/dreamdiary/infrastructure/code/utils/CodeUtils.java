package io.nicheblog.dreamdiary.infrastructure.code.utils;

import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * CodeUtils
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class CodeUtils {

    private final CodeLookupService autowiredCodeLookupService;
    private static CodeLookupService codeLookupService;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        codeLookupService = autowiredCodeLookupService;
    }

    /**
     * 분류 코드, 상세 코드로 상세 코드명 조회
     *
     * @param clCd 분류 코드 (String)
     * @param dtlCd 상세 코드 (String)
     * @return {@link String} -- 상세 코드명
     */
    public static String getDtlCdNm(final String clCd, final String dtlCd) {
        return codeLookupService.getDtlCdNm(clCd, dtlCd);
    }

}
