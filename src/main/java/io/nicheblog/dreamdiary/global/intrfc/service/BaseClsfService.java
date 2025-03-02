package io.nicheblog.dreamdiary.global.intrfc.service;

import io.nicheblog.dreamdiary.extension.clsf.managt.entity.embed.ManagtEmbed;
import io.nicheblog.dreamdiary.extension.clsf.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.extension.clsf.managt.model.cmpstn.ManagtCmpstnModule;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseClsfDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * BaseClsfService
 * <pre>
 *  (공통/상속) 일반게시물 CRUD 공통 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseClsfService<Dto extends BaseClsfDto & Identifiable<Key>, ListDto extends BaseClsfDto, Key extends Serializable, Entity extends BaseClsfEntity, Repository extends BaseStreamRepository<Entity, Key>, Spec extends BaseSpec<Entity>, Mapstruct extends BaseClsfMapstruct<Dto, ListDto, Entity>>
        extends BaseMultiCrudService<Dto, ListDto, Key, Entity, Repository, Spec, Mapstruct> {

    /**
     * default: 게시물 등록 (dto level)
     *
     * @param registDto 등록할 Dto 객체
     * @return {@link Dto} -- 등록 결과를 Dto로 변환한 객체
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    default ServiceResponse regist(final Dto registDto) throws Exception {
        final ServiceResponse response = new ServiceResponse();

        // optional: 등록 전처리 (dto)
        this.preRegist(registDto);

        // Dto -> Entity 변환
        final Mapstruct mapstruct = this.getMapstruct();
        final Entity registEntity = mapstruct.toEntity(registDto);

        // optional: 등록 전처리 (entity)
        this.preRegist(registEntity);

        // managt 처리
        if (registDto instanceof ManagtCmpstnModule && registEntity instanceof ManagtEmbedModule) {
            ((ManagtEmbedModule) registEntity).setManagt(new ManagtEmbed(true));
        }

        // insert
        final Entity updatedEntity = this.updt(registEntity);

        final Dto updatedDto = mapstruct.toDto(updatedEntity);

        if (registDto instanceof TagCmpstnModule && updatedDto instanceof TagCmpstnModule) {
            // 후처리를 위해 화면에서 넘어온 tagify 문자열 전달
            ((TagCmpstnModule) updatedDto).setTagFrom((TagCmpstnModule) registDto);
        }

        // optional: 등록 후처리 (dto)
        this.postRegist(updatedDto);

        response.setRslt(updatedDto.getKey() != null);
        response.setRsltObj(updatedDto);
        return response;
    }

    /**
     * default: 게시물 수정 (dto level)
     *
     * @param modifyDto 수정할 Dto 객체
     * @return {@link Dto} -- 수정 결과를 Dto로 변환한 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    default ServiceResponse modify(final Dto modifyDto) throws Exception {
        final ServiceResponse response = new ServiceResponse();

        // optional: 수정 전처리 (dto)
        this.preModify(modifyDto);

        // Entity 레벨 조회
        final Entity modifyEntity = this.getDtlEntity(modifyDto);

        // optional: 수정 전처리 (entity, 기존 데이터 처리 관련)
        this.preModify(modifyEntity);

        final Mapstruct mapstruct = this.getMapstruct();
        mapstruct.updateFromDto(modifyDto, modifyEntity);

        // optional: 수정 전처리 (entity)
        this.preModify(modifyEntity);

        // managt 처리
        if (this.isManagtModule(modifyDto, modifyEntity)) {
            // (수정시) 조치일자 변경하지 않음 처리
            final boolean isManagtDtNull = ((ManagtEmbedModule) modifyEntity).getManagt() == null || ((ManagtEmbedModule) modifyEntity).getManagt().getManagtDt() == null;
            final boolean updtManagtDt = isManagtDtNull || "Y".equals(((ManagtCmpstnModule) modifyDto).getManagt().getManagtDtUpdtYn());
            ((ManagtEmbedModule) modifyEntity).setManagt(new ManagtEmbed(updtManagtDt));
        }

        // update
        final Repository repository = this.getRepository();
        final Entity updatedEntity = repository.saveAndFlush(modifyEntity);

        final Dto updatedDto = mapstruct.toDto(updatedEntity);

        if (modifyDto instanceof TagCmpstnModule && updatedDto instanceof TagCmpstnModule) {
            // 후처리를 위해 화면에서 넘어온 tagify 문자열 전달
            ((TagCmpstnModule) updatedDto).setTagFrom((TagCmpstnModule) modifyDto);
        }

        // optional: 수정 후처리 (dto)
        this.postModify(updatedDto);

        response.setRslt(updatedDto.getKey() != null);
        response.setRsltObj(updatedDto);
        return response;
    }

    /**
     * Dto와 엔티티가 각각 ManagtCmpstnModule 및 ManagtEmbedModule의 인스턴스인지 확인합니다.
     *
     * @param dto 확인할 Dto 객체
     * @param entity 확인할 엔티티 객체
     * @return {@link Boolean} -- 두 객체가 해당 모듈의 인스턴스일 경우 true, 그렇지 않으면 false
     */
    default boolean isManagtModule(final Dto dto, final Entity entity) {
        return dto instanceof ManagtCmpstnModule && entity instanceof ManagtEmbedModule;
    }

    /**
     * Dto와 엔티티가 각각 TagCmpstnModule 및 TagEmbedModule의 인스턴스인지 확인합니다.
     *
     * @param dto 확인할 Dto 객체
     * @return {@link Boolean} -- 두 객체가 해당 모듈의 인스턴스일 경우 true, 그렇지 않으면 false
     */
    default boolean isTagModule(final Dto dto) {
        return dto instanceof TagCmpstnModule;
    }
}
