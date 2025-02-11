package io.nicheblog.dreamdiary.extension.clsf.tag.service;

import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagPropertyEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.mapstruct.TagPropertyMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagPropertyDto;
import io.nicheblog.dreamdiary.extension.clsf.tag.repository.jpa.TagPropertyRepository;
import io.nicheblog.dreamdiary.extension.clsf.tag.spec.TagPropertySpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * TagPropertyService
 * <pre>
 *  태그 속성 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("tagPropertyService")
@RequiredArgsConstructor
@Log4j2
public class TagPropertyService
        implements BaseCrudService<TagPropertyDto, TagPropertyDto, Integer, TagPropertyEntity, TagPropertyRepository, TagPropertySpec, TagPropertyMapstruct> {

    @Getter
    private final TagPropertyRepository repository;
    @Getter
    private final TagPropertySpec spec;
    @Getter
    private final TagPropertyMapstruct mapstruct = TagPropertyMapstruct.INSTANCE;
}
