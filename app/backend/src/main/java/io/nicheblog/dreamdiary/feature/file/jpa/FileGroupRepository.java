package io.nicheblog.dreamdiary.feature.file.jpa;

import io.nicheblog.dreamdiary.feature.file.entity.FileGroupEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * FileGroupRepository
 * <pre>
 *  첨부파일 (JPA) Repository 인터페이스.
 *  ※첨부파일(file_group) = 여러 첨부파일을 하나의 단위로 묶어놓은 객체. 첨부파일 상세(file_record)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface FileGroupRepository
        extends BaseStreamRepository<FileGroupEntity, Integer> {
    //
}

