package io.nicheblog.dreamdiary.extension.file.service;

import io.nicheblog.dreamdiary.extension.file.entity.AtchFileEntity;
import io.nicheblog.dreamdiary.extension.file.mapstruct.AtchFileMapstruct;
import io.nicheblog.dreamdiary.extension.file.model.AtchFileDto;
import io.nicheblog.dreamdiary.extension.file.repository.jpa.AtchFileRepository;
import io.nicheblog.dreamdiary.extension.file.spec.AtchFileSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;

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
    //
}
