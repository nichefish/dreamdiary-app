package io.nicheblog.dreamdiary.extension.file.service;

import io.nicheblog.dreamdiary.extension.file.entity.AtchFileDtlEntity;
import io.nicheblog.dreamdiary.extension.file.entity.AtchFileEntity;
import io.nicheblog.dreamdiary.extension.file.mapstruct.AtchFileMapstruct;
import io.nicheblog.dreamdiary.extension.file.model.AtchFileDto;
import io.nicheblog.dreamdiary.extension.file.repository.jpa.AtchFileRepository;
import io.nicheblog.dreamdiary.extension.file.spec.AtchFileSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;

/**
 * AtchFileService
 * <pre>
 *  공통 > 파일 처리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("atchFileService")
@RequiredArgsConstructor
public class AtchFileService
        implements BaseCrudService<AtchFileDto, AtchFileDto, Integer, AtchFileEntity> {

    @Getter
    private final AtchFileRepository repository;
    @Getter
    private final AtchFileSpec spec;
    @Getter
    private final AtchFileMapstruct mapstruct = AtchFileMapstruct.INSTANCE;

    public AtchFileMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public AtchFileMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final AtchFileDtlService atchFileDtlService;

    private final ApplicationContext context;
    private AtchFileService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 파일 처리.
     *
     * @param multiRequest 파일 업로드 요청 객체
     * @param atchFileList 파일 목록
     * @return {@link AtchFileEntity} -- 업로드된 파일 정보
     */
    @Transactional
    public AtchFileEntity procFiles(MultipartHttpServletRequest multiRequest, AtchFileEntity atchFile, List<AtchFileDtlEntity> atchFileList) throws Exception {
        atchFileDtlService.addFiles(multiRequest, atchFileList);
        atchFile.cascade();
        return this.getSelf().updt(atchFile);
    }
}
