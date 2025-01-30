package io.nicheblog.dreamdiary.global.intrfc.mapstruct;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseClsfDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

/**
 * BasePostMapstruct
 * <pre>
 *  (공통/상속) MapStruct 기반 Mapper 인터페이스.
 *  (clsf 기반 요소들 변환 로직 추가.)
 * <pre>
 *
 * @author nichefish
 */
public interface BasePostMapstruct<Dto extends BaseClsfDto, ListDto extends BaseClsfDto, Entity extends BaseClsfEntity>
        extends BaseClsfMapstruct<Dto, ListDto, Entity> {

    /**
     * default : Entity -> Dto 변환에서 ClsfEntity 요소들을 매핑한다.
     *
     * @param entity 매핑할 원본 Entity 객체
     * @param dto 매핑 대상인 Dto 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @AfterMapping
    default void mapPostFields(final Entity entity, final @MappingTarget Dto dto) throws Exception {
        MapstructHelper.mapPostFields(entity, dto);
    }

    /**
     * default : Entity -> ListDto 변환에서 ClsfEntity 요소들을 매핑한다.
     *
     * @param entity 매핑할 원본 Entity 객체
     * @param dto 매핑 대상인 ListDto 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @AfterMapping
    default void mapPostListFields(final Entity entity, final @MappingTarget ListDto dto) throws Exception {
        MapstructHelper.mapPostFields(entity, dto);
    }
}
