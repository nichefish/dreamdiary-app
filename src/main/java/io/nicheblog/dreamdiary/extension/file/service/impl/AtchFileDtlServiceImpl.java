package io.nicheblog.dreamdiary.extension.file.service.impl;

import io.nicheblog.dreamdiary.extension.file.config.FileConfig;
import io.nicheblog.dreamdiary.extension.file.entity.AtchFileDtlEntity;
import io.nicheblog.dreamdiary.extension.file.mapstruct.AtchFileDtlMapstruct;
import io.nicheblog.dreamdiary.extension.file.model.AtchFileDtlDto;
import io.nicheblog.dreamdiary.extension.file.repository.jpa.AtchFileDtlRepository;
import io.nicheblog.dreamdiary.extension.file.service.AtchFileDtlService;
import io.nicheblog.dreamdiary.extension.file.spec.AtchFileDtlSpec;
import io.nicheblog.dreamdiary.extension.file.utils.FileUtils;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.UUIDUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * AtchFileDtlService
 * <pre>
 *  공통 > 상세 파일 처리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("atchFileDtlService")
@RequiredArgsConstructor
@Log4j2
public class AtchFileDtlServiceImpl
        implements AtchFileDtlService {

    @Getter
    private final AtchFileDtlRepository repository;
    @Getter
    private final AtchFileDtlSpec spec;
    @Getter
    private final AtchFileDtlMapstruct mapstruct = AtchFileDtlMapstruct.INSTANCE;

    private final FileConfig fileConfig;

    private final ApplicationContext context;
    private AtchFileDtlService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 첨부파일 상세 목록 조회 (dto level)
     *
     * @param atchFileNo 조회할 첨부파일 묶음 번호
     * @return {@link List} -- 첨부파일 상세 정보 목록
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public List<AtchFileDtlDto> getPageDto(final Integer atchFileNo) throws Exception {
        final Map<String, Object> paramMap = new HashMap<>() {{
            put("atchFileNo", atchFileNo);
        }};
        return this.getSelf().getListDto(paramMap);
    }

    /**
     * 추가된 파일에 대하여 업로드 및 정보 DB에 등록한다.
     *
     * @param multiRequest 요청 정보
     * @param atchFileList 업로드된 파일 정보 목록
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional
    public void addFiles(final MultipartHttpServletRequest multiRequest, final List<AtchFileDtlEntity> atchFileList) throws Exception {

        // 파일 업로드 경로 생성
        final String fileUploadPath = Constant.UPFILE_PATH + DateUtils.getCurrDateStr(DatePtn.PDATE) + "/";
        final File fileUploadDirectory = new File(fileUploadPath);
        if (!fileUploadDirectory.exists() && !fileUploadDirectory.mkdirs()) throw new IOException(MessageUtils.getMessage("common.status.mkdir-failed"));

        // 파일 순회하며 업로드 처리
        final Iterator<String> fileNmIterator = multiRequest.getFileNames();
        while (fileNmIterator.hasNext()) {
            final String fileInputNm = fileNmIterator.next();
            final MultipartFile multipartFile = multiRequest.getFile(fileInputNm);
            if (multipartFile == null || multipartFile.isEmpty()) {
                log.debug("file is Empty...");
                continue;
            }

            // String fileIdx = fileInputNm.replace("atchFile", "");        // TODO: 파일 순번이 중요한 시점이 올수도 있다.

            // 파일명 체크
            final String orgnFileNmRaw = Optional.ofNullable(multipartFile.getOriginalFilename()).orElse(DateUtils.getCurrDateStr(DatePtn.DATE));
            final String orgnFileNm = FileUtils.sanitizeFileName(orgnFileNmRaw);
            if (!FileUtils.isValidFileName(orgnFileNm)) throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
            // 확장자 체크
            final String orgnFileExtn = Optional.ofNullable(FilenameUtils.getExtension(orgnFileNm)).orElse("");
            if (!fileConfig.getAllowedExtensions().contains(orgnFileExtn.toLowerCase())) throw new IllegalArgumentException("확장자가 유효하지 않습니다.");
            // 마임타입 체크
            final String contentType = Optional.ofNullable(multipartFile.getContentType()).orElse("application/octet-stream");
            if (!fileConfig.getAllowedMimeTypes().contains(contentType)) throw new IllegalArgumentException("MimeType이 유효하지 않습니다.");
            
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
            final AtchFileDtlEntity fileEntity = AtchFileDtlEntity.builder()
                    .fileSize(multipartFile.getSize())
                    .orgnFileNm(orgnFileNm)
                    .streFileNm(replaceFileNm)
                    .fileExtn(orgnFileExtn)
                    .contentType(contentType)
                    .fileStrePath(abPathStr)
                    .url("/" + fileUploadPath + replaceFileNm)
                    // TODO: 파일명 특수문자 등 있으면 처리 필요
                    .build();
            atchFileList.add(fileEntity);
        }
    }

    /**
     * 삭제된 파일에 대하여 DB 삭제 플래그를 세팅한다.
     *
     * @param multiRequest 요청 정보
     * @param atchFileList 업로드된 파일 정보 목록
     */
    public void delFile(final MultipartHttpServletRequest multiRequest, final List<AtchFileDtlEntity> atchFileList) {
        if (CollectionUtils.isEmpty(atchFileList)) return;

        atchFileList.stream()
                .peek(atchFileDtl -> {
                    String atchCtrl = multiRequest.getParameter("atchCtrl" + atchFileDtl.getAtchFileDtlNo());
                    if ("D".equals(atchCtrl)) atchFileDtl.setDelYn("Y");
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
