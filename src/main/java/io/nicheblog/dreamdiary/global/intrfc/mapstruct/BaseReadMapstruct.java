package io.nicheblog.dreamdiary.global.intrfc.mapstruct;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * BaseCrudMapstruct
 * <pre>
 *  (공통/상속) MapStruct 기반 Mapper 인터페이스.
 *  (Entity -> ListDto 변환 메소드 추가.)
 * <pre>
 *
 * @author nichefish
 */
public interface BaseReadMapstruct<Dto extends BaseCrudDto, Entity extends BaseCrudEntity>
        extends BaseMapstruct<Dto, Entity> {

    /**
     * EntityList to DtoList
     *
     * @param entityList 변환할 Entity 목록
     * @return {@link List} -- 변환된 Dto 목록
     */
    default List<Dto> toDtoList(final List<Entity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) return null;
        AtomicLong i = new AtomicLong(1);
        return entityList.stream()
                .map(entity -> {
                    try {
                        Dto dto = this.toDto(entity);
                        dto.setRnum(i.getAndIncrement());
                        return dto;
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * entityPage to DtoPage
     *
     * @param entityPage 변환할 Entity 목록
     * @return {@link List} -- 변환된 Dto 목록
     */
    default Page<Dto> toDtoPage(final Page<Entity> entityPage) {
        if (CollectionUtils.isEmpty(entityPage.getContent())) return null;
        final AtomicInteger counter = new AtomicInteger(1);
        final List<Dto> dtoList = entityPage.stream()
                .map(entity -> {
                    try {
                        final Dto dto = this.toDto(entity);
                        dto.setRnum(CmmUtils.getPageRnum(entityPage, counter.getAndIncrement()));
                        return dto;
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }


    /* ----- */

    /**
     * default : BaseEntity -> BaseDto 기본 요소들 매핑 (toDto)
     *
     * @param entity 매핑할 원본 Entity 객체
     * @param dto 매핑 대상인 Dto 객체
     */
    @AfterMapping
    default void mapBaseFields(final Entity entity, final @MappingTarget Dto dto) throws Exception {
        MapstructHelper.mapBaseFields(entity, dto);
    }

    /**
     * default : BaseEntity -> BaseListDto기본 요소들 매핑 (toListDto)
     *
     * @param entity 매핑할 원본 Entity 객체
     * @param dto 매핑 대상인 ListDto 객체
     */
    @AfterMapping
    default void mapBaseListFields(final Entity entity, final @MappingTarget Dto dto) throws Exception {
        MapstructHelper.mapBaseFields(entity, dto);
    }
}
