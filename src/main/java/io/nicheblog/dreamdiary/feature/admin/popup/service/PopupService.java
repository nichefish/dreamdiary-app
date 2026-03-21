package io.nicheblog.dreamdiary.feature.admin.popup.service;

import io.nicheblog.dreamdiary.feature.admin.popup.entity.PopupEntity;
import io.nicheblog.dreamdiary.feature.admin.popup.mapstruct.PopupMapstruct;
import io.nicheblog.dreamdiary.feature.admin.popup.model.PopupDto;
import io.nicheblog.dreamdiary.feature.admin.popup.repository.jpa.PopupRepository;
import io.nicheblog.dreamdiary.feature.admin.popup.spec.PopupSpec;
import io.nicheblog.dreamdiary.feature.clsf.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.clsf.shared.service.BaseClsfService;
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
        implements BaseClsfService<PopupDto, PopupDto, Integer, PopupEntity>, BaseMultipartWritableService<PopupDto, PopupDto, Integer, PopupEntity> {

    @Getter
    private final PopupRepository repository;
    @Getter
    private final PopupSpec spec;
    @Getter
    private final PopupMapstruct mapstruct = PopupMapstruct.INSTANCE;

    public PopupMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public PopupMapstruct getWriteMapstruct() {
        return this.mapstruct;
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
