package io.nicheblog.dreamdiary.global.intrfc.service;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.Serializable;

/**
 * BaseWritableService
 * <pre>
 *  (공통/상속) 쓰기 (dto level) 공통 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseDtoWritableService<PostDto extends BaseCrudDto & Identifiable<Key>, Dto extends BaseCrudDto & Identifiable<Key>, Key extends Serializable, Entity extends BaseCrudEntity>
        extends BaseDtoReadableService<Dto, Key, Entity>, BaseEntityWritableService<Key, Entity> {

    // Resource : mapstruct
    BaseWriteMapstruct<PostDto, Entity> getWriteMapstruct();

    /**
     * default: 등록 전처리 (dto level)
     *
     * @param registDto 등록할 Dto 객체
     */
    default void preRegist(final PostDto registDto) throws Exception {
        // 등록 전처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 등록 후처리 (dto level)
     *
     * @param updatedDto - 등록된 Dto 객체
     */
    default void postRegist(final Dto updatedDto) throws Exception {
        // 등록 후처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 등록 (dto level)
     *
     * @param registDto 등록할 Dto 객체
     * @return {@link PostDto} -- 등록 결과를 Dto로 변환한 객체
     */
    @Transactional
    default ServiceResponse regist(final PostDto registDto) throws Exception {
        final ServiceResponse response = new ServiceResponse();

        // optional: 등록 전처리 (dto)
        this.preRegist(registDto);

        // Dto -> Entity 변환
        final Entity registEntity = getWriteMapstruct().toEntity(registDto);

        // optional: 등록 전처리 (entity)
        this.preRegist(registEntity);

        // insert
        final Entity updatedEntity = this.updt(registEntity);

        final Dto updatedDto = getReadMapstruct().toDto(updatedEntity);

        // optional: 등록 후처리 (dto)
        this.postRegist(updatedDto);

        response.setRslt(updatedDto.getKey() != null);
        response.setRsltObj(updatedDto);
        return response;
    }

    /**
     * default: 수정 전처리 (dto)
     *
     * @param postDto 수정할 Dto 객체
     */
    default void preModify(final PostDto postDto) throws Exception {
        // 수정 전처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 수정 전처리 (dto, entity)
     *
     * @param postDto 수정할 Dto 객체
     * @param modifyEntity 수정 중간처리를 할 엔티티 객체
     */
    default void preModify(final PostDto postDto, final Entity modifyEntity) throws Exception {
        // 수정 전처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 수정 후처리 (dto level)
     *
     * @param updatedDto 수정된 dto 객체
     */
    default void postModify(final PostDto postDto, final Dto updatedDto) throws Exception {
        // 수정 후처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 수정 (dto level)
     *
     * @param postDto 수정할 Dto 객체
     * @return Dto - 수정된 결과를 Dto로 변환한 객체
     */
    @Transactional
    default ServiceResponse modify(final PostDto postDto) throws Exception {
        final ServiceResponse response = new ServiceResponse();

        // Entity 레벨 조회
        final Entity modifyEntity = this.getDtlEntity(postDto.getKey());

        // optional: 수정 전처리 (dto)
        this.preModify(postDto);
        // optional: 수정 전처리 (dto, entity)
        this.preModify(postDto, modifyEntity);

        // Entity 레벨 조회
        getWriteMapstruct().updateFromDto(postDto, modifyEntity);

        // update
        final Entity updatedEntity = getRepository().saveAndFlush(modifyEntity);

        final Dto updatedDto = getReadMapstruct().toDto(updatedEntity);

        // optional: 수정 후처리 (dto)
        this.postModify(postDto, updatedDto);

        response.setRslt(updatedDto.getKey() != null);
        response.setRsltObj(updatedDto);
        return response;
    }

    /**
     * default: 삭제 전처리 (dto level)
     *
     * @param deletedDto - 삭제할 dto 객체
     */
    default void preDelete(final Dto deletedDto) throws Exception {
        // 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 삭제 후처리 (dto level)
     *
     * @param deletedDto 삭제된 Dto 객체
     */
    default void postDelete(final Dto deletedDto) throws Exception {
        // 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 삭제 (Dto 사용)
     *
     * @param deleteDto 삭제할 Dto 객체
     * @return Boolean 삭제 성공시 true, 실패 시 false
     */
    @Transactional
    default ServiceResponse delete(final PostDto deleteDto) throws Exception {
        return this.delete(deleteDto.getKey());
    }

    /**
     * default: 삭제 (key 사용)
     *
     * @param key 삭제할 엔티티의 키
     * @return Boolean 삭제 성공시 true, 실패 시 false
     */
    @Transactional
    default ServiceResponse delete(final Key key) throws Exception {
        final ServiceResponse response = new ServiceResponse();

        final Entity deleteEntity = this.getDtlEntity(key);
        if (deleteEntity == null) throw new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete"));

        final Dto deletedDto = getReadMapstruct().toDto(deleteEntity);

        this.preDelete(deletedDto);

        this.remove(deleteEntity);

        this.postDelete(deletedDto);

        response.setRslt(true);
        response.setRsltObj(deletedDto);
        return response;
    }
}
