package io.nicheblog.dreamdiary.extension.cd.service;

import io.nicheblog.dreamdiary.extension.cache.util.RedisUtils;
import io.nicheblog.dreamdiary.extension.cd.entity.ClCdEntity;
import io.nicheblog.dreamdiary.extension.cd.mapstruct.ClCdMapstruct;
import io.nicheblog.dreamdiary.extension.cd.model.ClCdDto;
import io.nicheblog.dreamdiary.extension.cd.model.ClCdPatchDto;
import io.nicheblog.dreamdiary.extension.cd.repository.jpa.ClCdRepository;
import io.nicheblog.dreamdiary.extension.cd.spec.ClCdSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
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
public class ClCdService
        implements BaseDtoReadableService<ClCdDto, String, ClCdEntity>,
                   BaseDtoWritableService<ClCdDto, ClCdDto, String, ClCdEntity> {

    @Getter
    private final ClCdRepository repository;
    @Getter
    private final ClCdSpec spec;
    @Getter
    private final ClCdMapstruct mapstruct = ClCdMapstruct.INSTANCE;

    public ClCdMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public ClCdMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationContext context;
    private ClCdService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final ClCdDto updatedDto) throws Exception {
        this.evictCache(updatedDto);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final ClCdDto postDto, final ClCdDto updatedDto) throws Exception {
        this.evictCache(updatedDto);
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final ClCdDto deletedDto) throws Exception {
        this.evictCache(deletedDto);
    }

    /**
     * 관련 캐시 삭제.
     *
     * @param dto 캐시 처리할 엔티티
     */
    public void evictCache(final ClCdDto dto) throws Exception {
        RedisUtils.deleteData("cdEntityListByClCd::clCd:" + dto.getClCd());
        RedisUtils.deleteData("cdDtoListByClCd::clCd:" + dto.getClCd());
    }

    /**
     * 상태를 설정한다.
     *
     * @param clCd 대상 게시물 PK
     * @param patchDto 상태 Dto
     * @return collapsedYn 반영 성공 여부를 담은 ServiceResponse
     */
    public ServiceResponse patch(final String clCd, final ClCdPatchDto patchDto) throws Exception {
        if (StringUtils.isEmpty(patchDto.getUseYn())) {
            return ServiceResponse.builder()
                    .rslt(false)
                    .message("변경할 항목이 없습니다.")
                    .build();
        }

        return this.getSelf().setUse(clCd, patchDto.getUseYn());
    }

    /**
     * 변경 후처리. (override)
     *
     * @param updateEntity - 삭제된 객체
     */
    @Override
    public void postSetUse(final ClCdEntity updateEntity) {
        RedisUtils.deleteData("cdEntityListByClCd::clCd:" + updateEntity.getClCd());
        RedisUtils.deleteData("cdDtoListByClCd::clCd:" + updateEntity.getClCd());
    }
}