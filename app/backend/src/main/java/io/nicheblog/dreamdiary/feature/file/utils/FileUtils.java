package io.nicheblog.dreamdiary.feature.file.utils;

import io.nicheblog.dreamdiary.feature.file.entity.FileGroupEntity;
import io.nicheblog.dreamdiary.feature.file.entity.FileRecordEntity;
import io.nicheblog.dreamdiary.feature.file.model.FileRecordDto;
import io.nicheblog.dreamdiary.feature.file.service.FileGroupService;
import io.nicheblog.dreamdiary.feature.file.service.FileRecordService;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.global.util.UUIDUtils;
import io.nicheblog.dreamdiary.infrastructure.web.util.CookieUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * FileUtils
 * <pre>
 *  파일 처리 유틸리티 모듈.
 *  (파일 서비스에서 (서비스 인터페이스에서 쓰는) 파일 업로드 부분을 유틸리티 클래스로 분리)
 * </pre>
 *
 * 변경 전: {@code org.apache.commons.io.FileUtils} 상속 — commons-io 2.9+ 에서 해당 생성자가 deprecated 라
 * 암묵적 super() 호출로 "uses or overrides a deprecated API" 경고 발생.
 * 변경 후: 상속 제거 (상속 static 메서드를 이 클래스 경유로 호출하는 곳 0건 확인 — commons-io 는 필요 시 직접 사용).
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class FileUtils {

    private final FileGroupService autowiredFileService;
    private final FileRecordService autowiredFileDtlService;
    private final HttpServletResponse autowiredResponse;

    private static FileGroupService fileGroupService;
    private static FileRecordService fileRecordService;
    private static HttpServletResponse response;

    private static final String ILLEGAL_EXP = "[:\\\\/%*?|;\"<>]";

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        fileGroupService = autowiredFileService;
        fileRecordService = autowiredFileDtlService;
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
            final Integer fileRecordId = Integer.parseInt(fileId);
            final FileRecordEntity fileDtl = fileRecordService.getDtlEntity(fileRecordId);
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
     * 파일 이름이 유효한지 확인한다.
     *
     * @param fileName 파일의 이름, Path를 제외한 순수한 파일의 이름
     * @return boolean -- 유효한 파일 이름이면 true, 그렇지 않으면 false를 반환
     */
    public static boolean isValidFileName(final String fileName) {
        if (StringUtils.isEmpty(fileName)) return false;

        return !Pattern.compile(ILLEGAL_EXP).matcher(fileName).find();
    }

    public static void ensureDirectory(final String path) throws IOException {
        final File directory = new File(path);
        if (!directory.exists() && !directory.mkdirs()) throw new IOException(MessageUtils.getMessage("common.result.mkdir-failed"));
        log.info("Startup check completed. resource=directory path={} status=ready", path);
    }

    /**
     * 파일 이름에 사용할 수 없는 캐릭터를 바꿔서 유효한 파일로 만든다.
     *
     * @param fileName 파일 이름, Path를 제외한 순수한 파일의 이름.
     * @return String -- 유효한 파일 이름
     */
    public static String sanitizeFileName(final String fileName) {
        return sanitizeFileName(fileName, "_");
    }

    /**
     * 파일 이름에 사용할 수 없는 캐릭터를 바꿔서 유효한 파일로 만든다.
     *
     * @param fileName 파일 이름, Path를 제외한 순수한 파일의 이름.
     * @param replaceStr 파일 이름에 사용할 수 없는 캐릭터의 교체 문자
     * @return String -- 유효한 파일 이름
     */
    public static String sanitizeFileName(final String fileName, final String replaceStr) {
        if (StringUtils.isEmpty(fileName)) return UUIDUtils.getUUID();

        // 1. 기본 특수문자 치환
        String sanitized = fileName.replaceAll(ILLEGAL_EXP, Objects.requireNonNullElse(replaceStr, "_"));
        // 2. 디렉터리 트래버설 방어 (.. -> _)
        sanitized = sanitized.replaceAll("\\.+", "_");
        // 3. 파일명이 . 또는 _ 같은 특수문자로만 이루어졌다면 랜덤 UUID 적용
        if (sanitized.matches("^[._]+$")) return UUIDUtils.getUUID();

        return sanitized.trim();
    }

    /**
     * 업로드된 파일을 처리하여 반환합니다. (새 파일 처리)
     *
     * @param multiRequest Multipart 요청
     * @return {@link FileGroupEntity} -- 업로드된 파일 정보
     */
    public static FileGroupEntity getUploadedFile(final MultipartHttpServletRequest multiRequest) throws Exception {
        return getUploadedFile(multiRequest, null);
    }

    /**
     * 업로드된 파일을 처리하여 반환합니다. (기존 파일 또는 새 파일 처리)
     *
     * @param multiRequest Multipart 요청
     * @param fileGroupId 기존에 첨부된 파일 번호 (Integer)
     * @return {@link FileGroupEntity} -- 업로드된 파일 정보
     */
    public static FileGroupEntity getUploadedFile(
            final MultipartHttpServletRequest multiRequest,
            final Integer fileGroupId
    ) throws Exception {

        // 첨부파일 ID 세팅
        final FileGroupEntity fileGroup = (fileGroupId != null) ? fileGroupService.getDtlEntity(fileGroupId) : FileGroupEntity.builder().build();
        final List<FileRecordEntity> fileRecordList = fileGroup.getFileRecordList();

        // 파일 처리
        // input file이 안 넘어오는 경우
        final Map<String, MultipartFile> fileMap = multiRequest.getFileMap();
        final boolean isMultipartFileEmpty = MapUtils.isEmpty(fileMap);
        if (isMultipartFileEmpty) {
            // 추가된(multipart로 요청된) 파일도 없고 기존 파일도 없으면 리턴
            final boolean isFileGroupListEmpty = CollectionUtils.isEmpty(fileRecordList);
            if (isFileGroupListEmpty) return null;

            // 삭제된(del 플래그가 전달된) 파일에 대하여 DB삭제 플래그 세팅(atchCtrl="D") (메소드 분리)
            fileRecordService.delFile(multiRequest, fileRecordList);
        }
        // 추가된(multipart로 요청된) 파일에 대하여 업로드+DB추가
        return fileGroupService.procFiles(multiRequest, fileGroup, fileRecordList);
    }

    /**
     * 파일 업로드 (새 파일 처리)
     *
     * @param multiRequest Multipart 요청
     * @return {@link Integer} -- 업로드된 파일의 첨부파일 번호
     */
    public static Integer uploadFile(final MultipartHttpServletRequest multiRequest) throws Exception {
        return uploadFile(multiRequest, null);
    }

    /**
     * 파일 업로드 (기존 파일 또는 새 파일 처리)
     *
     * @param multiRequest Multipart 요청
     * @param fileGroupId 기존에 첨부된 파일 번호 (Integer), null일 경우 새로 첨부된 파일 처리
     * @return {@link Integer} -- 업로드된 파일의 첨부파일 번호
     */
    public static Integer uploadFile(
            final MultipartHttpServletRequest multiRequest,
            final Integer fileGroupId
    ) throws Exception {

        try {
            final FileGroupEntity rslt = getUploadedFile(multiRequest, fileGroupId);
            if (rslt == null) return null;

            return rslt.getId();
        } catch (final Exception e) {
            MessageUtils.alertMessageByKey("file.upload.failure");
        }
        return fileGroupId;
    }

    /**
     * 파일 업로드 (새 파일 처리)
     *
     * @param multiRequest Multipart 요청
     * @return {@link FileRecordDto} -- 업로드된 파일 객체 (단일 파일로 간주, 첫 번째 파일 반환)
     */
    public static FileRecordDto uploadDtlFile(final MultipartHttpServletRequest multiRequest) throws Exception {
        final FileGroupEntity rslt = getUploadedFile(multiRequest);
        if (rslt == null || CollectionUtils.isEmpty(rslt.getFileRecordList())) return null;

        return rslt.getFileRecordList().get(0).asDto();
    }

    /**
     * 메소드 분리 :: 삭제된 파일에 대하여 DB 삭제 플래그 세팅
     *
     * @param multiRequest Multipart 요청
     * @return {@link Integer} -- 업로드된 파일의 첨부파일 번호 (Integer)
     */
    private List<FileRecordEntity> delFile(
            final MultipartHttpServletRequest multiRequest,
            final List<FileRecordEntity> fileRecordList
    ) {
        if (CollectionUtils.isEmpty(fileRecordList)) return null;

        return fileRecordList.stream()
                .peek(fileRecord -> {
                    String atchCtrl = multiRequest.getParameter("atchCtrl" + fileRecord.getId());
                    if ("D".equals(atchCtrl)) fileRecord.setDeletedAt(DateUtils.getCurrLocalDateTime());
                    // TODO: 실제 파일 삭제?
                })
                .collect(Collectors.toList());
    }

    /**
     * 메소드 분리 :: 파일 다운로드
     *
     * @param file 다운로드할 파일 객체
     */
    public static void downloadFile(final File file) throws Exception {
        downloadFile(file, file.getName());
    }

    /**
     * 메소드 분리 :: 파일 다운로드 (파일 이름 지정)
     *
     * @param file 다운로드할 파일 객체
     * @param fileNm 클라이언트에게 전달할 파일 이름 (String)
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
            throw new IOException("file.download.failure", e);
        }
    }

    /**
     * 응답 헤더 설정 및 한글 파일명 처리 (메소드 분리)
     *
     * @param fileNm 다운로드 시 클라이언트에게 전달할 파일 이름
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
     */
    public static void setRespnsHeaderAndSuccessCookie(final String fileNm) throws Exception {
        setRespnsHeader(fileNm);
        CookieUtils.setFileDownloadSuccessCookie();
    }
}
