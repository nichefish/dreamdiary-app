package io.nicheblog.dreamdiary.feature.admin.code.service;

import io.nicheblog.dreamdiary.feature.admin.code.mapstruct.CodeGroupMapstruct;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupDto;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupPatchDto;
import io.nicheblog.dreamdiary.feature.admin.code.spec.CodeGroupSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoReadableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeGroupEntity;
import io.nicheblog.dreamdiary.infrastructure.code.repository.jpa.CodeGroupRepository;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * CodeGroupService
 * <pre>
 *  분류 코드 관리 서비스 모듈.
 *  ※분류 코드(cl_cd) = 상위 분류 코드. 상세 코드(dtl_cd)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class CodeGroupService
        implements BaseDtoReadableService<CodeGroupDto, Integer, CodeGroupEntity>,
                   BaseDtoWritableService<CodeGroupDto, CodeGroupDto, Integer, CodeGroupEntity> {

    @Getter
    private final CodeGroupRepository repository;
    @Getter
    private final CodeGroupSpec spec;
    @Getter
    private final CodeGroupMapstruct mapstruct = CodeGroupMapstruct.INSTANCE;

    public CodeGroupMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public CodeGroupMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationContext context;
    private CodeGroupService getSelf() {
        return context.getBean(this.getClass());
    }

    private final CodeLookupService codeLookupService;

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final CodeGroupDto updatedDto) throws Exception {
        this.evictCache(updatedDto);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final CodeGroupDto postDto, final CodeGroupDto updatedDto) throws Exception {
        this.evictCache(updatedDto);
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final CodeGroupDto deletedDto) throws Exception {
        this.evictCache(deletedDto);
    }

    /**
     * 관련 캐시 삭제.
     *
     * @param dto 캐시 처리할 엔티티
     */
    public void evictCache(final CodeGroupDto dto) throws Exception {
        codeLookupService.evictClCdCache(dto.getClCd());
    }

    /**
     * 상태를 설정한다.
     *
     * @param id 대상 게시물 PK
     * @param patchDto 상태 Dto
     * @return collapsedYn 반영 성공 여부를 담은 ServiceResponse
     */
    public ServiceResponse patch(final Integer id, final CodeGroupPatchDto patchDto) throws Exception {
        if (StringUtils.isEmpty(patchDto.getUseYn())) {
            return ServiceResponse.builder()
                    .rslt(false)
                    .message("변경할 항목이 없습니다.")
                    .build();
        }

        return this.getSelf().setUse(id, patchDto.getUseYn());
    }

    /**
     * 변경 후처리. (override)
     *
     * @param updateEntity - 삭제된 객체
     */
    @Override
    public void postSetUse(final CodeGroupEntity updateEntity) {
        codeLookupService.evictClCdCache(updateEntity.getClCd());
    }
}
