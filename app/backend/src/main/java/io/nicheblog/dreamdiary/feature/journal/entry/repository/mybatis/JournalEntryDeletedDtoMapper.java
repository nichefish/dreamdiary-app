package io.nicheblog.dreamdiary.feature.journal.entry.repository.mybatis;

import org.apache.ibatis.annotations.Param;

public interface JournalEntryDeletedDtoMapper<Dto> {

    /**
     * 소프트 삭제된 엔트리를 ID로 조회한다.
     *
     * @param id 엔트리 ID
     * @return 삭제 DTO
     */
    Dto getDeletedById(@Param("id") Integer id);
}
