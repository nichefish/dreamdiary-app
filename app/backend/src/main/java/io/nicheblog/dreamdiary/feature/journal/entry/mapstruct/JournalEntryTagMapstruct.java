package io.nicheblog.dreamdiary.feature.journal.entry.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryTagEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JournalEntryTagMapstruct
        extends BaseReadMapstruct<TagDto, JournalEntryTagEntity> {

    JournalEntryTagMapstruct INSTANCE = Mappers.getMapper(JournalEntryTagMapstruct.class);

    /**
     * 태그 엔티티를 태그 DTO로 변환한다.
     *
     * @param entity 원본 엔티티
     * @return 태그 DTO
     * @throws Exception 변환 중 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "ctgr", expression = "java(entity.getCtgr())")
    TagDto toDto(final JournalEntryTagEntity entity) throws Exception;
}
