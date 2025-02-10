package io.nicheblog.dreamdiary.extension.report.xlsx.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * XlsxStyledCell
 * <pre>
 *  (공통) 엑셀 셀(+스타일) 정보.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
public class XlsxStyledCell
        extends XlsxCell {

    /** 셀 스타일 */
    private CellStyle style;

    /* ----- */

    /**
     * 생성자.
     *
     * @param header 셀의 헤더 문자열
     * @param length 셀의 길이 (폭)
     */
    public XlsxStyledCell(final String header, final Integer length) {
        super(header, length);
    }
}
