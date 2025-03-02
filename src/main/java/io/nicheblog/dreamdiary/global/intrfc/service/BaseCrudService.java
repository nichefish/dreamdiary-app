package io.nicheblog.dreamdiary.global.intrfc.service;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * BaseCrudService
 * <pre>
 *  (공통/상속) CRUD 공통 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseCrudService<Dto extends BaseCrudDto & Identifiable<Key>, ListDto extends BaseCrudDto, Key extends Serializable, Entity extends BaseCrudEntity, Repository extends BaseStreamRepository<Entity, Key>, Spec extends BaseSpec<Entity>, Mapstruct extends BaseCrudMapstruct<Dto, ListDto, Entity>>
        extends BaseReadonlyService<Dto, ListDto, Key, Entity, Repository, Spec, Mapstruct> {

    /**
     * default: 등록 전처리 (dto level)
     *
     * @param registDto 등록할 Dto 객체
     * @throws Exception 전처리 중 발생할 수 있는 예외
     */
    default void preRegist(final Dto registDto) throws Exception {
        // 등록 전처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 등록 전처리 (entity level)
     *
     * @param registEntity 등록할 Entity 객체
     * @throws Exception 전처리 중 발생할 수 있는 예외
     */
    default void preRegist(final Entity registEntity) throws Exception {
        // 등록 전처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 등록 후처리 (dto level)
     *
     * @param updatedDto - 등록된 Dto 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    default void postRegist(final Dto updatedDto) throws Exception {
        // 등록 후처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 등록 (dto level)
     *
     * @param registDto 등록할 Dto 객체
     * @return {@link Dto} -- 등록 결과를 Dto로 변환한 객체
     * @throws Exception 등록 중 발생할 수 있는 예외
     */
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

        // insert
        final Entity updatedEntity = this.updt(registEntity);

        final Dto updatedDto = mapstruct.toDto(updatedEntity);

        // optional: 등록 후처리 (dto)
        this.postRegist(updatedDto);

        response.setRslt(updatedDto.getKey() != null);
        response.setRsltObj(updatedDto);
        return response;
    }

    /**
     * default: bulk 등록 전처리
     *
     * @param registEntityList - 등록할 엔티티 리스트
     */
    default void preRegistAll(final List<Entity> registEntityList) {
        // 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: bulk 등록 후처리
     *
     * @param updatedEntityList - 등록된 엔티티 리스트
     */
    default void postRegistAll(final List<Entity> updatedEntityList) {
        // 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: bulk-insert (entity level)
     *
     * @param registEntityList - 등록할 엔티티 리스트
     * @return {@link Boolean} -- 등록 성공시 true
     */
    @Transactional
    default List<Entity> registAll(final List<Entity> registEntityList) throws Exception {
        // optional: 벌크 등록 전처리
        this.preRegistAll(registEntityList);
        
        final Repository repository = this.getRepository();
        final List<Entity> updatedEntityList = repository.saveAllAndFlush(registEntityList);

        // optional: 벌크 등록 후처리
        this.postRegistAll(updatedEntityList);

        return updatedEntityList;
    }

    /**
     * default: 수정 전처리 (dto level)
     *
     * @param modifyDto 수정할 Dto 객체
     * @throws Exception 수정 전처리 중 발생할 수 있는 예외
     */
    default void preModify(final Dto modifyDto) throws Exception {
        // 수정 전처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 수정 전처리 (entity, 기존 데이터 처리 관련)
     *
     * @param modifyEntity 수정 중간처리를 할 엔티티 객체
     */
    default void preModify(final Entity modifyEntity) {
        // 수정 전 상태 저장 (기존 데이터 처리 관련):: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 수정 후처리 (dto level)
     *
     * @param updatedDto 수정된 dto 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    default void postModify(final Dto updatedDto) throws Exception {
        // 수정 후처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 수정 (dto level)
     *
     * @param modifyDto 수정할 Dto 객체
     * @return Dto - 수정된 결과를 Dto로 변환한 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional
    default ServiceResponse modify(final Dto modifyDto) throws Exception {
        final ServiceResponse response = new ServiceResponse();

        // optional: 수정 전처리 (dto)
        this.preModify(modifyDto);

        // Entity 레벨 조회
        final Entity modifyEntity = this.getDtlEntity(modifyDto);

        // optional: 수정 전처리 (entity, 기존 데이터 처리 관련)
        this.preModify(modifyEntity);

        // Entity 레벨 조회
        final Mapstruct mapstruct = this.getMapstruct();
        mapstruct.updateFromDto(modifyDto, modifyEntity);

        // update
        final Repository repository = this.getRepository();
        final Entity updatedEntity = repository.saveAndFlush(modifyEntity);

        final Dto updatedDto = mapstruct.toDto(updatedEntity);

        // optional: 수정 후처리 (dto)
        this.postModify(updatedDto);

        response.setRslt(updatedDto.getKey() != null);
        response.setRsltObj(updatedDto);
        return response;
    }

    /**
     * default: 수정 (entity level)
     *
     * @param modifyEntity 수정할 엔티티 객체
     * @return updatedEntity - 저장 및 새로고침된 엔티티 객체
     */
    @Transactional
    default Entity updt(final Entity modifyEntity) throws Exception {
        final Repository repository = this.getRepository();
        final Entity updatedEntity = repository.saveAndFlush(modifyEntity);
        try {
            repository.refresh(updatedEntity);
        } catch (final EntityNotFoundException ex) {
            ex.getStackTrace();
        }

        return updatedEntity;
    }

    /**
     * default: 삭제 전처리 (dto level)
     *
     * @param deletedDto - 삭제할 dto 객체
     * @throws Exception 삭제 전처리 중 발생할 수 있는 예외
     */
    default void preDelete(final Dto deletedDto) throws Exception {
        // 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 삭제 전처리 (entity level)
     *
     * @param deleteEntity - 삭제할 엔티티 객체
     * @throws Exception 삭제 전처리 중 발생할 수 있는 예외
     */
    default void preDelete(final Entity deleteEntity) throws Exception {
        // 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 삭제 후처리 (dto level)
     *
     * @param deletedDto 삭제된 Dto 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    default void postDelete(final Dto deletedDto) throws Exception {
        // 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 삭제 (Dto 사용)
     *
     * @param deleteDto 삭제할 Dto 객체
     * @return Boolean 삭제 성공시 true, 실패 시 false
     * @throws Exception 삭제 중 발생할 수 있는 예외
     */
    @Transactional
    default ServiceResponse delete(final Dto deleteDto) throws Exception {
        return this.delete(deleteDto.getKey());
    }

    /**
     * default: 삭제 (key 사용)
     *
     * @param key 삭제할 엔티티의 키
     * @return Boolean 삭제 성공시 true, 실패 시 false
     * @throws Exception 삭제 중 발생할 수 있는 예외
     */
    @Transactional
    default ServiceResponse delete(final Key key) throws Exception {
        final ServiceResponse response = new ServiceResponse();

        final Repository repository = this.getRepository();
        final Entity deleteEntity = this.getDtlEntity(key);
        if (deleteEntity == null) throw new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete"));

        // optional: 삭제 전처리 (entity level)
        this.preDelete(deleteEntity);

        final Mapstruct mapstruct = this.getMapstruct();
        final Dto deletedDto = mapstruct.toDto(deleteEntity);

        // optional: 삭제 전처리 (dto level)
        this.preDelete(deletedDto);

        repository.delete(deleteEntity);

        // optional: 삭제 후처리
        this.postDelete(deletedDto);

        response.setRslt(true);
        response.setRsltObj(deletedDto);
        return response;
    }

    /**
     * default: bulk 삭제 전처리
     *
     * @param deleteEntityList 삭제된 엔티티 리스트
     */
    default void preDeleteAll(final List<Entity> deleteEntityList) {
        // 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: bulk 삭제 후처리
     *
     * @param deletedEntityList 삭제된 엔티티 리스트
     */
    default void postDeleteAll(final List<Entity> deletedEntityList) {
        // 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: bulk-delete (entity level)
     *
     * @param searchParamMap 엔티티 리스트를 조회할 검색 파라미터 맵
     * @return Boolean 삭제 성공시 true
     * @throws Exception 삭제 중 발생할 수 있는 예외
     */
    @Transactional
    default boolean deleteAll(final Map<String, Object> searchParamMap) throws Exception {
        final List<Entity> deleteEntityList = this.getListEntity(searchParamMap);

        return this.deleteAll(deleteEntityList);
    }

    /**
     * default: bulk-delete (entity level)
     *
     * @param deleteEntityList 삭제할 엔티티 리스트
     * @return Boolean - 삭제 성공시 true
     */
    @Transactional
    default boolean deleteAll(final List<Entity> deleteEntityList) throws Exception {
        // optional: bulk 삭제 전처리 (emtity)
        this.preDeleteAll(deleteEntityList);

        final Repository repository = this.getRepository();
        repository.deleteAll(deleteEntityList);

        // optional: bulk 삭제 후처리 (emtity)
        this.postDeleteAll(deleteEntityList);

        return true;
    }
}
