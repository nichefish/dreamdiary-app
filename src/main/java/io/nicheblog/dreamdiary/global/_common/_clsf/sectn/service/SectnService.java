package io.nicheblog.dreamdiary.global._common._clsf.sectn.service;

import io.nicheblog.dreamdiary.global._common._clsf.sectn.entity.SectnEntity;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.mapstruct.SectnMapstruct;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.model.SectnDto;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.repository.jpa.SectnRepository;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.spec.SectnSpec;
import io.nicheblog.dreamdiary.global._common._clsf.state.service.BaseStateService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseMultiCrudService;

/**
 * SectnService
 * <pre>
 *  단락 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface SectnService
        extends BaseMultiCrudService<SectnDto, SectnDto, Integer, SectnEntity, SectnRepository, SectnSpec, SectnMapstruct>,
                BaseStateService<SectnDto, SectnDto, Integer, SectnEntity, SectnRepository, SectnSpec, SectnMapstruct> {

    //
}