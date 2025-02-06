package io.nicheblog.dreamdiary.global._common.cd.service;

import io.nicheblog.dreamdiary.extension.state.service.BaseStateService;
import io.nicheblog.dreamdiary.global._common.cd.entity.ClCdEntity;
import io.nicheblog.dreamdiary.global._common.cd.mapstruct.ClCdMapstruct;
import io.nicheblog.dreamdiary.global._common.cd.model.ClCdDto;
import io.nicheblog.dreamdiary.global._common.cd.repository.jpa.ClCdRepository;
import io.nicheblog.dreamdiary.global._common.cd.spec.ClCdSpec;
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