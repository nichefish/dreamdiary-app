package io.nicheblog.dreamdiary.global._common.file.service.impl;

import io.nicheblog.dreamdiary.global._common.file.mapstruct.AtchFileMapstruct;
import io.nicheblog.dreamdiary.global._common.file.repository.jpa.AtchFileRepository;
import io.nicheblog.dreamdiary.global._common.file.service.AtchFileService;
import io.nicheblog.dreamdiary.global._common.file.spec.AtchFileSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
public class AtchFileServiceImpl
        implements AtchFileService {

    @Getter
    private final AtchFileRepository repository;
    @Getter
    private final AtchFileSpec spec;
    @Getter
    private final AtchFileMapstruct mapstruct = AtchFileMapstruct.INSTANCE;
}
