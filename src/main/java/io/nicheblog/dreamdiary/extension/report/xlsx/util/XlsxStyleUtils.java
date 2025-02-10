package io.nicheblog.dreamdiary.extension.report.xlsx.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * XlsxStyleUtils
 * <pre>
 *  엑셀 셀 스타일 관련 처리 유틸리티 클래스
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
@Log4j2
public class XlsxStyleUtils {

    /**
     * 기본 셀 스타일을 생성하여 반환합니다.
     *
     * @param workbook 셀 스타일을 생성할 SXSSFWorkbook 객체
     * @return 생성된 기본 셀 스타일
     */
    public static CellStyle getDefaultStyle(final SXSSFWorkbook workbook) {
        return workbook.createCellStyle();
    }

    /**
     * 기본 스타일에 테두리를 추가한 제목 스타일을 생성하여 반환합니다.
     *
     * @param workbook 셀 스타일을 생성할 SXSSFWorkbook 객체
     * @return 생성된 제목 셀 스타일
     */
    public static CellStyle getTitleStyle(final SXSSFWorkbook workbook) {
        final CellStyle style = workbook.createCellStyle();
        // 폰트
        final Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        style.setFont(titleFont);
        style.setAlignment(HorizontalAlignment.CENTER);
        // 테두리
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        // 자동줄바꿈
        style.setWrapText(true);
        return style;
    }

    /**
     * 헤더 스타일(기본 스타일에 테두리 추가)을 생성하여 반환합니다.
     *
     * @param workbook 셀 스타일을 생성할 SXSSFWorkbook 객체
     * @return 생성된 헤더 셀 스타일
     */
    public static CellStyle getHeaderStyle(final SXSSFWorkbook workbook) {
        final CellStyle style = workbook.createCellStyle();
        // 폰트
        final Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        // 테두리
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        // 자동줄바꿈
        style.setWrapText(true);
        return style;
    }

    /**
     * 데이터 스타일(기본 스타일에 테두리를 추가하지 않음)을 생성하여 반환합니다.
     *
     * @param workbook 셀 스타일을 생성할 SXSSFWorkbook 객체
     * @return 생성된 데이터 셀 스타일
     */
    public static CellStyle getDataStyle(final SXSSFWorkbook workbook) {
        final CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        // 자동줄바꿈
        style.setWrapText(true);
        return style;
    }

    /* ----- */

    /**
     * 테두리 스타일을 생성하여 반환합니다.
     *
     * @param workbook 셀 스타일을 생성할 SXSSFWorkbook 객체
     * @return 생성된 테두리 셀 스타일
     */
    public static CellStyle getBorderStyle(final SXSSFWorkbook workbook) {
        final CellStyle style = workbook.createCellStyle();
        // 테두리
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        // 자동줄바꿈
        style.setWrapText(true);
        return style;
    }

    /**
     * 라벨lABEL(가운데 정렬 및 테두리를 가짐) 스타일일을 생성하여 반환합니다.
     *
     * @param workbook 셀 스타일을 생성할 SXSSFWorkbook 객체
     * @return 생성된 레이블 셀 스타일
     */
    public static CellStyle getLabelStyle(final SXSSFWorkbook workbook) {
        final CellStyle style = workbook.createCellStyle();
        // 가운데 정렬
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        // 테두리
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        // 배경색
        style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 뻘건 라벨LABEL(가운데 정렬 및 테두리를 갖고, 빨간 글씨 추가) 스타일을 생성하여 반환합니다.
     *
     * @param workbook 셀 스타일을 생성할 SXSSFWorkbook 객체
     * @return 생성된 빨간 레이블 셀 스타일
     */
    public static CellStyle getRedLabelStyle(final SXSSFWorkbook workbook) {
        final CellStyle style = getLabelStyle(workbook);
        // 빨간글씨
        final Font font = workbook.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        style.setFont(font);
        return style;
    }

    /**
     * 커버 타이틀COVER_TITLE(가운데 정렬 및 테두리를 갖고, 크고 굵은 글씨 추가) 스타일을 생성하여 반환합니다.
     *
     * @param workbook 셀 스타일을 생성할 SXSSFWorkbook 객체
     * @return 생성된 커버 셀 스타일
     */
    public static CellStyle getCoverStyle(final SXSSFWorkbook workbook) {
        final CellStyle style = getLabelStyle(workbook);
        // 크고굵은글씨
        final Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 24);
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
