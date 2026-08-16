package io.nicheblog.dreamdiary.feature.journal.chapter.repository.mybatis;

import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * JournalChapterMapper
 * <pre>
 *  저널 챕터 MyBatis 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface JournalChapterMapper {

    /**
     * 삭제된 데이터 정보 조회
     * @param id 조회할 게시글 번호 (삭제된 데이터)
     * @return {@link JournalChapterDto} -- 삭제된 저널 챕터 데이터
     */
    JournalChapterDto getDeletedById(final @Param("id") Integer id);

}

