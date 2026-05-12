package io.nicheblog.dreamdiary.feature.file.jpa;

import io.nicheblog.dreamdiary.feature.file.entity.FileRecordEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * FileRecordRepository
 * <pre>
 *  첨부파일 상세 (JPA) Repository 인터페이스.
 *  ※첨부파일 상세(file_record) = 실제 첨부파일 정보를 담고 있는 객체. 첨부파일(file_group)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface FileRecordRepository
        extends BaseStreamRepository<FileRecordEntity, Integer> {
    //
}

