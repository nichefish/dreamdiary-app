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
     * 분류 코드 + 상세 code 값으로 표시명(code_name) 조회
     *
     * @param groupCode 분류 코드 (String)
     * @param code 상세 코드 값 (String)
     * @return {@link String} -- 상세 코드명
     */
    public static String getCodeName(final String groupCode, final String code) {
        return codeLookupService.getCodeName(groupCode, code);
    }

}
