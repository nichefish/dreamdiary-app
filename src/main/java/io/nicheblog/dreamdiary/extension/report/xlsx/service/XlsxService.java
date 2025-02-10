package io.nicheblog.dreamdiary.extension.report.xlsx.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * XlsxService
 * <pre>
 *  엑셀 POI 라이브러리 관련 처리 서비스 모듈
 *  (단순 목록은 XlsxUtils 이용, 여기는 특별 양식 등 개별 로직 커스터마이징이 필요한 항목 정의)
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class XlsxService {

    /**
     * 상세 엑셀 파일 : 표지 페이지 생성 (메소드 분리) (개별 보고서 양식에 맞게 커스터마이징되어야 함)
     */
    // private void createRptCover(
    //         final SXSSFSheet sheet,
    //         final Map<String, Object> dataMap,
    //         final Map<String, CellStyle> styleMap
    // ) {
    //     // Header Row 생성
    //     Row headerRow = sheet.createRow(0);
    //     Cell headerCell;
    //     for (int i = 0; i < WIDTH_CELL_CNT; i++) {
    //         headerCell = headerRow.createCell(i);
    //         sheet.setColumnWidth(i, 8 * 256);
    //     }
//
    //     CellStyle LABEL = styleMap.get(XlsxStyle.LABEL);
    //     Row mngNoRow = sheet.createRow(1);
    //     XlsxUtils.createCellWithStyleInit(mngNoRow, 0, LABEL, "관리번호");
    //     XlsxUtils.createCellWithStyleInit(
    //             mngNoRow,
    //             1,
    //             LABEL,
    //             (String) Optional.ofNullable(dataMap.get("rptMngNo"))
    //                              .orElse(dataMap.get("title"))
    //     );
    //     XlsxUtils.createBlankCellRangeWithStyle(mngNoRow, 2, 3, LABEL);
    //     sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 3));
//
    //     CellStyle RED_LABEL = styleMap.get(XlsxStyle.RED_LABEL);
    //     Row secureRow = sheet.createRow(2);
    //     XlsxUtils.createCellWithStyleInit(secureRow, 0, RED_LABEL, "외부 유출 금지");
    //     XlsxUtils.createBlankCellRangeWithStyle(secureRow, 1, 3, RED_LABEL);
    //     sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 3));
    //     XlsxUtils.createCellWithStyleInit(secureRow, MAX_CELL - 1, RED_LABEL, "비공개");
    //     XlsxUtils.createBlankCellRangeWithStyle(secureRow, MAX_CELL, MAX_CELL, RED_LABEL);
    //     sheet.addMergedRegion(new CellRangeAddress(2, 2, MAX_CELL - 1, MAX_CELL));
//
    //     // 제목 생성
    //     CellStyle COVER_TITLE = styleMap.get(XlsxStyle.COVER_TITLE);
    //     Row titleBfRow = sheet.createRow(9);
    //     XlsxUtils.createCellWithStyleInit(titleBfRow, 1, COVER_TITLE, (String) dataMap.get("rptCoverTitle"));
    //     XlsxUtils.createBlankCellRangeWithStyle(titleBfRow, 2, MAX_CELL - 1, COVER_TITLE);
    //     Row titleRow = sheet.createRow(10);
    //     XlsxUtils.createBlankCellRangeWithStyle(titleRow, 1, MAX_CELL - 1, COVER_TITLE);
    //     Row titleNextRow = sheet.createRow(11);
    //     XlsxUtils.createBlankCellRangeWithStyle(titleNextRow, 1, MAX_CELL - 1, COVER_TITLE);
    //     sheet.addMergedRegion(new CellRangeAddress(9, 11, 1, MAX_CELL - 1));
//
    //     // 날짜 생성
    //     CellStyle COVER_DT = styleMap.get(XlsxStyle.COVER_DT);
    //     Row regDtRow = sheet.createRow(23);
    //     Cell regDtCell = XlsxUtils.createCellWithStyleInit(regDtRow, 3, COVER_DT, (String) dataMap.get("regDtDy"));
    //     sheet.addMergedRegion(new CellRangeAddress(23, 23, 3, MAX_CELL - 3));
//
    //     // 낙인 생성
    //     CellStyle COVER_SIGN = styleMap.get(XlsxStyle.COVER_SIGN);
    //     Row signRow = sheet.createRow(30);
    //     Cell signCell = XlsxUtils.createCellWithStyleInit(signRow, 2, COVER_SIGN, "정보통신보안담당관");
    //     sheet.addMergedRegion(new CellRangeAddress(30, 30, 2, MAX_CELL - 2));
//
    //     // 낙인 생성
    //     Row sign2Row = sheet.createRow(32);
    //     Cell sign2Cell = XlsxUtils.createCellWithStyleInit(sign2Row, 2, COVER_SIGN, "(서울사이버안전센터)");
    //     sheet.addMergedRegion(new CellRangeAddress(32, 32, 2, MAX_CELL - 2));
    // }

    /**
     * 상세 엑셀 파일 : 내용 페이지 생성 (메소드 분리) (개별 보고서 양식에 맞게 커스터마이징되어야 함)
     */
    // private int createRptContent(
    //         final SXSSFSheet sheet,
    //         final XlsxDefInfo xlxsInfo,
    //         final Map<String, Object> dataMap,
    //         final Map<String, CellStyle> styleMap
    // ) {
//
    //     // 42, 85, 128, 171 ...
    //     String[][] dtlHeaderList = xlxsInfo.getDtlHeader();
    //     Row dataRow;
    //     Cell headerCell;
    //     Cell valueCell;
    //     Cell dtCell;
    //     int rowIdx = 0;
    //     int nextCellIdx;
    //     int rowLength;
//
    //     int dtlHeaderLength = dtlHeaderList.length;
    //     CellStyle DEFAULT = styleMap.get(XlsxStyle.DEFAULT);
    //     CellStyle CN_TITLE = styleMap.get(XlsxStyle.CN_TITLE);
    //     CellStyle CN_HEADER = styleMap.get(XlsxStyle.CN_HEADER);
    //     CellStyle CN_HEADER_U = styleMap.get(XlsxStyle.CN_HEADER_U);
    //     CellStyle style;
    //     String key;
//
    //     for (int i = 0; i < dtlHeaderLength; i++) {
    //         rowIdx = 42 + i;
    //         dataRow = sheet.createRow(rowIdx);
    //         dataRow.setHeight((short) -1);
    //         String rowType = dtlHeaderList[i][0];
//
    //         // 공백 행은 빈 줄만 만들고 패스
    //         if (XlsxConstant.BLANK.equals(rowType)) continue;
//
    //         style = Optional.ofNullable(styleMap.get(rowType))
    //                         .orElse(styleMap.get(XlsxStyle.DEFAULT_BORDER));
//
    //         // 헤더
    //         key = dtlHeaderList[i][1];
    //         headerCell = XlsxUtils.createCellWithStyleInit(dataRow, 0, style, key);
    //         if (XlsxConstant.CN_DATA_PLAIN.equals(rowType)) {
    //             headerCell.setCellValue((String) dataMap.get(key));
    //             headerCell.setCellStyle(DEFAULT);
    //             continue;
    //         }
    //         if (XlsxConstant.CN_TITLE.equals(rowType)) continue;
    //         if (XlsxConstant.CN_DATA.equals(rowType)) {
    //             headerCell.setCellStyle(CN_HEADER);
    //             XlsxUtils.createBlankCellWithStyle(dataRow, 1, CN_HEADER);
    //         } else {
    //             XlsxUtils.createBlankCellWithStyle(dataRow, 1, style);
    //         }
    //         sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 1));
    //         nextCellIdx = 2;
//
    //         // 처리일시
    //         rowLength = dtlHeaderList[i].length;
    //         if (rowLength > 3) {
    //             dtCell = XlsxUtils.createBlankCellWithStyle(dataRow, nextCellIdx, style);
    //             key = dtlHeaderList[i][2];
    //             if (XlsxConstant.CN_HEADER_U.equals(rowType)) {
    //                 dtCell.setCellValue(key);
    //             } else {
    //                 dtCell.setCellValue((String) dataMap.get(key));
    //             }
    //             XlsxUtils.createBlankCellWithStyle(dataRow, nextCellIdx + 1, style);
    //             sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, nextCellIdx, nextCellIdx + 1));
    //             nextCellIdx = nextCellIdx + 2;
    //         }
//
    //         // 내용
    //         valueCell = XlsxUtils.createBlankCellWithStyle(dataRow, nextCellIdx, style);
    //         key = dtlHeaderList[i][rowLength - 1];
    //         if (XlsxConstant.CN_HEADER_U.equals(rowType)) {
    //             valueCell.setCellValue(key);
    //         } else {
    //             valueCell.setCellValue((String) dataMap.get(key));
    //         }
    //         XlsxUtils.createBlankCellRangeWithStyle(dataRow, nextCellIdx + 1, MAX_CELL, style);
    //         sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, nextCellIdx, MAX_CELL));
    //     }
//
    //     return rowIdx;
    // }
}
