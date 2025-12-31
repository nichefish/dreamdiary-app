package io.nicheblog.dreamdiary.domain.admin.tmplat.service;

import io.nicheblog.dreamdiary.domain.admin.tmplat.entity.TmplatDefEntity;
import io.nicheblog.dreamdiary.domain.admin.tmplat.mapstruct.TmplatDefMapstruct;
import io.nicheblog.dreamdiary.domain.admin.tmplat.model.TmplatDefDto;
import io.nicheblog.dreamdiary.domain.admin.tmplat.repository.jpa.TmplatDefRepository;
import io.nicheblog.dreamdiary.domain.admin.tmplat.spec.TmplatDefSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TmplatDefService
 * <pre>
 *  템플릿 정의 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("tmplatDefService")
@RequiredArgsConstructor
public class TmplatDefService
        implements BaseCrudService<TmplatDefDto, TmplatDefDto, Integer, TmplatDefEntity> {

    @Getter
    private final TmplatDefRepository repository;
    @Getter
    private final TmplatDefSpec spec;
    @Getter
    private final TmplatDefMapstruct mapstruct = TmplatDefMapstruct.INSTANCE;

    public TmplatDefMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public TmplatDefMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }
}