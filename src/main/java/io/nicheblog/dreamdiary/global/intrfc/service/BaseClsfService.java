package io.nicheblog.dreamdiary.global.intrfc.service;

import io.nicheblog.dreamdiary.extension.clsf.managt.entity.embed.ManagtEmbed;
import io.nicheblog.dreamdiary.extension.clsf.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.extension.clsf.managt.model.cmpstn.ManagtCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseClsfDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
     * @param registDto 등록할 DTO 객체
     * @return {@link Dto} -- 등록 결과를 DTO로 변환한 객체
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    default Dto regist(final Dto registDto) throws Exception {
        // 등록 전처리
        this.preRegist(registDto);

        // Dto -> Entity 변환
        final Mapstruct mapstruct = this.getMapstruct();
        final Entity registEntity = mapstruct.toEntity(registDto);

        // 등록 중간처리
        this.midRegist(registEntity);

        // managt 처리
        if (registDto instanceof ManagtCmpstnModule && registEntity instanceof ManagtEmbedModule) {
            ((ManagtEmbedModule) registEntity).setManagt(new ManagtEmbed(true));
        }

        // insert
        final Entity updatedEntity = this.updt(registEntity);

        // 등록 후처리
        this.postRegist(updatedEntity);

        // 연관 캐시 삭제
        Map<EntityKey, ?> entities = new HashMap<>() {{
            put(EntityKey.REGIST_ENTITY, registEntity);
            put(EntityKey.UPDATED_ENTITY, updatedEntity);
        }};
        this.evictRelCaches(entities);

        return mapstruct.toDto(updatedEntity);
    }

    /**
     * default: 게시물 수정 (dto level)
     *
     * @param modifyDto 수정할 DTO 객체
     * @return {@link Dto} -- 수정 결과를 DTO로 변환한 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional
    default Dto modify(final Dto modifyDto) throws Exception {
        // 수정 전처리
        this.preModify(modifyDto);

        // Entity 레벨 조회
        final Mapstruct mapstruct = this.getMapstruct();
        final Entity modifyEntity = this.getDtlEntity(modifyDto);
        mapstruct.updateFromDto(modifyDto, modifyEntity);

        // 수정 중간처리
        this.midModify(modifyEntity);

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

        // 수정 후처리
        this.postModify(updatedEntity);

        // 연관 캐시 삭제
        Map<EntityKey, ?> entities = new HashMap<>() {{
            put(EntityKey.REGIST_ENTITY, modifyEntity);
            put(EntityKey.UPDATED_ENTITY, updatedEntity);
        }};
        this.evictRelCaches(entities);

        return mapstruct.toDto(updatedEntity);
    }

    /**
     * DTO와 엔티티가 각각 ManagtCmpstnModule 및 ManagtEmbedModule의 인스턴스인지 확인합니다.
     *
     * @param dto 확인할 DTO 객체
     * @param entity 확인할 엔티티 객체
     * @return {@link Boolean} -- 두 객체가 해당 모듈의 인스턴스일 경우 true, 그렇지 않으면 false
     */
    default boolean isManagtModule(final Dto dto, final Entity entity) {
        return dto instanceof ManagtCmpstnModule && entity instanceof ManagtEmbedModule;
    }
}
