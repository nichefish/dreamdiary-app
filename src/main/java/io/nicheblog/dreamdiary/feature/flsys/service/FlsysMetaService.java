package io.nicheblog.dreamdiary.feature.flsys.service;

import io.nicheblog.dreamdiary.feature.flsys.entity.FlsysMetaEntity;
import io.nicheblog.dreamdiary.feature.flsys.mapstruct.FlsysMetaMapstruct;
import io.nicheblog.dreamdiary.feature.flsys.model.FlsysMetaDto;
import io.nicheblog.dreamdiary.feature.flsys.repository.jpa.FlsysMetaRepository;
import io.nicheblog.dreamdiary.feature.flsys.spec.FlsysMetaSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * FlsysMetaService
 * <pre>
 *  파일시스템 메타 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class FlsysMetaService
        implements BaseDtoWritableService<FlsysMetaDto, FlsysMetaDto, Integer, FlsysMetaEntity> {

    @Getter
    private final FlsysMetaRepository repository;
    @Getter
    private final FlsysMetaSpec spec;
    @Getter
    private final FlsysMetaMapstruct mapstruct = FlsysMetaMapstruct.INSTANCE;

    public FlsysMetaMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public FlsysMetaMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }
}

