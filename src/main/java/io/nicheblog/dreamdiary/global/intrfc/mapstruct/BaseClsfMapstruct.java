package io.nicheblog.dreamdiary.global.intrfc.mapstruct;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseClsfDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

/**
 * BaseClsfMapstruct
 * <pre>
 *  (공통/상속) MapStruct 기반 Mapper 인터페이스.
 *  (clsf 기반 요소들 변환 로직 추가)
 * <pre>
 *
 * @author nichefish
 */
public interface BaseClsfMapstruct<Dto extends BaseClsfDto, ListDto extends BaseClsfDto, Entity extends BaseClsfEntity>
        extends BaseCrudMapstruct<Dto, ListDto, Entity> {

    /**
     * default : ClsfEntity -> ClsfDto 요소들 매핑
     *
     * @param entity 매핑할 원본 Entity 객체
     * @param dto 매핑 대상인 Dto 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @AfterMapping
    default void mapClsfFields(final Entity entity, final @MappingTarget Dto dto) throws Exception {
        MapstructHelper.mapClsfFields(entity, dto);
    }

    /**
     * default : ClsfEntity -> ClsfListDto 요소들 매핑
     *
     * @param entity 매핑할 원본 Entity 객체
     * @param dto 매핑 대상인 Dto 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @AfterMapping
    default void mapClsfListFields(final Entity entity, final @MappingTarget ListDto dto) throws Exception {
        MapstructHelper.mapClsfFields(entity, dto);
    }
}
