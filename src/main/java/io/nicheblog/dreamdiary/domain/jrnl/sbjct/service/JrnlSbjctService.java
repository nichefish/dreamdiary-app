package io.nicheblog.dreamdiary.domain.jrnl.sbjct.service;

import io.nicheblog.dreamdiary.domain.jrnl.sbjct.entity.JrnlSbjctEntity;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.mapstruct.JrnlSbjctMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.model.JrnlSbjctDto;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.repository.jpa.JrnlSbjctRepository;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.spec.JrnlSbjctSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BasePostService;

/**
 * JrnlSbjctService
 * <pre>
 *  저널 주제 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface JrnlSbjctService
        extends BasePostService<JrnlSbjctDto.DTL, JrnlSbjctDto.LIST, Integer, JrnlSbjctEntity, JrnlSbjctRepository, JrnlSbjctSpec, JrnlSbjctMapstruct> {

    //
}