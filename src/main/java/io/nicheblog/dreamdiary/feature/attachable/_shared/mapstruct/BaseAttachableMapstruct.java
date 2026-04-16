package io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.helper.AttachableMapstructHelper;
import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

/**
 * BaseAttachableMapstruct
 * <pre>
 *  (공통/상속) MapStruct 기반 Mapper 인터페이스.
 *  (attachable 기반 요소들 변환 로직 추가)
 * </pre>
 *
 * @author nichefish
 */
public interface BaseAttachableMapstruct<Dto extends BaseAttachableDto, Entity extends BaseAttachableEntity>
        extends BaseReadMapstruct<Dto, Entity> {

    /**
     * default : AttachableEntity -> AttachableDto 요소들 매핑
     *
     * @param entity 매핑할 원본 Entity 객체
     * @param dto 매핑 대상인 Dto 객체
     */
    @AfterMapping
    default void mapAttachableFields(final Entity entity, final @MappingTarget Dto dto) throws Exception {
        AttachableMapstructHelper.mapAttachableFields(entity, dto);
    }
}
