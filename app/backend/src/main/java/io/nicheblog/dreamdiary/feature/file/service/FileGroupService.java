package io.nicheblog.dreamdiary.feature.file.service;

import io.nicheblog.dreamdiary.feature.file.entity.FileGroupEntity;
import io.nicheblog.dreamdiary.feature.file.entity.FileRecordEntity;
import io.nicheblog.dreamdiary.feature.file.jpa.FileGroupRepository;
import io.nicheblog.dreamdiary.feature.file.mapstruct.FileGroupMapstruct;
import io.nicheblog.dreamdiary.feature.file.model.FileGroupDto;
import io.nicheblog.dreamdiary.feature.file.spec.FileGroupSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;

/**
 * FileGroupService
 * <pre>
 *  공통 > 파일 처리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class FileGroupService
        implements BaseDtoWritableService<FileGroupDto, FileGroupDto, Integer, FileGroupEntity> {

    @Getter
    private final FileGroupRepository repository;
    @Getter
    private final FileGroupSpec spec;
    @Getter
    private final FileGroupMapstruct mapstruct = FileGroupMapstruct.INSTANCE;

    public FileGroupMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public FileGroupMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final FileRecordService fileRecordService;

    private final ApplicationContext context;
    private FileGroupService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 파일 처리.
     *
     * @param multiRequest 파일 업로드 요청 객체
     * @param fileRecordList 파일 목록
     * @return {@link FileGroupEntity} -- 업로드된 파일 정보
     */
    @Transactional
    public FileGroupEntity procFiles(MultipartHttpServletRequest multiRequest, FileGroupEntity fileGroup, List<FileRecordEntity> fileRecordList) throws Exception {
        fileRecordService.addFiles(multiRequest, fileRecordList);
        fileGroup.cascade();
        return this.getSelf().updt(fileGroup);
    }
}

