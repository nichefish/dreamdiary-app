package io.nicheblog.dreamdiary.domain.admin.popup.service;

import io.nicheblog.dreamdiary.domain.admin.popup.entity.PopupEntity;
import io.nicheblog.dreamdiary.domain.admin.popup.mapstruct.PopupMapstruct;
import io.nicheblog.dreamdiary.domain.admin.popup.model.PopupDto;
import io.nicheblog.dreamdiary.domain.admin.popup.repository.jpa.PopupRepository;
import io.nicheblog.dreamdiary.domain.admin.popup.spec.PopupSpec;
import io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseMultiCrudService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * PopupService
 * <pre>
 *  팝업 정보 관리 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("popupService")
@RequiredArgsConstructor
public class PopupService
        implements BaseMultiCrudService<PopupDto, PopupDto, Integer, PopupEntity, PopupRepository, PopupSpec, PopupMapstruct> {

    @Getter
    private final PopupRepository repository;
    @Getter
    private final PopupSpec spec;
    @Getter
    private final PopupMapstruct mapstruct = PopupMapstruct.INSTANCE;

    /**
     * 등록 전처리. (override)
     *
     * @param dto 등록할 객체
     */
    @Override
    public void preRegist(final PopupDto dto) {
        if (dto.getState() == null) dto.setState(new StateCmpstn());
    }

    /**
     * 활성화 중인 팝업 모음
     */
    // public Page<PopupDto> getActivePopupList() throws Exception {
    //     Page<PopupEntity> entityPage = popupRepository.findAll(popupSpec.getActives(), Pageable.unpaged());

    //     // Page<Entity> -> Page<Dto>
    //     return this.pageEntityToDto(entityPage);
    // }
}
