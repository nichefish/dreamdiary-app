package io.nicheblog.dreamdiary.extension.report.xlsx.util;

import lombok.experimental.UtilityClass;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * XlsxTemplateLoader
 * <pre>
 *  엑셀 POI 라이브러리 관련 처리 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public class XlsxTemplateLoader {

    /**
     * 엑셀 XSSF 템플릿 조회
     *
     * @param templatePath 템플릿 파일 경로
     * @return XSSFWorkbook 객체 (엑셀 템플릿)
     * @throws IOException 파일을 읽는 도중 발생할 수 있는 예외
     */
    public static XSSFWorkbook loadTemplateAsXssf(final String templatePath) throws IOException {
        try (final FileInputStream fis = new FileInputStream(templatePath)) {
            return new XSSFWorkbook(fis);
        }
    }

    /**
     * 엑셀 SXSSF 템플릿 조회 (스트림 처리)
     *
     * @param templatePath 템플릿 파일 경로
     * @return SXSSFWorkbook 객체 (스트림 방식 엑셀 템플릿)
     * @throws IOException 파일을 읽는 도중 발생할 수 있는 예외
     */
    public static SXSSFWorkbook loadTemplateAsSxssf(final String templatePath) throws IOException {
        final XSSFWorkbook xssfWorkbook = loadTemplateAsXssf(templatePath);
        // XSSFWorkbook을 SXSSFWorkbook으로 변환
        return new SXSSFWorkbook(xssfWorkbook);
    }
}
