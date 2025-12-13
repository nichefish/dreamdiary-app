package io.nicheblog.dreamdiary.global.intrfc.mapstruct;

import org.mapstruct.Named;

/**
 * BaseMapstruct
 * <pre>
 *  (공통/상속) MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseMapstruct<Dto, Entity> {

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Named("toDto")
    Dto toDto(final Entity entity) throws Exception;
}
