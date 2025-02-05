package io.nicheblog.dreamdiary.extension.file.mapstruct;

import io.nicheblog.dreamdiary.extension.file.entity.AtchFileEntity;
import io.nicheblog.dreamdiary.extension.file.model.AtchFileDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * AtchFileMapstruct
 * <pre>
 *  첨부파일 MapStruct 기반 Mapper 인터페이스.
 *  ※첨부파일(atch_file) = 여러 첨부파일을 하나의 단위로 묶어놓은 객체. 첨부파일 상세(atch_file_dtl)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AtchFileMapstruct
        extends BaseCrudMapstruct<AtchFileDto, AtchFileDto, AtchFileEntity> {

    AtchFileMapstruct INSTANCE = Mappers.getMapper(AtchFileMapstruct.class);

    /**
     * Entity -> Dto 변환
     * 
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    AtchFileDto toDto(final AtchFileEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     * 
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    AtchFileDto toListDto(final AtchFileEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     * 
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    AtchFileEntity toEntity(final AtchFileDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final AtchFileDto dto, final @MappingTarget AtchFileEntity entity) throws Exception;
}
