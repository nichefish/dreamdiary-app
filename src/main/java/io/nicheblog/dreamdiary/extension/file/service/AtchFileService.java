package io.nicheblog.dreamdiary.extension.file.service;

import io.nicheblog.dreamdiary.extension.file.entity.AtchFileDtlEntity;
import io.nicheblog.dreamdiary.extension.file.entity.AtchFileEntity;
import io.nicheblog.dreamdiary.extension.file.mapstruct.AtchFileMapstruct;
import io.nicheblog.dreamdiary.extension.file.model.AtchFileDto;
import io.nicheblog.dreamdiary.extension.file.repository.jpa.AtchFileRepository;
import io.nicheblog.dreamdiary.extension.file.spec.AtchFileSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
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
public interface AtchFileService
        extends BaseCrudService<AtchFileDto, AtchFileDto, Integer, AtchFileEntity, AtchFileRepository, AtchFileSpec, AtchFileMapstruct> {

    /**
     * 파일 처리.
     *
     * @param multiRequest 파일 업로드 요청 객체
     * @param atchFileList 파일 목록
     * @return {@link AtchFileEntity} -- 업로드된 파일 정보
     * @throws Exception 업로드 처리 중 발생할 수 있는 예외
     */
    AtchFileEntity procFiles(MultipartHttpServletRequest multiRequest, AtchFileEntity atchFile, List<AtchFileDtlEntity> atchFileList) throws Exception;
}
