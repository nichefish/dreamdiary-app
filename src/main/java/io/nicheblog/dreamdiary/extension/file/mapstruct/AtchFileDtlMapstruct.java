package io.nicheblog.dreamdiary.extension.file.mapstruct;

import io.nicheblog.dreamdiary.extension.file.entity.AtchFileDtlEntity;
import io.nicheblog.dreamdiary.extension.file.model.AtchFileDtlDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * AtchFileDtlMapstruct
 * <pre>
 *  첨부파일 상세 MapStruct 기반 Mapper 인터페이스.
 *  ※첨부파일 상세(atch_file_dtl) = 실제 첨부파일 정보를 담고 있는 객체. 첨부파일(atch_file)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AtchFileDtlMapstruct
        extends BaseCrudMapstruct<AtchFileDtlDto, AtchFileDtlDto, AtchFileDtlEntity> {

    AtchFileDtlMapstruct INSTANCE = Mappers.getMapper(AtchFileDtlMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    AtchFileDtlDto toDto(final AtchFileDtlEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    AtchFileDtlDto toListDto(final AtchFileDtlEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    AtchFileDtlEntity toEntity(final AtchFileDtlDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final AtchFileDtlDto dto, final @MappingTarget AtchFileDtlEntity entity) throws Exception;
}
