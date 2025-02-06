package io.nicheblog.dreamdiary.extension.file.utils;

import io.nicheblog.dreamdiary.extension.file.entity.AtchFileDtlEntity;
import io.nicheblog.dreamdiary.extension.file.entity.AtchFileEntity;
import io.nicheblog.dreamdiary.extension.file.model.AtchFileDtlDto;
import io.nicheblog.dreamdiary.extension.file.service.AtchFileDtlService;
import io.nicheblog.dreamdiary.extension.file.service.AtchFileService;
import io.nicheblog.dreamdiary.global.util.CookieUtils;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * FileUtils
 * <pre>
 *  파일 처리 유틸리티 모듈.
 *  (파일 서비스에서 (서비스 인터페이스에서 쓰는) 파일 업로드 부분을 유틸리티 클래스로 분리)
 * </pre>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class FileUtils {

    private final AtchFileService autowiredFileService;
    private final AtchFileDtlService autowiredFileDtlService;
    private final HttpServletResponse autowiredResponse;

    private static AtchFileService atchFileService;
    private static AtchFileDtlService atchFileDtlService;
    private static HttpServletResponse response;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        atchFileService = autowiredFileService;
        atchFileDtlService = autowiredFileDtlService;
        response = autowiredResponse;
    }

    /**
     * 파일 유무 체크
     *
     * @param fileId 파일 ID 또는 파일 이름 (String)
     * @return {@link Boolean} -- 파일이 존재하면 true, 그렇지 않으면 false를 반환
     */
    public static Boolean fileChck(final String fileId) {
        try {
            // 1. 파일ID일 경우로 상정 ::
            final Integer atchFileDtlNo = Integer.parseInt(fileId);
            final AtchFileDtlEntity fileDtl = atchFileDtlService.getDtlEntity(atchFileDtlNo);
            new File(fileDtl.getFileStrePath(), fileDtl.getStreFileNm());
        } catch (final NumberFormatException e) {
            // 2. 에러시 Integer형 ID가 아닌 것으로 판단, 파일명으로 처리
            log.info(MessageUtils.getExceptionMsg(e));
            new File("content/" + fileId);
            return true;
        } catch (final Exception e) {
            log.info(MessageUtils.getExceptionMsg(e));
            return false;
        }
        return true;
    }

    /**
     * 업로드된 파일을 처리하여 반환합니다. (새 파일 처리)
     *
     * @param multiRequest Multipart 요청
     * @return {@link AtchFileEntity} -- 업로드된 파일 정보
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public static AtchFileEntity getUploadedFile(final MultipartHttpServletRequest multiRequest) throws Exception {
        return getUploadedFile(multiRequest, null);
    }

    /**
     * 업로드된 파일을 처리하여 반환합니다. (기존 파일 또는 새 파일 처리)
     *
     * @param multiRequest Multipart 요청
     * @param atchFileNo 기존에 첨부된 파일 번호 (Integer)
     * @return {@link AtchFileEntity} -- 업로드된 파일 정보
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public static AtchFileEntity getUploadedFile(
            final MultipartHttpServletRequest multiRequest,
            final Integer atchFileNo
    ) throws Exception {
        // 첨부파일 ID 세팅
        AtchFileEntity atchFile = (atchFileNo != null) ? atchFileService.getDtlEntity(atchFileNo) : null;
        if (atchFile == null) atchFile = AtchFileEntity.builder().build();
        final List<AtchFileDtlEntity> atchFileList = atchFile.getAtchFileList();
        final boolean isAtchFileListEmpty = CollectionUtils.isEmpty(atchFileList);

        // 파일 처리
        // input file이 안 넘어오는 경우
        final Map<String, MultipartFile> fileMap = multiRequest.getFileMap();
        final boolean isMultipartFileEmpty = MapUtils.isEmpty(fileMap);
        if (isMultipartFileEmpty) {
            // 추가된(multipart로 요청된) 파일도 없고 기존 파일도 없으면 리턴
            if (isAtchFileListEmpty) return null;
            // 삭제된(del 플래그가 전달된) 파일에 대하여 DB삭제 플래그 세팅(atchCtrl="D") (메소드 분리)
            atchFileDtlService.delFile(multiRequest, atchFileList);
        }
        // 추가된(multipart로 요청된) 파일에 대하여 업로드+DB추가
        atchFileDtlService.addFiles(multiRequest, atchFileList);
        atchFile.cascade();
        return atchFileService.updt(atchFile);
    }

    /**
     * 파일 업로드 (새 파일 처리)
     *
     * @param multiRequest Multipart 요청
     * @return {@link Integer} -- 업로드된 파일의 첨부파일 번호
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public static Integer uploadFile(final MultipartHttpServletRequest multiRequest) throws Exception {
        return uploadFile(multiRequest, null);
    }

    /**
     * 파일 업로드 (기존 파일 또는 새 파일 처리)
     *
     * @param multiRequest Multipart 요청
     * @param atchFileNo 기존에 첨부된 파일 번호 (Integer), null일 경우 새로 첨부된 파일 처리
     * @return {@link Integer} -- 업로드된 파일의 첨부파일 번호
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public static Integer uploadFile(
            final MultipartHttpServletRequest multiRequest,
            final Integer atchFileNo
    ) throws Exception {

        try {
            final AtchFileEntity rslt = getUploadedFile(multiRequest, atchFileNo);
            if (rslt == null) return null;
            return rslt.getAtchFileNo();
        } catch (final Exception e) {
            MessageUtils.alertMessage("파일 업로드에 실패했습니다.");
        }
        return atchFileNo;
    }

    /**
     * 파일 업로드 (새 파일 처리)
     *
     * @param multiRequest Multipart 요청
     * @return {@link AtchFileDtlDto} -- 업로드된 파일 객체 (단일 파일로 간주, 첫 번째 파일 반환)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public static AtchFileDtlDto uploadDtlFile(final MultipartHttpServletRequest multiRequest) throws Exception {
        final AtchFileEntity rslt = getUploadedFile(multiRequest);
        if (rslt == null || CollectionUtils.isEmpty(rslt.getAtchFileList())) return null;
        return rslt.getAtchFileList().get(0).asDto();
    }

    /**
     * 메소드 분리 :: 삭제된 파일에 대하여 DB 삭제 플래그 세팅
     *
     * @param multiRequest Multipart 요청
     * @return {@link Integer} -- 업로드된 파일의 첨부파일 번호 (Integer)
     */
    private List<AtchFileDtlEntity> delFile(
            final MultipartHttpServletRequest multiRequest,
            final List<AtchFileDtlEntity> atchFileList
    ) {
        if (CollectionUtils.isEmpty(atchFileList)) return null;
        return atchFileList.stream()
                .peek(atchFileDtl -> {
                    String atchCtrl = multiRequest.getParameter("atchCtrl" + atchFileDtl.getAtchFileDtlNo());
                    if ("D".equals(atchCtrl)) atchFileDtl.setDelYn("Y");
                    // TODO: 실제 파일 삭제?
                })
                .collect(Collectors.toList());
    }

    /**
     * 메소드 분리 :: 파일 다운로드
     *
     * @param file 다운로드할 파일 객체
     * @throws Exception 파일 처리 중 발생할 수 있는 예외
     */
    public static void downloadFile(final File file) throws Exception {
        downloadFile(file, file.getName());
    }

    /**
     * 메소드 분리 :: 파일 다운로드 (파일 이름 지정)
     *
     * @param file 다운로드할 파일 객체
     * @param fileNm 클라이언트에게 전달할 파일 이름 (String)
     * @throws Exception 파일 처리 중 발생할 수 있는 예외
     */
    public static void downloadFile(final File file, final String fileNm) throws Exception {
        FileUtils.setRespnsHeaderAndSuccessCookie(fileNm);       // 응답 헤더 설정 및 한글 파일명 처리 (메소드 분리)
        response.setHeader("Content-Length", String.valueOf(file.length()));        // 파일 크기 설정

        // try-with-resources를 사용하여 스트림을 자동으로 닫음
        try (final InputStream is = new FileInputStream(file);
             final OutputStream os = response.getOutputStream()) {

            // 10MB 기준으로 flush 처리
            final int FLUSH_SIZE = 10 * 1024 * 1024;

            final byte[] buffer = new byte[2048];
            int bytesRead;
            int bytesBuffered = 0;
            while ((bytesRead = is.read(buffer)) > -1) {
                os.write(buffer, 0, bytesRead);
                bytesBuffered += bytesRead;

                // 10MB마다 flush
                if (bytesBuffered >= FLUSH_SIZE) {
                    os.flush();
                    bytesBuffered = 0; // 다시 0으로 초기화
                }
            }
            // 마지막 남은 데이터 flush
            os.flush();
        } catch (final IOException e) {
            log.error("파일 다운로드 중 오류 발생: {}", e.getMessage(), e);
            throw new IOException("파일 다운로드에 실패했습니다.", e);
        }
    }

    /**
     * 응답 헤더 설정 및 한글 파일명 처리 (메소드 분리)
     *
     * @param fileNm 다운로드 시 클라이언트에게 전달할 파일 이름
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public static void setRespnsHeader(final String fileNm) throws Exception {
        final HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        final String client = request.getHeader("User-Agent");

        // 브라우저가 IE 및 IE11일 경우 별도 처리
        if (client.contains("MSIE") || client.contains("rv:11.0")) {
            final String encodedFileName = URLEncoder.encode(fileNm, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\";");
        } else {
            // 비IE 브라우저의 한글 파일명 처리
            final String korFileNm = new String(fileNm.getBytes("euc-kr"), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + korFileNm + "\"");
            response.setHeader("Content-type", "application/octet-stream; charset=euc-kr");
        }
    }

    /**
     * 응답 헤더 설정 및 한글 파일명 처리 + 다운로드 성공 쿠키 추가 (메소드 분리)
     *
     * @param fileNm 다운로드 시 클라이언트에게 전달할 파일 이름
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public static void setRespnsHeaderAndSuccessCookie(final String fileNm) throws Exception {
        setRespnsHeader(fileNm);
        CookieUtils.setFileDownloadSuccessCookie();
    }
}