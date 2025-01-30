package io.nicheblog.dreamdiary.domain.admin.tmplat.service;

import io.nicheblog.dreamdiary.domain.admin.tmplat.entity.TmplatTxtEntity;
import io.nicheblog.dreamdiary.domain.admin.tmplat.mapstruct.TmplatTxtMapstruct;
import io.nicheblog.dreamdiary.domain.admin.tmplat.model.TmplatTxtDto;
import io.nicheblog.dreamdiary.domain.admin.tmplat.repository.jpa.TmplatTxtRepository;
import io.nicheblog.dreamdiary.domain.admin.tmplat.spec.TmplatTxtSpec;
import io.nicheblog.dreamdiary.global._common._clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global._common._clsf.state.service.BaseStateService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
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
        implements BaseCrudService<TmplatTxtDto, TmplatTxtDto, Integer, TmplatTxtEntity, TmplatTxtRepository, TmplatTxtSpec, TmplatTxtMapstruct>,
                   BaseStateService<TmplatTxtDto, TmplatTxtDto, Integer, TmplatTxtEntity, TmplatTxtRepository, TmplatTxtSpec, TmplatTxtMapstruct> {

    @Getter
    private final TmplatTxtRepository repository;
    @Getter
    private final TmplatTxtSpec spec;
    @Getter
    private final TmplatTxtMapstruct mapstruct = TmplatTxtMapstruct.INSTANCE;

    /**
     * 등록 전처리. (override)
     *
     * @param dto 등록할 객체
     */
    @Override
    public void preRegist(final TmplatTxtDto dto) {
        if (dto.getState() == null) dto.setState(new StateCmpstn());
    }

    /**
     * 템플릿 정의 코드와 카테고리 코드에 따라 기본 템플릿 텍스트를 조회합니다.
     *
     * @param tmplatDefCd 템플릿 정의 코드
     * @param ctgrCd 카테고리 코드
     * @return {@link TmplatTxtDto} -- 템플릿 텍스트 정보
     * @throws Exception 조회 중 발생 가능한 예외
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