package io.nicheblog.dreamdiary.extension.cd.service;

import io.nicheblog.dreamdiary.extension.cd.entity.ClCdEntity;
import io.nicheblog.dreamdiary.extension.cd.mapstruct.ClCdMapstruct;
import io.nicheblog.dreamdiary.extension.cd.model.ClCdDto;
import io.nicheblog.dreamdiary.extension.cd.repository.jpa.ClCdRepository;
import io.nicheblog.dreamdiary.extension.cd.spec.ClCdSpec;
import io.nicheblog.dreamdiary.extension.clsf.state.service.BaseStateService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;

/**
 * ClCdService
 * <pre>
 *  분류 코드 관리 서비스 인터페이스.
 *  ※분류 코드(cl_cd) = 상위 분류 코드. 상세 코드(dtl_cd)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
public interface ClCdService
        extends BaseCrudService<ClCdDto, ClCdDto, String, ClCdEntity, ClCdRepository, ClCdSpec, ClCdMapstruct>,
                BaseStateService<ClCdDto, ClCdDto, String, ClCdEntity, ClCdRepository, ClCdSpec, ClCdMapstruct> {

    //
}