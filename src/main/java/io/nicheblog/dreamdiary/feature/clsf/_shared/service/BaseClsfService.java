package io.nicheblog.dreamdiary.feature.clsf._shared.service;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.helper.BaseClsfHistoryHelper;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.helper.BaseClsfManagtHelper;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.helper.BaseClsfProcPostProcessor;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryActionModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.Serializable;

/**
 * BaseClsfService
 * <pre>
 *  (공통/상속) 일반 게시물 CRUD 공통 서비스 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface BaseClsfService<PostDto extends BaseClsfDto & Identifiable<Key>, Dto extends BaseClsfDto & Identifiable<Key>, Key extends Serializable, Entity extends BaseClsfEntity>
        extends BaseDtoWritableService<PostDto, Dto, Key, Entity> {

    /**
     * default: 게시물 등록 (dto level)
     *
     * @param registDto 등록할 Dto 객체
     * @return {@link ServiceResponse} -- 등록 결과 객체
     */
    @Override
    @Transactional
    default ServiceResponse regist(final PostDto registDto) throws Exception {
        // optional: 등록 전처리(dto)
        this.preRegist(registDto);

        // Dto -> Entity 변환
        final Entity registEntity = getWriteMapstruct().toEntity(registDto);

        // optional: 등록 전처리(entity)
        this.preRegist(registEntity);

        // managt 처리
        BaseClsfManagtHelper.applyRegistManagt(registDto, registEntity);

        // insert
        final Entity updatedEntity = this.updt(registEntity);
        final Dto updatedDto = getReadMapstruct().toDto(updatedEntity);

        // 필수 후처리(등록/수정 공통): tag/meta 전달 + 처리
        BaseClsfProcPostProcessor.afterWrite(registDto, updatedDto);

        // optional: 등록 후처리(dto)
        this.postRegist(updatedDto);

        final ServiceResponse response = new ServiceResponse();
        response.setRslt(updatedDto != null && updatedDto.getKey() != null);
        response.setRsltObj(updatedDto);
        return response;
    }

    /**
     * default: 게시물 수정 (dto level)
     *
     * @param postDto 수정할 Dto 객체
     * @return {@link ServiceResponse} -- 수정 결과 객체
     */
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    default ServiceResponse modify(final PostDto postDto) throws Exception {
        // Entity 먼저 조회
        final Entity modifyEntity = this.getDtlEntity(postDto.getKey());
        final Entity historySnapshot = BaseClsfHistoryHelper.isHistoryModule(modifyEntity)
                ? (Entity) modifyEntity.toBuilder().build()
                : null;

        // optional: 수정 전처리(dto)
        this.preModify(postDto);
        // optional: 수정 전처리(dto, entity)
        this.preModify(postDto, modifyEntity);

        // Dto -> 기존 Entity 반영
        getWriteMapstruct().updateFromDto(postDto, modifyEntity);

        // 이력 처리
        BaseClsfManagtHelper.applyModifyManagt(postDto, modifyEntity);
        BaseClsfHistoryHelper.applyModifyHistory(historySnapshot, modifyEntity);

        final Entity updatedEntity = getRepository().saveAndFlush(modifyEntity);
        final Dto updatedDto = getReadMapstruct().toDto(updatedEntity);

        // 필수 후처리(등록/수정 공통): tag/meta 전달 + 처리
        BaseClsfProcPostProcessor.afterWrite(postDto, updatedDto);
        // 이력 후처리
        final HistoryType historyType = postDto instanceof HistoryActionModule historyAction
                ? historyAction.resolveHistoryType()
                : HistoryType.CHANGE;
        final Integer fromHistoryNo = postDto instanceof HistoryActionModule historyAction
                ? historyAction.getFromHistoryNo()
                : null;
        BaseClsfHistoryHelper.publishHistoryEventIfSupported(this, historySnapshot, updatedEntity, historyType, fromHistoryNo);

        // optional: 수정 후처리(dto)
        this.postModify(postDto, updatedDto);

        final ServiceResponse response = new ServiceResponse();
        response.setRslt(updatedDto != null && updatedDto.getKey() != null);
        response.setRsltObj(updatedDto);
        return response;
    }

    /**
     * default: 삭제 (key 사용)
     *
     * @param key 삭제할 엔티티의 키
     * @return {@link ServiceResponse} -- 삭제 결과 객체
     */
    @Override
    @Transactional
    default ServiceResponse delete(final Key key) throws Exception {
        final Entity deleteEntity = this.getDtlEntity(key);
        if (deleteEntity == null) {
            throw new EntityNotFoundException("exception.EntityNotFoundException.to-delete");
        }

        final Dto deletedDto = getReadMapstruct().toDto(deleteEntity);

        // optional: 삭제 전처리(dto)
        this.preDelete(deletedDto);

        this.remove(deleteEntity);

        // 필수 후처리(삭제 공통): tag/meta 처리
        BaseClsfProcPostProcessor.afterDelete(deletedDto);

        // optional: 삭제 후처리(dto)
        this.postDelete(deletedDto);

        final ServiceResponse response = new ServiceResponse();
        response.setRslt(true);
        response.setRsltObj(deletedDto);
        return response;
    }
}
