package io.nicheblog.dreamdiary.feature.journal.entry.repository.mybatis;

import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JournalEntryMapper {

    JournalEntryDto getDeletedByIdAndContentType(
            @Param("id") Integer id,
            @Param("contentType") String contentType
    );
}
