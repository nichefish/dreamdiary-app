package io.nicheblog.dreamdiary.extension.cd.utils;

import io.nicheblog.dreamdiary.extension.cd.service.DtlCdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * CdUtils
 */
@Component
@RequiredArgsConstructor
public class CdUtils {

    private final DtlCdService autowiredDtlCdService;
    private static DtlCdService dtlCdService;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        dtlCdService = autowiredDtlCdService;
    }

    /**
     * 분류 코드, 상세 코드로 상세 코드명 조회
     *
     * @param clCd 분류 코드 (String)
     * @param dtlCd 상세 코드 (String)
     * @return {@link String} -- 상세 코드명
     */
    public static String getDtlCdNm(final String clCd, final String dtlCd) {
        return dtlCdService.getDtlCdNm(clCd, dtlCd);
    }

}
