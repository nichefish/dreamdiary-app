package io.nicheblog.dreamdiary.feature.admin.tmplat.service;

import io.nicheblog.dreamdiary.feature.admin.tmplat.entity.TmplatTxtEntity;
import io.nicheblog.dreamdiary.feature.admin.tmplat.mapstruct.TmplatTxtMapstruct;
import io.nicheblog.dreamdiary.feature.admin.tmplat.model.TmplatTxtDto;
import io.nicheblog.dreamdiary.feature.admin.tmplat.repository.jpa.TmplatTxtRepository;
import io.nicheblog.dreamdiary.feature.admin.tmplat.spec.TmplatTxtSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * TmplatTxtService
 * <pre>
 *  템플릿 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("tmplatTxtService")
@RequiredArgsConstructor
public class TmplatTxtService
        implements BaseDtoWritableService<TmplatTxtDto, TmplatTxtDto, Integer, TmplatTxtEntity> {

    @Getter
    private final TmplatTxtRepository repository;
    @Getter
    private final TmplatTxtSpec spec;
    @Getter
    private final TmplatTxtMapstruct mapstruct = TmplatTxtMapstruct.INSTANCE;

    public TmplatTxtMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public TmplatTxtMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /**
     * 템플릿 정의 코드와 카테고리 코드에 따라 기본 템플릿 텍스트를 조회합니다.
     *
     * @param tmplatDefCd 템플릿 정의 코드
     * @param ctgrCd 카테고리 코드
     * @return {@link TmplatTxtDto} -- 템플릿 텍스트 정보
     */
    public TmplatTxtDto getTmplatTxtByTmplatDef(
            final String tmplatDefCd,
            final String ctgrCd
    ) throws Exception {
        final Optional<TmplatTxtEntity> entityWrapper = repository.findByTmplatDefCdAndCtgrCdAndDefaultYn(tmplatDefCd, ctgrCd, "Y");
        if (entityWrapper.isEmpty()) return null;
        return mapstruct.toDto(entityWrapper.get());
    }

}
