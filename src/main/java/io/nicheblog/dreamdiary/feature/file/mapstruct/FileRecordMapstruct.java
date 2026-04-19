package io.nicheblog.dreamdiary.feature.file.mapstruct;

import io.nicheblog.dreamdiary.feature.file.entity.FileRecordEntity;
import io.nicheblog.dreamdiary.feature.file.model.FileRecordDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * FileRecordMapstruct
 * <pre>
 *  첨부파일 상세 MapStruct 기반 Mapper 인터페이스.
 *  ※첨부파일 상세(file_record) = 실제 첨부파일 정보를 담고 있는 객체. 첨부파일(file_group)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileRecordMapstruct
        extends BaseWriteMapstruct<FileRecordDto, FileRecordEntity>, BaseReadMapstruct<FileRecordDto, FileRecordEntity> {

    FileRecordMapstruct INSTANCE = Mappers.getMapper(FileRecordMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    FileRecordDto toDto(final FileRecordEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    FileRecordEntity toEntity(final FileRecordDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final FileRecordDto dto, final @MappingTarget FileRecordEntity entity) throws Exception;
}
