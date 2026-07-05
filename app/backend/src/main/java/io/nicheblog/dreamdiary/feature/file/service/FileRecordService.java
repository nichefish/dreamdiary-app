package io.nicheblog.dreamdiary.feature.file.service;

import io.nicheblog.dreamdiary.feature.file.config.FileConfig;
import io.nicheblog.dreamdiary.feature.file.entity.FileRecordEntity;
import io.nicheblog.dreamdiary.feature.file.jpa.FileRecordRepository;
import io.nicheblog.dreamdiary.feature.file.mapstruct.FileRecordMapstruct;
import io.nicheblog.dreamdiary.feature.file.model.FileRecordDto;
import io.nicheblog.dreamdiary.feature.file.spec.FileRecordSpec;
import io.nicheblog.dreamdiary.feature.file.utils.FileUtils;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.util.UUIDUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * FileRecordService
 * <pre>
 *  공통 > 상세 파일 처리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class FileRecordService
        implements BaseDtoWritableService<FileRecordDto, FileRecordDto, Integer, FileRecordEntity> {

    @Getter
    private final FileRecordRepository repository;
    @Getter
    private final FileRecordSpec spec;
    @Getter
    private final FileRecordMapstruct mapstruct = FileRecordMapstruct.INSTANCE;

    public FileRecordMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public FileRecordMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final FileConfig fileConfig;

    private final ApplicationContext context;
    private FileRecordService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 첨부파일 상세 목록 조회 (dto level)
     *
     * @param fileGroupId 조회할 첨부파일 묶음 번호
     * @return {@link List} -- 첨부파일 상세 정보 목록
     */
    public List<FileRecordDto> getPageDto(final Integer fileGroupId) throws Exception {
        final Map<String, Object> paramMap = new HashMap<>() {{
            put("fileGroupId", fileGroupId);
        }};

        return this.getSelf().getListDto(paramMap);
    }

    /**
     * 추가된 파일에 대하여 업로드 및 정보 DB에 등록한다.
     *
     * @param multiRequest 요청 정보
     * @param fileRecordList 업로드된 파일 정보 목록
     */
    @Transactional
    public void addFiles(final MultipartHttpServletRequest multiRequest, final List<FileRecordEntity> fileRecordList) throws Exception {

        // 파일 업로드 경로 생성
        final String fileUploadPath = Constant.UPFILE_PATH + DateUtils.getCurrDateStr(DatePtn.PDATE) + "/";
        FileUtils.ensureDirectory(fileUploadPath);

        // 파일 순회하며 업로드 처리
        final Iterator<String> fileNmIterator = multiRequest.getFileNames();
        while (fileNmIterator.hasNext()) {
            final String fileInputNm = fileNmIterator.next();
            final MultipartFile multipartFile = multiRequest.getFile(fileInputNm);
            if (multipartFile == null || multipartFile.isEmpty()) {
                log.debug("file is Empty...");
                continue;
            }

            // String fileIdx = fileInputNm.replace("fileGroup", "");        // TODO: 파일 순번이 중요한 시점이 올수도 있다.

            // 파일명 체크
            final String orgnFileNmRaw = Optional.ofNullable(multipartFile.getOriginalFilename()).orElse(DateUtils.getCurrDateStr(DatePtn.DATE));
            final String orgnFileNm = FileUtils.sanitizeFileName(orgnFileNmRaw);
            if (!FileUtils.isValidFileName(orgnFileNm)) throw new IllegalArgumentException("file.name.invalid");
            // 확장자 체크
            final String orgnFileExtn = Optional.ofNullable(FilenameUtils.getExtension(orgnFileNm)).orElse("");
            if (!fileConfig.getAllowedExtensions().contains(orgnFileExtn.toLowerCase())) throw new IllegalArgumentException("file.extension.invalid");
            // 마임타입 체크
            final String contentType = Optional.ofNullable(multipartFile.getContentType()).orElse("application/octet-stream");
            if (!fileConfig.getAllowedMimeTypes().contains(contentType)) throw new IllegalArgumentException("file.mime-type.invalid");

            // TODO: Tika 이용한 더 정밀한 파일 타입 검증

            // 실제 파일 저장 경로 생성
            // multipartfile.transferto(file)를 사용시 절대경로를 사용하지 않으면 문제 발생!
            final String uuidStr = UUIDUtils.getUUID();
            final String replaceFileNm = uuidStr + "." + orgnFileExtn;
            final Path abPath = Paths.get(fileUploadPath, replaceFileNm).toAbsolutePath();
            final String abPathStr = abPath.getParent().toString();
            log.debug("absolute path: {} pathStr: {}", abPath, abPathStr);
            // 실제 파일 저장
            final File saveFile = abPath.toFile();
            multipartFile.transferTo(saveFile);
            // 이미지 파일의 경우 썸네일 생성
            if (fileConfig.getImageExtensions().contains(orgnFileExtn.toLowerCase())) {
                final Path thumbPath = Paths.get(fileUploadPath, uuidStr + "_t." + orgnFileExtn).toAbsolutePath();
                this.getSelf().makeThumbnail(abPath, contentType, thumbPath);
            }

            /* 파일정보 DB 저장 준비 (entity에 할당) */
            final FileRecordEntity fileEntity = FileRecordEntity.builder()
                    .fileSize(multipartFile.getSize())
                    .orgnFileNm(orgnFileNm)
                    .streFileNm(replaceFileNm)
                    .fileExtn(orgnFileExtn)
                    .contentType(contentType)
                    .fileStrePath(abPathStr)
                    .url("/" + fileUploadPath + replaceFileNm)
                    // TODO: 파일명 특수문자 등 있으면 처리 필요
                    .build();
            fileRecordList.add(fileEntity);
        }
    }

    /**
     * 삭제된 파일에 대하여 DB 삭제 플래그를 세팅한다.
     *
     * @param multiRequest 요청 정보
     * @param fileRecordList 업로드된 파일 정보 목록
     */
    public void delFile(final MultipartHttpServletRequest multiRequest, final List<FileRecordEntity> fileRecordList) {
        if (CollectionUtils.isEmpty(fileRecordList)) return;

        fileRecordList.stream()
                .peek(fileRecord -> {
                    String atchCtrl = multiRequest.getParameter("atchCtrl" + fileRecord.getId());
                    if ("D".equals(atchCtrl)) fileRecord.setDeletedAt(DateUtils.getCurrLocalDateTime());
                    // TODO: 실제 파일 삭제?
                });
    }

    /**
     * 이미지 파일에 대하여 썸네일 생성
     *
     * @param orgImagePath 원본 이미지 경로
     * @param contentType 컨텐츠 타입
     * @param thumbPath 썸네일 경로
     */
    public void makeThumbnail(final Path orgImagePath, final String contentType, final Path thumbPath) {
        // 기본 썸네일 크기
        int dw = 250, dh = 140;
        try {
            final String formatName = contentType.replace("image/", "").toUpperCase();
            if("SVG+XML".equals(formatName)) {
                Files.copy(orgImagePath, thumbPath, StandardCopyOption.REPLACE_EXISTING);
                return;
            }

            final BufferedImage srcImg = ImageIO.read(orgImagePath.toFile());
            int ow = srcImg.getWidth();
            int oh = srcImg.getHeight();
            if(dw > ow) {
                Files.copy(orgImagePath, thumbPath, StandardCopyOption.REPLACE_EXISTING);
                return;
            }

            int nw = ow; int nh = (ow * dh) / dw;
            if(nh > oh) {
                nw = (oh * dw) / dh;
                nh = oh;
            }
            final BufferedImage cropImg = Scalr.crop(srcImg, (ow-nw)/2, (oh-nh)/2, nw, nh);
            final BufferedImage destImg = Scalr.resize(cropImg, dw, dh);
            ImageIO.write(destImg, formatName, thumbPath.toFile());
        } catch(Exception e) {
            log.error(e);
        }
    }
}
