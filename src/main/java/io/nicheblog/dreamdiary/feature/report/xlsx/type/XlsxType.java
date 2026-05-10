package io.nicheblog.dreamdiary.feature.report.xlsx.type;

import io.nicheblog.dreamdiary.feature.report.xlsx.XlsxHeader;
import io.nicheblog.dreamdiary.feature.report.xlsx.model.XlsxCell;
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

    BOARD(
            "게시판",
            "게시판 #1",
            "게시판",
            XlsxHeader.BOARD
    );

    public final String fileNm;
    public final String sheetNm;
    public final String title;
    public final List<XlsxCell> headers;
}
