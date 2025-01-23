package io.nicheblog.dreamdiary.global._common.file.service;

import io.nicheblog.dreamdiary.global._common.file.entity.AtchFileDtlEntity;
import io.nicheblog.dreamdiary.global._common.file.mapstruct.AtchFileDtlMapstruct;
import io.nicheblog.dreamdiary.global._common.file.model.AtchFileDtlDto;
import io.nicheblog.dreamdiary.global._common.file.repository.jpa.AtchFileDtlRepository;
import io.nicheblog.dreamdiary.global._common.file.spec.AtchFileDtlSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class AtchFileDtlService
        implements BaseCrudService<AtchFileDtlDto, AtchFileDtlDto, Integer, AtchFileDtlEntity, AtchFileDtlRepository, AtchFileDtlSpec, AtchFileDtlMapstruct> {

    @Getter
    private final AtchFileDtlRepository repository;
    @Getter
    private final AtchFileDtlSpec spec;
    @Getter
    private final AtchFileDtlMapstruct mapstruct = AtchFileDtlMapstruct.INSTANCE;

    /**
     * 첨부파일 상세 목록 조회 (dto level)
     *
     * @param atchFileNo 조회할 첨부파일 묶음 번호
     * @return {@link List} -- 첨부파일 상세 정보 목록
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public List<AtchFileDtlDto> getPageDto(final Integer atchFileNo) throws Exception {
        Map<String, Object> paramMap = new HashMap<>() {{
            put("atchFileNo", atchFileNo);
        }};
        return this.getListDto(paramMap);
    }

    /**
     * 추가된 파일에 대하여 업로드 및 정보 DB에 등록한다.
     *
     * @param multiRequest 요청 정보
     * @param atchFileList 업로드된 파일 정보 목록
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional
    public void addFiles(
            final MultipartHttpServletRequest multiRequest,
            final List<AtchFileDtlEntity> atchFileList
    ) throws Exception {

        String path = "upfile/";
        File file = new File(path);
        if (!file.exists()) if (!file.mkdirs()) throw new Exception("폴더 생성에 실패했습니다.");
        path = "upfile/" + DateUtils.getCurrDateStr(DatePtn.DATE) + "/";
        file = new File(path);
        if (!file.exists()) if (!file.mkdirs()) throw new Exception("폴더 생성에 실패했습니다.");

        final Iterator<String> fileNmIterator = multiRequest.getFileNames();
        String fileInputNm, orgnFileNm, orgnFileExtn, replaceFileNm;
        while (fileNmIterator.hasNext()) {
            fileInputNm = fileNmIterator.next();
            final MultipartFile multipartFile = multiRequest.getFile(fileInputNm);
            if (multipartFile == null || multipartFile.isEmpty()) {
                log.debug("file is Empty...");
                continue;
            }
            // String fileIdx = fileInputNm.replace("atchFile", "");        // TODO: 파일 순번이 중요한 시점이 올수도 있다.
            orgnFileNm = Optional.ofNullable(multipartFile.getOriginalFilename())
                    .orElse(DateUtils.getCurrDateStr(DatePtn.DATE));
            orgnFileExtn = orgnFileNm.substring(orgnFileNm.lastIndexOf('.') + 1);
            replaceFileNm = System.nanoTime() + "." + orgnFileExtn;
            // 실제 파일 저장
            //multipartfile.transferto(file)를 사용시 절대경로를 사용하지 않으면 문제 발생!
            final Path abPath = Paths.get(path + replaceFileNm)
                    .toAbsolutePath();
            final String abPathStr = Paths.get(path)
                    .toAbsolutePath()
                    .toString();
            log.debug("absolute path: {} pathStr: {}", abPath, abPathStr);
            multipartFile.transferTo(abPath.toFile());
            /* 파일정보 DB 저장 준비 (entity에 할당) */
            final AtchFileDtlEntity fileEntity = new AtchFileDtlEntity();
            fileEntity.setFileSize(multipartFile.getSize());
            // TODO: 파일명 특수문자 등 있으면 처리
            fileEntity.setOrgnFileNm(orgnFileNm);
            fileEntity.setStreFileNm(replaceFileNm);
            fileEntity.setFileExtn(orgnFileExtn);
            fileEntity.setFileStrePath(abPathStr);
            fileEntity.setUrl("/" + path + replaceFileNm);
            atchFileList.add(fileEntity);
        }
    }

    /**
     * 삭제된 파일에 대하여 DB 삭제 플래그를 세팅한다.
     *
     * @param multiRequest 요청 정보
     * @param atchFileList 업로드된 파일 정보 목록
     */
    public void delFile(
            final MultipartHttpServletRequest multiRequest,
            final List<AtchFileDtlEntity> atchFileList
    ) {
        if (CollectionUtils.isEmpty(atchFileList)) return;
        atchFileList.stream()
                .peek(atchFileDtl -> {
                    String atchCtrl = multiRequest.getParameter("atchCtrl" + atchFileDtl.getAtchFileDtlNo());
                    if ("D".equals(atchCtrl)) atchFileDtl.setDelYn("Y");
                    // TODO: 실제 파일 삭제?
                });
    }
}
