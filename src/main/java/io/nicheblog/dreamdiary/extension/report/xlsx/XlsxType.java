package io.nicheblog.dreamdiary.extension.report.xlsx;

import io.nicheblog.dreamdiary.extension.report.xlsx.model.XlsxCell;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * XlsxType
 * <pre>
 *  사전 정의된 엑셀 규격 정보 Enum
 * </pre>
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum XlsxType {

    NOTICE(
            "공지사항",
            "공지사항 #1",
            "공지사항",
            XlsxHeader.NOTICE
    );

    public final String fileNm;
    public final String sheetNm;
    public final String title;
    public final List<XlsxCell> headers;
}