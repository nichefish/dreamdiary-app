package io.nicheblog.dreamdiary.global._common.file.service;

import io.nicheblog.dreamdiary.global._common.file.entity.AtchFileEntity;
import io.nicheblog.dreamdiary.global._common.file.mapstruct.AtchFileMapstruct;
import io.nicheblog.dreamdiary.global._common.file.model.AtchFileDto;
import io.nicheblog.dreamdiary.global._common.file.repository.jpa.AtchFileRepository;
import io.nicheblog.dreamdiary.global._common.file.spec.AtchFileSpec;
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
