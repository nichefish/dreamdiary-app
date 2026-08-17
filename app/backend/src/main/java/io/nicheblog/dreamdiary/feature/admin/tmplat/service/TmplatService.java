package io.nicheblog.dreamdiary.feature.admin.tmplat.service;

import io.nicheblog.dreamdiary.feature.admin.tmplat.entity.TmplatEntity;
import io.nicheblog.dreamdiary.feature.admin.tmplat.mapstruct.TmplatMapstruct;
import io.nicheblog.dreamdiary.feature.admin.tmplat.model.TmplatDto;
import io.nicheblog.dreamdiary.feature.admin.tmplat.repository.jpa.TmplatRepository;
import io.nicheblog.dreamdiary.feature.admin.tmplat.spec.TmplatSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TmplatService
 * <pre>
 *  템플릿 정보 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class TmplatService
        implements BaseDtoReadableService<TmplatDto, Integer, TmplatEntity>,
                   BaseDtoWritableService<TmplatDto, TmplatDto, Integer, TmplatEntity> {

    @Getter
    private final TmplatRepository repository;
    @Getter
    private final TmplatSpec spec;
    @Getter
    private final TmplatMapstruct mapstruct = TmplatMapstruct.INSTANCE;

    public TmplatMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public TmplatMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }
}