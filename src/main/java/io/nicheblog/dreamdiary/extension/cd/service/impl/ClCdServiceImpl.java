package io.nicheblog.dreamdiary.extension.cd.service.impl;

import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.cache.util.RedisUtils;
import io.nicheblog.dreamdiary.extension.cd.mapstruct.ClCdMapstruct;
import io.nicheblog.dreamdiary.extension.cd.model.ClCdDto;
import io.nicheblog.dreamdiary.extension.cd.repository.jpa.ClCdRepository;
import io.nicheblog.dreamdiary.extension.cd.service.ClCdService;
import io.nicheblog.dreamdiary.extension.cd.spec.ClCdSpec;
import io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn.StateCmpstn;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ClCdService
 * <pre>
 *  분류 코드 관리 서비스 모듈.
 *  ※분류 코드(cl_cd) = 상위 분류 코드. 상세 코드(dtl_cd)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Service("clCdService")
@RequiredArgsConstructor
public class ClCdServiceImpl
        implements ClCdService {

    @Getter
    private final ClCdRepository repository;
    @Getter
    private final ClCdSpec spec;
    @Getter
    private final ClCdMapstruct mapstruct = ClCdMapstruct.INSTANCE;

    /**
     * 등록 전처리. (override)
     *
     * @param dto 등록할 객체
     */
    @Override
    public void preRegist(final ClCdDto dto) {
        if (dto.getState() == null) dto.setState(new StateCmpstn());
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postRegist(final ClCdDto updatedDto) throws Exception {
        this.evictCache(updatedDto);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final ClCdDto updatedDto) throws Exception {
        this.evictCache(updatedDto);
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postDelete(final ClCdDto deletedDto) throws Exception {
        this.evictCache(deletedDto);
    }

    /**
     * 관련 캐시 삭제.
     *
     * @param dto 캐시 처리할 엔티티
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public void evictCache(final ClCdDto dto) throws Exception {
        RedisUtils.deleteData("cdEntityListByClCd::clCd:" + dto.getClCd());
        RedisUtils.deleteData("cdDtoListByClCd::clCd:" + dto.getClCd());
        // 연관된 모든 엔티티의 캐시 클리어
        EhCacheUtils.clearL2Cache();
    }
}