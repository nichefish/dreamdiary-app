package io.nicheblog.dreamdiary.global.intrfc.service;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * BaseEntityWritableService
 * <pre>
 *  (공통/상속) 쓰기 (entity level) 공통 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseEntityWritableService<Key extends Serializable, Entity extends BaseCrudEntity>
        extends BaseEntityReadableService<Key, Entity> {

    /**
     * default: 등록 전처리 (entity level)
     *
     * @param registEntity 등록할 Entity 객체
     */
    default void preRegist(final Entity registEntity) throws Exception {
        // 등록 전처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 등록 (entity level)
     *
     * @param entity 등록할 Entity 객체
     */
    @Transactional
    default Entity regist(final Entity entity) throws Exception {
        // optional: 등록 전처리 (dto)
        this.preRegist(entity);

        // insert
        final Entity updatedEntity = this.updt(entity);

        // optional: 등록 후처리 (dto)
        this.postRegist(updatedEntity);

        return updatedEntity;
    }

    /**
     * default: 등록 후처리 (entity level)
     *
     * @param updatedEntity - 등록된 Dto 객체
     */
    default void postRegist(final Entity updatedEntity) throws Exception {
        // 등록 후처리:: 기본 공백, 필요시 각 함수에서 Override
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
        
        final List<Entity> updatedEntityList = getRepository().saveAllAndFlush(registEntityList);

        // optional: 벌크 등록 후처리
        this.postRegistAll(updatedEntityList);

        return updatedEntityList;
    }

    /**
     * default: 수정 전처리 (entity, 기존 데이터 처리 관련)
     *
     * @param modifyEntity 수정 중간처리를 할 엔티티 객체
     */
    default void preUpdt(final Entity modifyEntity) {
        // 수정 전 상태 저장 (기존 데이터 처리 관련):: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 수정 (entity level)
     *
     * @param entity 수정할 엔티티 객체
     * @return updatedEntity - 저장 및 새로고침된 엔티티 객체
     */
    @Transactional
    default Entity updt(final Entity entity) throws Exception {
        // optional: 수정 전처리 (entity)
        this.preUpdt(entity);

        final Entity updatedEntity = getRepository().saveAndFlush(entity);
        try {
            getRepository().refresh(updatedEntity);
        } catch (final EntityNotFoundException ex) {
            ex.printStackTrace();
        }

        // optional: 수정 후처리 (entity)
        this.postUpdt(entity);

        return updatedEntity;
    }
    /**
     * default: 수정 후처리 (entity level)
     *
     * @param modifiedEntity 수정된 dto 객체
     */
    default void postUpdt(final Entity modifiedEntity) throws Exception {
        // 수정 후처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 삭제 전처리 (entity level)
     *
     * @param deleteEntity - 삭제할 엔티티 객체
     */
    default void preRemove(final Entity deleteEntity) throws Exception {
        // 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * default: 삭제 (key 사용)
     *
     * @param key 삭제할 엔티티의 키
     */
    @Transactional
    default void remove(final Key key) throws Exception {
        final Entity entity = this.getDtlEntity(key);
        if (entity == null) throw new EntityNotFoundException("exception.EntityNotFoundException.to-delete");

        this.remove(entity);
    }

    /**
     * default: 삭제
     *
     * @param entity 삭제할 엔티티
     */
    default void remove(final Entity entity) throws Exception {
        // optional: 삭제 전처리 (entity level)
        this.preRemove(entity);

        getRepository().delete(entity);
        getRepository().flush();

        // optional: 삭제 후처리
        this.postRemove(entity);
    }

    /**
     * default: 삭제 후처리 (entity level)
     *
     * @param deletedEntity 삭제된 Entity 객체
     */
    default void postRemove(final Entity deletedEntity) throws Exception {
        // 기본 공백, 필요시 각 함수에서 Override
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
     * default: bulk-delete (entity level)
     *
     * @param searchParamMap 엔티티 리스트를 조회할 검색 파라미터 맵
     * @return Boolean 삭제 성공시 true
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

        getRepository().deleteAll(deleteEntityList);

        // optional: bulk 삭제 후처리 (emtity)
        this.postDeleteAll(deleteEntityList);

        return true;
    }

    /**
     * default: bulk 삭제 후처리
     *
     * @param deletedEntityList 삭제된 엔티티 리스트
     */
    @SuppressWarnings("unused")
    default void postDeleteAll(final List<Entity> deletedEntityList) {
        // 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * 사용 여부 세팅
     * @param key Key
     * @return ServiceResponse
     */
    default ServiceResponse setUse(final Key key, final String yn) throws Exception {
        final Entity existingEntity = this.getDtlEntity(key);
        if (!(existingEntity instanceof Usable usable)) {
            return ServiceResponse.builder()
                    .rslt(false)
                    .build();
        }

        usable.setUseYn(yn);
        final Entity updatedEntity = getRepository().save(existingEntity);

        this.postSetUse(updatedEntity);

        return ServiceResponse.builder()
            .rslt(true)
            .rsltSts("Y".equals(yn) ? "ON" : "OFF")
            .build();
    }

    /**
     * default: 상태 변경 후처리 (entity level)
     *
     * @param updatedEntity 상태 변경된 Entity 객체
     */
    default void postSetUse(Entity updatedEntity) {
        //
    }
}
