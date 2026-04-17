package io.nicheblog.dreamdiary.feature.board.group.mapstruct;

import io.nicheblog.dreamdiary.feature.admin.menu.model.SiteAcsInfo;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BoardMapstruct extends BaseWriteMapstruct<BoardDto, BoardEntity>, BaseReadMapstruct<BoardDto, BoardEntity> {

    BoardMapstruct INSTANCE = Mappers.getMapper(BoardMapstruct.class);

    @Override
    @Named("toDto")
    BoardDto toDto(final BoardEntity entity) throws Exception;

    @Override
    BoardEntity toEntity(final BoardDto dto) throws Exception;

    @Mapping(target = "boardKey", source = "boardKey")
    @Mapping(target = "menuNm", expression = "java(entity.getBoardName())")
    SiteAcsInfo toMenu(final BoardEntity entity) throws Exception;

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final BoardDto dto, final @MappingTarget BoardEntity entity) throws Exception;
}
