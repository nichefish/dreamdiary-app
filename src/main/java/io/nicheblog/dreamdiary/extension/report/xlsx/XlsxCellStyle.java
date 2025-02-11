package io.nicheblog.dreamdiary.extension.report.xlsx;

import io.nicheblog.dreamdiary.extension.report.xlsx.util.XlsxStyleUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.EnumMap;
import java.util.Map;

/**
 * XlsxCellStyle
 * <pre>
 *  사전 정의된 엑셀 셀 스타일 Enum.
 * </pre>
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum XlsxCellStyle {

    // 기본
    DEFAULT,
    TITLE,
    HEADER,
    DATA,
    // 스타일
    BORDER,
    LABEL,
    RED_LABEL,
    COVER;

    public static Map<XlsxCellStyle, CellStyle> createStyleMap(SXSSFWorkbook workbook) {
        return new EnumMap<>(XlsxCellStyle.class) {{
            put(DEFAULT, XlsxStyleUtils.getDefaultStyle(workbook));
            put(TITLE, XlsxStyleUtils.getTitleStyle(workbook));
            put(HEADER, XlsxStyleUtils.getHeaderStyle(workbook));
            put(DATA, XlsxStyleUtils.getDataStyle(workbook));
            //
            put(BORDER, XlsxStyleUtils.getBorderStyle(workbook));
            put(LABEL, XlsxStyleUtils.getLabelStyle(workbook));
            put(RED_LABEL, XlsxStyleUtils.getRedLabelStyle(workbook));
            put(COVER, XlsxStyleUtils.getCoverStyle(workbook));
        }};
    }
}
