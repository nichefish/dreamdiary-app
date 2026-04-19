package io.nicheblog.dreamdiary.feature.file.mapstruct;

import io.nicheblog.dreamdiary.feature.file.entity.FileGroupEntity;
import io.nicheblog.dreamdiary.feature.file.model.FileGroupDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * FileGroupMapstruct
 * <pre>
 *  첨부파일 MapStruct 기반 Mapper 인터페이스.
 *  ※첨부파일(file_group) = 여러 첨부파일을 하나의 단위로 묶어놓은 객체. 첨부파일 상세(file_record)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileGroupMapstruct
        extends BaseWriteMapstruct<FileGroupDto, FileGroupEntity>, BaseReadMapstruct<FileGroupDto, FileGroupEntity> {

    FileGroupMapstruct INSTANCE = Mappers.getMapper(FileGroupMapstruct.class);

    /**
     * Entity -> Dto 변환
     * 
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    FileGroupDto toDto(final FileGroupEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     * 
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    FileGroupEntity toEntity(final FileGroupDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final FileGroupDto dto, final @MappingTarget FileGroupEntity entity) throws Exception;
}
