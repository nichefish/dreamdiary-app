package io.nicheblog.dreamdiary.extension.report.xlsx.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.extension.file.utils.FileUtils;
import io.nicheblog.dreamdiary.extension.report.xlsx.XlsxCellStyle;
import io.nicheblog.dreamdiary.extension.report.xlsx.XlsxType;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * XlsxUtils
 * <pre>
 *  엑셀 POI 라이브러리 관련 처리 유틸리티 모듈
 *  (SXSSF 사용)
 *  (리플렉션 사용하여 일반화한 공통 로직, 가변적인 소규모 데이터에 적합, 대량(수십만건 이상) 처리에는 부적합.)
 *  (대용량 처리시 xlsxService에 특화된 로직 구현하기)
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class XlsxUtils {

    private final HttpServletResponse autowiredResponse;

    private static HttpServletResponse response;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        response = autowiredResponse;
    }

    /**
     * 목록 엑셀 파일 다운로드
     *
     * @param xlsxType 엑셀 파일 타입 정보
     * @param dataStream 데이터 스트림
     * @throws Exception 처리 과정에서 발생할 수 있는 예외
     */
    public static void listXlxsDownload(final XlsxType xlsxType, final Stream<?> dataStream) throws Exception {

        // 엑셀 파일 생성 (메소드 분리)
        final SXSSFWorkbook workbook = makeListXlsxFile(xlsxType, dataStream);

        // 생성된 엑셀 파일 다운로드 (메소드 분리)
        xslxFileDownload(xlsxType, workbook);
    }

    /**
     * 다중 목록 엑셀 파일 다운로드 (목록이 여러 개 이어진 형태)
     *
     * @param xlsxType 엑셀 파일 타입 정보
     * @param dataMap 데이터 맵 (key-stream)
     * @throws Exception 엑셀 파일 생성 중 발생할 수 있는 예외
     */
    public void multiListXlxsDownload(final XlsxType xlsxType, final Map<String, Stream<?>> dataMap) throws Exception {

        // 엑셀 파일 생성 (메소드 분리)
        final SXSSFWorkbook workbook = makeMultiListXlsxFile(xlsxType, dataMap);

        // 생성된 엑셀 파일 다운로드 (메소드 분리)
        xslxFileDownload(xlsxType, workbook);
    }

    /**
     * 목록 엑셀 파일 생성 (메소드 분리)
     *
     * @param xlsxType 엑셀 파일 타입 정보
     * @param dataStream 엑셀 파일에 추가할 스트림 데이터
     * @return {@link SXSSFWorkbook} -- 생성된 엑셀 객체
     * @throws Exception 엑셀 파일 생성 중 발생할 수 있는 예외
     */
    public static SXSSFWorkbook makeListXlsxFile(final XlsxType xlsxType, final Stream<?> dataStream) throws Exception {

        // 1. 파일 및 시트 생성
        final SXSSFWorkbook workbook = new SXSSFWorkbook();
        final SXSSFSheet sheet = workbook.createSheet(xlsxType.title);

        // 2. 기본 Cell Style 생성
        final Map<XlsxCellStyle, CellStyle> styleMap = XlsxCellStyle.createStyleMap(workbook);

        // 3-1. Title Row 생성 (메소드 분리)
        createTitleRow(xlsxType, sheet, styleMap);

        // 3-2. Header Row 생성 (메소드 분리)
        createListHeaderRow(xlsxType, sheet, styleMap);

        // 3-3. dataStream Rows 생성 (if row is not empty) (메소드 분리)
        createListDataRows(dataStream, sheet, styleMap);

        return workbook;
    }

    /**
     * 목록 엑셀 파일 생성 (메소드 분리)
     *
     * @param xlsxType 엑셀 파일 타입 정보
     * @param dataMap 키와 데이터 스트림으로 구성된 맵. 각 키는 데이터의 범주를 나타내며, 값은 해당 범주의 데이터 스트림입니다.
     * @return 생성된 엑셀 파일의 SXSSFWorkbook 객체
     * @throws Exception 파일 생성 중 발생할 수 있는 예외
     */
    public static SXSSFWorkbook makeMultiListXlsxFile(final XlsxType xlsxType, Map<String, Stream<?>> dataMap) throws Exception {

        // 1. 파일 및 시트 생성
        final SXSSFWorkbook workbook = new SXSSFWorkbook();
        final SXSSFSheet sheet = workbook.createSheet(xlsxType.title);

        // 2. 기본 Cell Style 생성
        final Map<XlsxCellStyle, CellStyle> styleMap = XlsxCellStyle.createStyleMap(workbook);

        // 3. Title Row 생성 (메소드 분리)
        createTitleRow(xlsxType, sheet, styleMap);

        // 4. 목록들 순회하며 생성
        for (String key : dataMap.keySet()) {
            final Stream<?> dataStream = dataMap.get(key);

            // Header Row 생성 (메소드 분리)
            createListHeaderRow(xlsxType, sheet, styleMap);

            // dataStream Rows 생성 (if row is not empty) (메소드 분리)
            createListDataRows(dataStream, sheet, styleMap);
        }
        return workbook;
    }

    /**
     * 엑셀 파일 최상단 제목title 행 생성 (메소드 분리)
     *
     * @param xlsxType 엑셀 파일 타입 정보
     * @param sheet 제목 행이 생성될 시트입니다.
     * @param styleMap 셀 스타일을 포함하는 맵으로, 각 스타일에 대한 셀 스타일을 가져옵니다.
     */
    public static void createTitleRow(final XlsxType xlsxType, final SXSSFSheet sheet, final Map<XlsxCellStyle, CellStyle> styleMap) {
        final SXSSFRow titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(45);
        final SXSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(xlsxType.title);
        titleCell.setCellStyle(styleMap.get(XlsxCellStyle.TITLE));
        int headerLength = xlsxType.headers.size();
        if (headerLength > 1) {
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headerLength - 1));
        }
    }

    /**
     * 엑셀 파일 목록 Header Row 생성 (메소드 분리)
     * 주어진 {@link XlsxType}에 정의된 헤더 정보를 사용하여 헤더 행 생성.
     *
     * @param xlsxType 엑셀 파일 타입 정보
     * @param sheet 헤더 행이 추가될 {@link SXSSFSheet}
     * @param styleMap 셀 스타일을 정의하는 {@link Map} 객체, 키는 {@link XlsxCellStyle}, 값은 {@link CellStyle}
     */
    public static void createListHeaderRow(final XlsxType xlsxType, final SXSSFSheet sheet, final Map<XlsxCellStyle, CellStyle> styleMap) {
        final SXSSFRow headerRow = sheet.createRow(sheet.getLastRowNum() + 1);
        headerRow.setHeightInPoints(40);
        // IntStream을 사용하여 헤더 리스트를 반복
        IntStream.range(0, xlsxType.headers.size()).forEach(i -> {
            final SXSSFCell headerCell = headerRow.createCell(i);
            sheet.setColumnWidth(i, xlsxType.headers.get(i).getLength() * 256);
            headerCell.setCellStyle(styleMap.get(XlsxCellStyle.HEADER));
            headerCell.setCellValue(xlsxType.headers.get(i).getHeader());
        });
    }

    /**
     * 엑셀 파일 목록 dataStream Rows 생성 (메소드 분리)
     * merged cell에 대하여 자동 열 높이 기능 작동안함. (poi가 아니라 엑셀 자체 문제)
     * TODO:: 필드 캐싱 처리?
     *
     * @param dataStream 엑셀 시트에 작성될 데이터 항목의 스트림
     * @param sheet 데이터 행이 생성될 SXSSFSheet
     * @param styleMap 셀의 형식을 지정하기 위한 XlsxCellStyle와 CellStyle의 맵
     * @throws Exception 파일 생성 중 발생할 수 있는 예외
     */
    private static void createListDataRows(final Stream<?> dataStream, final SXSSFSheet sheet,  final Map<XlsxCellStyle, CellStyle> styleMap) {
        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper

        // row iteration
        final AtomicInteger rowIndex = new AtomicInteger(sheet.getLastRowNum() + 1);
        dataStream.forEach(item -> {
            try {
                final SXSSFRow dataRow = sheet.createRow(rowIndex.getAndIncrement());
                dataRow.setHeight((short) -1);

                final LinkedHashMap<String, Object> rowDataMap = mapper.convertValue(item, LinkedHashMap.class);

                // if column is not empty
                if (MapUtils.isNotEmpty(rowDataMap)) {
                    final String[] dataKeyset = rowDataMap.keySet().toArray(new String[0]);
                    log.debug("dataKeyset: {}", Arrays.toString(dataKeyset));
                    IntStream.range(0, dataKeyset.length).forEach(colIndex -> {
                        final SXSSFCell dataCell = dataRow.createCell(colIndex);
                        final Object valueObj = rowDataMap.get(dataKeyset[colIndex]);
                        // 숫자는 숫자 형식 유지하도록
                        if (valueObj instanceof Integer) {
                            dataCell.setCellValue((Integer) valueObj);
                            final CellStyle cellStyle = styleMap.get(XlsxCellStyle.DATA);
                            cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
                            dataCell.setCellStyle(cellStyle);
                        } else {
                            final String cellValue = valueObj != null ? valueObj.toString() : "";
                            dataCell.setCellValue(cellValue);
                        }
                    });
                }
            } catch (final Exception e) {
                throw new RuntimeException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
            }
        });
    }

    /* ----- */

    /**
     * 셀 생성과 동시에 스타일 적용
     *
     * @param row 생성할 셀의 행
     * @param index 생성할 셀의 인덱스
     * @param style 적용할 셀 스타일
     * @return 생성된 셀
     */
    public static Cell createBlankCellWithStyle(final Row row, final int index, final CellStyle style) {
        final Cell cell = row.createCell(index);
        cell.setCellStyle(style);
        return cell;
    }

    /**
     * 셀 생성과 동시에 스타일 적용, 값 세팅
     *
     * @param row 생성할 셀의 행
     * @param index 생성할 셀의 인덱스
     * @param style 적용할 셀 스타일
     * @param value 설정할 셀의 값
     * @return 생성된 셀
     */
    public static Cell createCellWithStyleInit(final Row row, final int index, final CellStyle style, final String value) {
        final Cell cell = createBlankCellWithStyle(row, index, style);
        cell.setCellValue(value);
        return cell;
    }

    /**
     * 범위로 빈 셀 생성
     *
     * @param row 셀을 생성할 행
     * @param startIndex 생성할 셀의 시작 인덱스
     * @param endIndex 생성할 셀의 종료 인덱스
     * @param style 적용할 셀 스타일
     */
    public static void createBlankCellRangeWithStyle(final Row row, final int startIndex, final int endIndex, final CellStyle style) {
        for (int i = startIndex; i <= endIndex; i++) {
            final Cell cell = row.createCell(i);
            cell.setCellStyle(style);
        }
    }

    /**
     * 생성된 엑셀 파일 다운로드 (메소드 분리)
     *
     * @param xlsxType 엑셀 파일의 유형
     * @param workbook 다운로드할 SXSSFWorkbook 객체
     * @throws IOException 엑셀 파일 다운로드 중 입출력 오류가 발생한 경우
     */
    private static void xslxFileDownload(final XlsxType xlsxType, final SXSSFWorkbook workbook) throws IOException {

        try {
            // 응답 헤더 설정 및 한글 파일명 처리 (메소드 분리)
            FileUtils.setRespnsHeaderAndSuccessCookie(xlsxType.fileNm);

            // Write.xlsx에 대한 출력 스트림 생성
            // try-with-resources로 BufferedOutputStream 자동 리소스 관리
            try (final BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream())) {
                // Write.xlsx에 위에서 입력된 데이터를 씀
                workbook.write(outs);
            }
        } catch (final Exception e) {
            log.info(MessageUtils.getExceptionMsg(e));
        } finally {
            // SXSSFWorkbook에 의해 생성된 임시 파일 정리
            // TODO: 바로 버리는 대신 저장해놓고 캐시?
            workbook.dispose();
        }
    }
}
